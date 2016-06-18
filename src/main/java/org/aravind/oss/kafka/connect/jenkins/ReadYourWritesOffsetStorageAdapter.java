package org.aravind.oss.kafka.connect.jenkins;

import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.aravind.oss.kafka.connect.lib.Partitions;
import org.aravind.oss.kafka.connect.lib.SourcePartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.aravind.oss.kafka.connect.jenkins.Util.*;

/**
 * Sources cache the offsets and periodically flush(or commit) them asynchronously. The following two properties
 * are used to control this behavior.
 * <p>
 * <ul>
 * <li>offset.flush.timeout.ms = 5000</li>
 * <li>offset.flush.interval.ms = 1000</li>
 * </ul>
 * <p>
 * Currently the {@link org.apache.kafka.connect.storage.OffsetStorageReader} only reads the offsets that
 * have been flushed. The cached offsets are not returned. However, the read-your-writes consistency
 * model is needed to simplify source connector development.
 * </p>
 * <p>
 * Read-your-writes consistency: A value written by a process on a data item X will be always available to a
 * successive read operation performed by the same process on data item X.
 * </p>
 * <p>
 * This class provides the Read-your-Writes semantics within a single {@link org.apache.kafka.connect.source.SourceTask}
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class ReadYourWritesOffsetStorageAdapter {

    private OffsetStorageReader storageReader;

    private final Partitions partitions;

    //task level cache
    private Map<Map<String, String>, Map<String, Object>> cache = new HashMap<>();

    //task level offsets from StorageReader
    private Map<Map<String, String>, Map<String, Object>> offsets;

    private static final Logger logger = LoggerFactory.getLogger(ReadYourWritesOffsetStorageAdapter.class);

    public ReadYourWritesOffsetStorageAdapter(OffsetStorageReader reader, String jobUrls, Partitions ps) {
        storageReader = reader;
        partitions = ps;
        offsets = loadAndGetOffsets(storageReader, jobUrls);
        logger.debug("Loaded offsets: {}", offsets);
    }

    public void cache(String jobName, Object buildNumber) {
        Map<String, String> key = Collections.singletonMap(JenkinsSourceTask.JOB_NAME, jobName);
        Map<String, Object> value = Collections.singletonMap(JenkinsSourceTask.BUILD_NUMBER, buildNumber);

        cache.put(key, value);
    }

    public boolean containsPartition(String partitionValue) {
        Map<String, String> key = Collections.singletonMap(JenkinsSourceTask.JOB_NAME, partitionValue);
        if (offsets.keySet().contains(key)) {
            return true;
        } else {
            logger.error("Didn't find the key {} in offset storage", key);
            return cache.containsKey(key);
        }
    }

    public Optional<Map<String, Object>> getOffset(String partitionValue) {
        Map<String, String> key = Collections.singletonMap(JenkinsSourceTask.JOB_NAME, partitionValue);
        if (offsets.get(key) != null) {
            return Optional.of(offsets.get(key));
        } else {
            logger.error("Didn't find the key {} in offset storage so trying from cache", key);
            return Optional.ofNullable(cache.get(key));
        }
    }

    public Optional<Map<String, Object>> getOffset(SourcePartition partition) {
        if (offsets.get(partition.key) != null) {
            return Optional.of(offsets.get(partition.encoded));
        } else {
            logger.error("Didn't find the key {} in offset storage so trying from cache", partition.key);
            return Optional.ofNullable(cache.get(partition.encoded));
        }
    }

    private Map<Map<String, String>, Map<String, Object>> loadAndGetOffsets(OffsetStorageReader reader, String jobUrls) {
        String[] jobUrlArray = jobUrls.split(",");

        logger.debug("Total jobs: {}. Loading offsets from Connect.", jobUrlArray.length);
        Collection<Map<String, String>> partitions = new ArrayList<>(jobUrlArray.length);
        for (String jobUrl : jobUrlArray) {
            partitions.add(Collections.singletonMap(JenkinsSourceTask.JOB_NAME, urlDecode(extractJobName(jobUrl))));
        }
        return reader.offsets(partitions);
    }
}