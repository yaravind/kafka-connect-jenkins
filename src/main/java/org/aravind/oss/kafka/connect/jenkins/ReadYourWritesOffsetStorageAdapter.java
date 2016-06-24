package org.aravind.oss.kafka.connect.jenkins;

import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.aravind.oss.kafka.connect.lib.SourceOffset;
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
    private Map<Map<String, ?>, Map<String, Object>> cache = new HashMap<>();

    //task level offsets from StorageReader
    private Map<Map<String, String>, Map<String, Object>> offsets;

    private static final Logger logger = LoggerFactory.getLogger(ReadYourWritesOffsetStorageAdapter.class);

    public ReadYourWritesOffsetStorageAdapter(OffsetStorageReader reader, String jobUrls, Partitions ps) {
        storageReader = reader;
        partitions = ps;
        offsets = loadAndGetOffsets(storageReader, jobUrls);
        logger.debug("Loaded offsets: {}", offsets);
    }

    public void cache(SourcePartition p, SourceOffset o) {
        Map<String, ?> key = p.encoded;
        Map<String, Object> value = o.encoded;

        cache.put(key, value);
    }

    public boolean containsPartition(SourcePartition partition) {
        if (offsets.keySet().contains(partition.encoded)) {
            return true;
        } else {
            logger.trace("Didn't find the key {} in offset storage", partition.encoded);
            return cache.containsKey(partition.encoded);
        }
    }

    public Optional<SourceOffset> getOffset(SourcePartition partition) {
        if (offsets.get(partition.encoded) != null) {
            return Optional.of(SourceOffset.decode(offsets.get(partition.encoded)));
        } else {
            logger.trace("Didn't find the key {} in offset storage so trying from cache", partition.encoded);
            return Optional.ofNullable(SourceOffset.decode(cache.get(partition.encoded)));
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