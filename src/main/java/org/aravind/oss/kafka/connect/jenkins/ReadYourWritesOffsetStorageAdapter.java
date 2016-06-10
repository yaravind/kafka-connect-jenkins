package org.aravind.oss.kafka.connect.jenkins;

import org.apache.kafka.connect.storage.OffsetStorageReader;

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

    //task level cache
    private Map<String, Object> lookup = new HashMap<>();

    //Offsets from StorageReader
    private Map<Map<String, String>, Map<String, Object>> offsets;

    public ReadYourWritesOffsetStorageAdapter(OffsetStorageReader reader, String jobUrls) {
        storageReader = reader;
        offsets = loadAndGetOffsets(storageReader, jobUrls);
        Util.logger.debug("Total loaded offsets: {}", offsets.size());
        Util.logger.error("Loaded offsets: {}", offsets);
    }


    public boolean containsPartition(String partitionValue) {
        return offsets.keySet().contains(Collections.singletonMap(JenkinsSourceTask.JOB_NAME, partitionValue));
    }

    public Optional<Map<String, Object>> getOffset(String partitionValue) {
        return Optional.ofNullable(offsets.get(Collections.singletonMap(JenkinsSourceTask.JOB_NAME, partitionValue)));
    }

    private Map<Map<String, String>, Map<String, Object>> loadAndGetOffsets(OffsetStorageReader reader, String jobUrls) {
        String[] jobUrlArray = jobUrls.split(",");

        Util.logger.debug("Total jobs: {}. Loading offsets from Connect.", jobUrlArray.length);
        Collection<Map<String, String>> partitions = new ArrayList<>(jobUrlArray.length);
        for (String jobUrl : jobUrlArray) {
            partitions.add(Collections.singletonMap(JenkinsSourceTask.JOB_NAME, urlDecode(extractJobName(jobUrl))));
        }
        return reader.offsets(partitions);
    }


}