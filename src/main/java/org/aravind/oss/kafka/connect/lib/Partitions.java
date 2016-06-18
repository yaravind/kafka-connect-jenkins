package org.aravind.oss.kafka.connect.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class Partitions {
    private static final Map<String, SourcePartition> cache = new HashMap<>();
    private final String partitionKey;
    private static final Logger logger = LoggerFactory.getLogger(Partitions.class);

    public Partitions(String key) {
        partitionKey = key;
    }

    /**
     * Has a <b>side-effect</b> of caching the partition for future if it doesn't already exist in the cache.
     *
     * @param partition Name (or value) of the partition
     * @return Cached {@link SourcePartition}
     */
    public SourcePartition of(String partition) {
        if (!cache.containsKey(partition)) {
            logger.trace("Adding {} to cache.", partition);
            cache.put(partition, new SourcePartition(partitionKey, partition));
        }
        return cache.get(partition);
    }
}
