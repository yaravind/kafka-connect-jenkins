package org.aravind.oss.kafka.connect.jenkins;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.util.ConnectorUtils;
import org.aravind.oss.jenkins.JenkinsException;
import org.aravind.oss.jenkins.JenkinsClient;
import org.aravind.oss.jenkins.domain.Jenkins;
import org.aravind.oss.jenkins.domain.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Is responsible for breaking the job into a set of tasks  that can be distributed to workers.
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsSourceConnector extends SourceConnector {
    private JenkinsSourceConfig jenkinsCfg;
    private JenkinsClient client;
    private static Logger logger = LoggerFactory.getLogger(JenkinsSourceConnector.class);

    @Override
    public String version() {
        return Version.get();
    }

    /**
     * Start this Connector. This method will only be called on a clean Connector, i.e. it has either just been
     * instantiated and initialized or stop() has been invoked.
     *
     * @param props
     */
    @Override
    public void start(Map<String, String> props) {
        logger.debug("Starting the Jenkins Connector");
        jenkinsCfg = new JenkinsSourceConfig(props);

        //Do a test connection to Fail Fast
        try {
            logger.trace("Doing a test connection to {}", jenkinsCfg.getJobsResource());
            client = new JenkinsClient(jenkinsCfg.getJobsResource(), jenkinsCfg.getJenkinsConnTimeout(), jenkinsCfg.getJenkinsReadTimeout());
            HttpURLConnection connection = client.connect();
            connection.disconnect();
        } catch (JenkinsException e) {
            throw new ConnectException("Unable to open connection to " + jenkinsCfg.getJenkinsUrl(), e);
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return JenkinsSourceTask.class;
    }

    /**
     * Returns a set of configurations for {@link JenkinsSourceTask} based on the current configuration, producing at most {@code numTasks} configurations.
     *
     * @param maxTasks maximum number of configurations to generate
     * @return configurations for Tasks
     */
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        logger.debug("Calculating taskConfigs");
        Optional<Jenkins> resp = null;
        try {
            resp = client.getJenkins();
        } catch (JenkinsException e) {
            //TODO sometimes the client might have been brought down. Let us handle it silently for now.
            logger.warn("Error while GET to " + jenkinsCfg.getJobsResource() + ". Ignoring it.", e);
        }

        TaskConfigExtractor taskConfigExtractor = new JobTaskConfigExtractor();

        if (resp.isPresent()) {
            Jenkins jenkins = resp.get();

            TaskConfigBuilder<Job> taskCfgBuilder = new TaskConfigBuilder<Job>(maxTasks, JenkinsSourceTask.JOB_URLS, jenkinsCfg, taskConfigExtractor);

            return taskCfgBuilder.build(jenkins.getJobs());
        }
        return Collections.emptyList();
    }

    @Override
    public void stop() {
        logger.debug("Stopping the Connector");
        //Not used at this moment
    }

    @Override
    public ConfigDef config() {
        return JenkinsSourceConfig.DEFS;
    }

    public JenkinsSourceConfig getJenkinsCfg() {
        return jenkinsCfg;
    }
}
