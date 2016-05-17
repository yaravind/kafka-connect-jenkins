package org.aravind.oss.kafka.connector;

import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.util.ConnectorUtils;
import org.aravind.oss.jenkins.JenkinsException;
import org.aravind.oss.jenkins.JenkinsClient;
import org.aravind.oss.jenkins.domain.Jenkins;
import org.aravind.oss.jenkins.domain.Job;

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
        jenkinsCfg = new JenkinsSourceConfig(props);

        //Initialize connection to Jenkins instance
        try {
            client = new JenkinsClient(jenkinsCfg.getJobsResource());

            //Do a test connection to Fail Fast
            HttpURLConnection connection = client.connect();
            connection.disconnect();
        } catch (JenkinsException e) {
            throw new ConnectException("Unable to open connection to " + jenkinsCfg.getJenkinsUrl(), e);
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return null;
    }

    /**
     * Returns a set of configurations for {@link JenkinsSourceTask} based on the current configuration, producing at most {@code numTasks} configurations.
     *
     * @param maxTasks maximum number of configurations to generate
     * @return configurations for Tasks
     */
    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        Optional<Jenkins> resp = null;
        try {
            resp = client.getJenkins();
        } catch (JenkinsException e) {
            //TODO sometimes the client might have been brought down. Let us handle it silently for now.
            e.printStackTrace();
        }

        if (resp.isPresent()) {
            Jenkins jenkins = resp.get();

            //Create job groups
            int jobsCount = jenkins.getJobs().size();
            int numGroups = Math.min(jobsCount, maxTasks);
            List<List<Job>> jobGroups = ConnectorUtils.groupPartitions(jenkins.getJobs(), numGroups);

            //Create task configs for each group
            List<Map<String, String>> taskConfigs = new ArrayList<>(jobGroups.size());

            for (List<Job> group : jobGroups) {
                Map<String, String> taskProps = new HashMap<>();

                //Concatenate all job urls that need to be handled by a single task
                String commaSeparatedJobUrls = group.stream()
                        .map(j -> j.getUrl())
                        .collect(Collectors.joining(","));

                taskProps.put(JenkinsSourceTask.JOB_URLS, commaSeparatedJobUrls);

                taskConfigs.add(taskProps);
            }
            return taskConfigs;
        }
        return Collections.emptyList();
    }

    @Override
    public void stop() {
    }
}
