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
import org.aravind.oss.kafka.connect.lib.SourceOffset;
import org.aravind.oss.kafka.connect.lib.Partitions;
import org.aravind.oss.kafka.connect.lib.SourcePartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;

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

    private Time time;
    private long lastUpdate;
    private long pollIntervalInMillis;
    private static int totalJenkinsPulls = 1;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final static Partitions partitions = new Partitions(JenkinsSourceTask.JOB_NAME);

    private Map<String, String> taskProps;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean stop;
    private ReadYourWritesOffsetStorageAdapter storageAdapter;

    public JenkinsSourceTask() {
        this.time = new SystemTime();
    }

    @Override
    public String version() {
        return Version.get();
    }

    @Override
    public void start(Map<String, String> props) {
        logger.info("JenkinsSourceTask starting");
        lastUpdate = 0;
        taskProps = props;
        pollIntervalInMillis = Long.parseLong(taskProps.get(JENKINS_POLL_INTERVAL_MS_CONFIG));
        stop = new AtomicBoolean(false);
    }

    public Optional<SourceRecord> createSourceRecord(String jobUrl, Long lastSavedBuildNumber) {
        JenkinsClient client = null;

        try {
	    if (getJenkinsUsername() != null && !getJenkinsUsername().isEmpty()) {
                client = new JenkinsClient(new URL(jobUrl + "api/json"), getJenkinsUsername(), getJenkinsPassword(), getJenkinsConnTimeout(), getJenkinsReadTimeout());
            }
            else {
		client = new JenkinsClient(new URL(jobUrl + "api/json"), getJenkinsConnTimeout(), getJenkinsReadTimeout());
	    }            
        } 
	catch (JenkinsException je) {
  	    logger.error("Can't create URL object for Jenkins server at {}.", jobUrl, je);
            //TODO Silently log the error and ignore? What should we do?
	}
	catch (MalformedURLException e) {
            logger.error("Can't create URL object for Jenkins server at {}.", jobUrl, e);
            //TODO Silently log the error and ignore? What should we do?
        }
        Optional<String> resp = Optional.empty();
        if (client != null) {
            try {
                logger.debug("GET job details for {}", jobUrl + "api/json");
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

                logger.trace("Builds are: {}", builds);

                if (builds != null) {
                    SourcePartition partition = partitions.make(builds.getName());

                    Build lastBuild = builds.getLastBuild();

                    //Some jobs might not have any builds. TODO need to figure out how to represent these
                    //And the lastBuild might have already been stored

                    if (lastBuild != null && !lastBuild.getNumber().equals(lastSavedBuildNumber)) {
                        Long offsetValue = lastBuild.getNumber();
                        logger.debug("Partition: {}, lastBuild: {}, lastSavedBuild: {}", partition.value, offsetValue, lastSavedBuildNumber);
                        SourceOffset sourceOffset = SourceOffset.make(BUILD_NUMBER, offsetValue);

                        //get Build details
                        lastBuild.setConnTimeoutInMillis(getJenkinsConnTimeout());
                        lastBuild.setReadTimeoutInMillis(getJenkinsReadTimeout());
			lastBuild.setUsername(getJenkinsUsername());
			lastBuild.setPassword(getJenkinsPassword());
                        Optional<String> lastBuildDetails = lastBuild.getDetails();

                        if (lastBuildDetails.isPresent()) {
                            //add build details JSON string as the value
                            logger.debug("Create SourceRecord for {}", partition.value);
                            SourceRecord record = new SourceRecord(partition.encoded, sourceOffset.encoded, taskProps.get(TOPIC_CONFIG), Schema.STRING_SCHEMA, partition.value, Schema.STRING_SCHEMA, lastBuildDetails.get());
                            storageAdapter.cache(partition, sourceOffset);

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
            long now = time.milliseconds();
            logger.trace("Now: {}", sdf.format(new Date(now)));
            long nextUpdate = 0;

            //Check if poll time had elapsed
            if (lastUpdate == 0) {
                logger.trace("First call after starting the connector. So Pulling the Jobs from Jenkins now.");
                nextUpdate = now;
            } else {
                nextUpdate = lastUpdate + pollIntervalInMillis;
                logger.trace("Next pull from Jenkins should happen at {} (approx).", nextUpdate);
            }
            long untilNext = nextUpdate - now;
            logger.debug("now: {}, nextUpdate: {}, untilNext: {}", sdf.format(new Date(now)), sdf.format(new Date(nextUpdate)), untilNext);

            if (untilNext > 0) {
                logger.info("Waiting {} ms before next pull", untilNext);
                time.sleep(untilNext);
                continue;
            }

            logger.debug("Total pulls from Jenkins so far: {}", totalJenkinsPulls);
            String jobUrls = taskProps.get(JOB_URLS);
            storageAdapter = new ReadYourWritesOffsetStorageAdapter(context.offsetStorageReader(), jobUrls, partitions);

            String[] jobUrlArray = jobUrls.split(",");

            List<SourceRecord> records = new ArrayList<>();
            for (String jobUrl : jobUrlArray) {

                SourcePartition partition = partitions.make(urlDecode(extractJobName(jobUrl)));

                logger.trace("Get lastSavedOffset for: '{}' with partitionValue: {}", jobUrl, partition.value);
                Optional<SourceOffset> offset = storageAdapter.getOffset(partition);

                Long lastSavedBuildNumber = null;
                if (offset.isPresent()) {
                    logger.debug("lastSavedOffset for '{}' is: {}", partition.value, offset.get());
                    lastSavedBuildNumber = (Long) offset.get().value;
                } else {
                    logger.debug("lastSavedOffset not available for: {}", partition.value);
                }
                Optional<SourceRecord> sourceRecord = createSourceRecord(jobUrl, lastSavedBuildNumber);
                if (sourceRecord.isPresent()) records.add(sourceRecord.get());
            }

            logger.info("Total SourceRecords created: {}. Returning these from poll()", records.size());

            //Update the last updated time to now just before returning the call
            lastUpdate = time.milliseconds();
            logger.debug("Setting the lastUpdate time to : {}", sdf.format(new Date(lastUpdate)));

            totalJenkinsPulls++;
            return records;
        }

        logger.debug("Returning null from poll(). This is because the runtime called shutdown.");
        //Only in case make shutdown. null indicates no data
        return null;
    }

    @Override
    public synchronized void stop() {
        logger.info("JenkinsSourceTask stopping");
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

    private String getJenkinsUsername() {
        return String.valueOf(taskProps.getOrDefault(JENKINS_USERNAME_CONFIG, ""));
    }

    private String getJenkinsPassword() {
        return String.valueOf(taskProps.getOrDefault(JENKINS_PASSWORD_OR_API_TOKEN_CONFIG, ""));
    }

}
