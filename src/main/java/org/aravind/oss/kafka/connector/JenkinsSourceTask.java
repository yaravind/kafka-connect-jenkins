package org.aravind.oss.kafka.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.aravind.oss.jenkins.JenkinsClient;
import org.aravind.oss.jenkins.JenkinsException;
import org.aravind.oss.jenkins.domain.Build;
import org.aravind.oss.jenkins.domain.BuildCollection;
import org.aravind.oss.jenkins.domain.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsSourceTask extends SourceTask {
    public static final String JOB_URLS = "job.urls";
    public static final String JOB_NAME = "jobName";
    public static final String BUILD_NUMBER = "buildNumber";
    private static final Logger logger = LoggerFactory.getLogger(JenkinsSourceTask.class);

    private Map<String, String> taskProps;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean stop;
    private Map<Map<String, String>, Map<String, Long>> offsets;

    @Override
    public String version() {
        return Version.get();
    }

    @Override
    public void start(Map<String, String> props) {
        logger.debug("Starting the Task");
        taskProps = props;
        stop = new AtomicBoolean(false);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        //Keep trying in a loop until stop() is called on this instance.
        //TODO use RxJava for this in future
        while (!stop.get()) {
            String jobUrls = taskProps.get(JOB_URLS);
            List<SourceRecord> records = new ArrayList<>();

            //handle single job until we get this right
            String jobUrl = jobUrls.split(",")[0];
            JenkinsClient client = null;

            try {
                client = new JenkinsClient(new URL(jobUrl + "api/json"));
            } catch (MalformedURLException e) {
                logger.error("Can't create URL object for {}", jobUrl, e);
                //TODO Silently log the error and ignore? What should we do?
            }

            if (client != null) {
                try {
                    Optional<String> resp = client.get();
                    if (resp.isPresent()) {
                        //build SourceRecords
                        try {
                            BuildCollection builds = mapper.readValue(resp.get(), BuildCollection.class);
                            logger.debug("Builds are: ", builds);
                            Map<String, String> sourcePartition = Collections.singletonMap(JOB_NAME, builds.getName());

                            Build lastBuild = builds.getLastBuild();
                            Map<String, Long> sourceOffset = Collections.singletonMap(BUILD_NUMBER, lastBuild.getNumber());

                            //get Build details
                            Optional<String> lastBuildDetails = lastBuild.getDetails();
                            if (lastBuildDetails.isPresent()) {
                                //add build details JSON string as the value
                                SourceRecord record = new SourceRecord(sourcePartition, sourceOffset, taskProps.get(JenkinsSourceConfig.TOPIC_CONFIG), Schema.STRING_SCHEMA, lastBuildDetails.get());
                                records.add(record);
                            } else {
                                logger.warn("Ignoring job details for {}. Not creating SourceRecord.", lastBuild.getBuildDetailsResource());
                            }
                        } catch (IOException e) {
                            logger.error("Error while parsing the Build JSON {} for {}", resp.get(), jobUrl + "api/json", e);
                        }
                    } else {
                        //If no builds were found
                        continue;
                    }
                    return records;
                } catch (JenkinsException e) {
                    logger.error("Can't do a GET to resource {}", jobUrl, e);
                    //TODO Silently log the error and ignore? What should we do?
                }
            }
        }
        //null indicates no data
        return null;
    }

    @Override
    public synchronized void stop() {
        logger.debug("Stopping the Task");
        if (stop != null) stop.set(true);
    }

    @Override
    public void initialize(SourceTaskContext context) {
        super.initialize(context);
    }


    public boolean containsPartition(String partitionValue) {
        return offsets.keySet().contains(Collections.singletonMap(JOB_NAME, partitionValue));
    }

    public Optional<Map<String, Long>> getOffset(String partitionValue) {
        return Optional.ofNullable(offsets.get(Collections.singletonMap(JOB_NAME, partitionValue)));
    }
}
