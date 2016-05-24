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
    private Map<String, String> taskProps;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean stop;
    private static final Logger logger = LoggerFactory.getLogger(JenkinsSourceTask.class);

    @Override
    public String version() {
        return Version.get();
    }

    @Override
    public void start(Map<String, String> props) {
        taskProps = props;
        stop = new AtomicBoolean(false);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        //Keep trying in a loop until stop() is called on this instance.
        //TODO use RxJava for this in future
        while (!stop.get()) {
            String jobUrls = taskProps.get(JOB_URLS);

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
                List<SourceRecord> records = null;
                try {
                    Optional<String> resp = client.get(512);
                    if (resp.isPresent()) {
                        //build SourceRecords
                        try {
                            BuildCollection builds = mapper.readValue(resp.get(), BuildCollection.class);
                            logger.debug("Builds are: ", builds);
                            Map sourcePartition = Collections.singletonMap("jobName", builds.getName());
                            records = new ArrayList<>(builds.getBuilds().size());
                            for (Build build : builds.getBuilds()) {
                                Map sourceOffset = Collections.singletonMap("buildNumber", build.getNumber());
                                SourceRecord record = new SourceRecord(sourcePartition, sourceOffset, "jenkines.test", Schema.STRING_SCHEMA, resp.get());
                                records.add(record);
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
        if (stop != null) stop.set(true);
    }

    @Override
    public void initialize(SourceTaskContext context) {
        super.initialize(context);
    }
}
