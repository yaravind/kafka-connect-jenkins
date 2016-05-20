package org.aravind.oss.kafka.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsSourceTask extends SourceTask {
    public static final String JOB_URLS = "job.urls";
    private Map<String, String> taskProps;
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JenkinsSourceTask.class);

    @Override
    public String version() {
        return Version.get();
    }

    @Override
    public void start(Map<String, String> props) {
        taskProps = props;
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

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
            try {
                Optional<String> resp = client.get(512);
                if (resp.isPresent()) {
                    try {
                        BuildCollection builds = mapper.readValue(resp.get(), BuildCollection.class);
                        logger.trace("Builds are: ", builds);
                        System.out.println(builds);
                    } catch (IOException e) {
                        logger.error("Error while parsing the Build JSON {} for {}", resp.get(), jobUrl + "api/json", e);
                    }
                }
            } catch (JenkinsException e) {
                logger.error("Can't do a GET to resource {}", jobUrl, e);
                //TODO Silently log the error and ignore? What should we do?
            }
        }
        return null;
    }

    @Override
    public synchronized void stop() {
        //Nothing to stop as of now. May be in future.
    }

    @Override
    public void initialize(SourceTaskContext context) {
        super.initialize(context);
    }
}
