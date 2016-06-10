package org.aravind.oss.kafka.connect.jenkins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.aravind.oss.jenkins.JenkinsClient;
import org.aravind.oss.jenkins.JenkinsException;
import org.aravind.oss.jenkins.domain.Build;
import org.aravind.oss.jenkins.domain.BuildCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.valueOf;
import static org.aravind.oss.kafka.connect.jenkins.JenkinsSourceConfig.*;
import static org.aravind.oss.kafka.connect.jenkins.JenkinsSourceConfig.JENKINS_CONN_TIMEOUT_CONFIG;
import static org.aravind.oss.kafka.connect.jenkins.Util.extractJobName;
import static org.aravind.oss.kafka.connect.jenkins.Util.urlDecode;

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
    private ReadYourWritesOffsetStorageAdapter storageAdapter;

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

    public Optional<SourceRecord> createSourceRecord(String jobUrl, Long lastSavedBuildNumber) {
        JenkinsClient client = null;

        try {
            client = new JenkinsClient(new URL(jobUrl + "api/json"), getJenkinsConnTimeout(), getJenkinsReadTimeout());
        } catch (MalformedURLException e) {
            logger.error("Can't create URL object for {}.", jobUrl, e);
            //TODO Silently log the error and ignore? What should we do?
        }
        Optional<String> resp = Optional.empty();
        if (client != null) {
            try {
                logger.trace("GET job details for {}", jobUrl + "api/json");
                resp = client.get();
            } catch (JenkinsException e) {
                logger.warn("Can't do a GET to resource {}", jobUrl + "api/json", e);
                //TODO Silently log the error and ignore? What should we do?
            }
            if (resp.isPresent()) {
                logger.trace("Resp: {}", resp.get());
                BuildCollection builds = null;

                //build SourceRecords
                try {
                    builds = mapper.readValue(resp.get(), BuildCollection.class);
                } catch (IOException e) {
                    logger.error("Error while parsing the Build JSON {} for {}", resp.get(), jobUrl + "api/json", e);
                }

                logger.debug("Builds are: {}", builds);

                if (builds != null) {
                    String partitionValue = builds.getName();
                    Map<String, String> sourcePartition = Collections.singletonMap(JOB_NAME, partitionValue);

                    Build lastBuild = builds.getLastBuild();

                    //Some jobs might not have any builds. TODO need to figure out how to represent these
                    //And the lastBuild might have already been stored
                    if (lastBuild != null && !lastBuild.getNumber().equals(lastSavedBuildNumber)) {
                        logger.trace("Partition: {}, lastBuild: {}, lastSavedBuild: {}", partitionValue, lastBuild.getNumber(), lastSavedBuildNumber);
                        Map<String, Long> sourceOffset = Collections.singletonMap(BUILD_NUMBER, lastBuild.getNumber());

                        //get Build details
                        lastBuild.setConnTimeoutInMillis(getJenkinsConnTimeout());
                        lastBuild.setReadTimeoutInMillis(getJenkinsReadTimeout());
                        Optional<String> lastBuildDetails = lastBuild.getDetails();

                        if (lastBuildDetails.isPresent()) {
                            //add build details JSON string as the value
                            logger.debug("Create SourceRecord");
                            SourceRecord record = new SourceRecord(sourcePartition, sourceOffset, taskProps.get(TOPIC_CONFIG), Schema.STRING_SCHEMA, lastBuildDetails.get());
                            return Optional.of(record);
                        } else {
                            logger.debug("Ignoring job details for {} as there are no builds for this Job. Not creating SourceRecord.", lastBuild.getBuildDetailsResource());
                        }
                    } else {
                        logger.debug("Not creating SourceRecord for {} because either the lastBuild details aren't available or it was already saved earlier", jobUrl + "api/json");
                    }
                }
            } else {
                //If no builds were found
                logger.debug("No builds were found for {}", jobUrl);
            }

        }
        return Optional.empty();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        logger.debug("In poll()");

        //Keep trying in a loop until stop() is called on this instance.
        //TODO use RxJava for this in future
        while (!stop.get()) {
            String jobUrls = taskProps.get(JOB_URLS);
            storageAdapter = new ReadYourWritesOffsetStorageAdapter(context.offsetStorageReader(), jobUrls);

            String[] jobUrlArray = jobUrls.split(",");

            List<SourceRecord> records = new ArrayList<>();
            for (String jobUrl : jobUrlArray) {
                String partitionValue = urlDecode(extractJobName(jobUrl));

                logger.debug("Get lastSavedOffset for {} with partitionValue as {}", jobUrl, partitionValue);
                Optional<Map<String, Object>> offset = storageAdapter.getOffset(partitionValue);

                Long lastSavedBuildNumber = null;
                if (offset.isPresent()) {
                    logger.debug("lastSavedOffset for {} is {}", partitionValue, offset);
                    lastSavedBuildNumber = (Long) offset.get().get(BUILD_NUMBER);
                } else {
                    logger.debug("lastSavedOffset not available for: {}", partitionValue);
                }
                Optional<SourceRecord> sourceRecord = createSourceRecord(jobUrl, lastSavedBuildNumber);
                if (sourceRecord.isPresent()) records.add(sourceRecord.get());
            }
            logger.debug("Total SourceRecords created: {}. Returning these from poll()", records.size());
            return records;
        }

        logger.debug("Returning null from poll(). This is because the runtime called shutdown.");
        //Only in case of shutdown. null indicates no data
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

    private int getJenkinsReadTimeout() {
        return Integer.valueOf(taskProps.getOrDefault(JENKINS_READ_TIMEOUT_CONFIG, valueOf(JENKINS_READ_TIMEOUT_DEFAULT)));
    }

    private int getJenkinsConnTimeout() {
        return Integer.valueOf(taskProps.getOrDefault(JENKINS_CONN_TIMEOUT_CONFIG, valueOf(JENKINS_CONN_TIMEOUT_DEFAULT)));
    }
}
