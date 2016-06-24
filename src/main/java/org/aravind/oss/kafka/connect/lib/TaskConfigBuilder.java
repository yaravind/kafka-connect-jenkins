package org.aravind.oss.kafka.connect.lib;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.util.ConnectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class encapsulates the following common behavior of building task configurations
 * <p>
 * <ul>
 * <li>Group all the work into multiple smaller work groups.</li>
 * <li>For each smaller work group, creates a <i>task config</i> that will be handed over to a single {@link SourceTask}</li>
 * <li>Each task config is encoded as a comma separated string. This is the simpler convention followed by few other connectors as well. The {@link SourceTask} is expected to split this String with comma delimiter.</li>
 * <li>Adds the connector specific configuration to <i>task config</i> so that it can be forwarded to {@link SourceTask}</li>
 * </ul>
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class TaskConfigBuilder<T> {
    private final int maxTasks;
    private final AbstractConfig connectorCfg;
    private final TaskConfigExtractor<T> taskCfgExtractor;
    private final String taskCfgKey;

    private static Logger logger = LoggerFactory.getLogger(TaskConfigBuilder.class);

    /**
     * @param maxTasks            The maximum simultaneous tasks configured to be run by connector.
     * @param taskConfigKey       The key to be used while adding the task config.
     * @param connectorCfg        The reference to the custom config class you wrote extending from {@link AbstractConfig}. Null if not applicable.
     * @param taskConfigExtractor The class used to extract the task configuration. This is used as the value for key <i>taskConfigKey</i>.
     */
    public TaskConfigBuilder(int maxTasks, String taskConfigKey, AbstractConfig connectorCfg, TaskConfigExtractor taskConfigExtractor) {
        this.maxTasks = maxTasks;
        this.connectorCfg = connectorCfg;
        this.taskCfgExtractor = taskConfigExtractor;
        this.taskCfgKey = taskConfigKey;
    }

    /**
     * Builds task config for each task.
     *
     * @param work The entire work expected to be fulfilled by this connector
     * @return The task configuration for each of the tasks
     */
    public List<Map<String, String>> build(List<T> work) {
        //Create job groups
        int workSizeCount = work.size();
        int numGroups = Math.min(workSizeCount, maxTasks);

        logger.debug("Total work size: {} units. maxTasks: {}. numGroups: {}.", workSizeCount, maxTasks, numGroups);

        List<List<T>> workGroups = ConnectorUtils.groupPartitions(work, numGroups);
        logger.debug("Number of work groups created: {}.", workGroups.size());

        //Create task configs for each group
        List<Map<String, String>> taskConfigs = new ArrayList<>(workGroups.size());

        for (List<T> group : workGroups) {
            //Forward the config from connector to SourceTask so that they have access to the configured TOPIC NAME
            Map<String, String> taskProps = new HashMap<>(connectorCfg.originalsStrings());

            //Concatenate all job urls that need to be handled by a single task
            String commaSeparatedJobUrls = group.stream()
                    .map(j -> taskCfgExtractor.extract(j))
                    .collect(Collectors.joining(","));

            taskProps.put(taskCfgKey, commaSeparatedJobUrls);
            logger.trace("taskProps: {}", taskProps);
            taskConfigs.add(taskProps);
        }
        return taskConfigs;
    }
}