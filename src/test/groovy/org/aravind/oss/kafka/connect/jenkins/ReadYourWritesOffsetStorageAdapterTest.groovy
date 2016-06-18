package org.aravind.oss.kafka.connect.jenkins

import org.apache.kafka.connect.storage.OffsetStorageReader
import org.aravind.oss.kafka.connect.lib.SourceOffset
import org.aravind.oss.kafka.connect.lib.Partitions
import org.aravind.oss.kafka.connect.lib.SourcePartition
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class ReadYourWritesOffsetStorageAdapterTest extends Specification {

    @Shared
    def Partitions partitions = new Partitions(JenkinsSourceTask.JOB_NAME)

    @Shared
    def OffsetStorageReader storageReader = Mock()

    @Shared
    def ReadYourWritesOffsetStorageAdapter adapter

    @Shared
    def Map<Map<String, String>, Map<String, Object>> offsets = [:]

    def setupSpec() {
        offsets.put(['jobName': 'Abdera-Trunk'], ['buildNumber': '72'])
        offsets.put(['jobName': 'Hadoop-Trunk'], ['buildNumber': '14'])

        storageReader.offsets(_) >> offsets
        adapter = new ReadYourWritesOffsetStorageAdapter(storageReader, 'Abdera-Trunk', partitions)
    }

    def "containsPartition"() {
        when:
        def result = adapter.containsPartition(partitions.make('Abdera-Trunk'))

        then:
        result == true
    }

    def "getOffset - kay exists in offsets read from OffsetStorageReader"() {
        when:
        Optional<SourceOffset> result = adapter.getOffset(partitions.make('Abdera-Trunk'))

        then:
        result.get().encoded == ['buildNumber': '72']
    }

    def "getOffset - if key doesn't exist in offsets then read from cache"() {
        given: "the current offsets aren't yet flushed"
        SourcePartition p = partitions.make('NonExistingJob-Trunk')
        Optional<SourceOffset> result = adapter.getOffset(p)
        result.isPresent() == false

        when: "we create a new SourceRecord for the new build"
        String partitionValue = 'NonExistingJob-Trunk'
        Object offsetValue = '272'

        adapter.cache(p, SourceOffset.make(JenkinsSourceTask.BUILD_NUMBER, offsetValue))

        then:
        adapter.containsPartition(p) == true
        adapter.getOffset(p).isPresent() == true
    }

    def "getOffset - should return no result for non existing partitions"() {
        when:
        Optional<SourceOffset> result = adapter.getOffset(partitions.make('NonExistingJob1-Trunk'))

        then:
        result.isPresent() == false
    }
}
