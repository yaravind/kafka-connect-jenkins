package org.aravind.oss.kafka.connect.jenkins

import org.apache.kafka.connect.storage.OffsetStorageReader
import org.junit.Before
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class ReadYourWritesOffsetStorageAdapterTest extends Specification {

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
        adapter = new ReadYourWritesOffsetStorageAdapter(storageReader, 'Abdera-Trunk')
    }

    def "containsPartition"() {
        when:
        Optional<Map<String, Object>> result = adapter.getOffset('Abdera-Trunk')

        then:
        result.isPresent() == true
        result.get() == ['buildNumber': '72']
    }

    def "getOffset - kay exists in offsets read from OffsetStorageReader"() {
        when:
        Optional<Map<String, Object>> result = adapter.getOffset('Abdera-Trunk')

        then:
        result.get() == ['buildNumber': '72']
    }

    def "getOffset - if key doesn't exist in offsets then read from cache"() {
        given: "the current offsets aren't yet flushed"
        Optional<Map<String, Object>> result = adapter.getOffset('NonExistingJob-Trunk')
        result.isPresent() == false

        when: "we create a new SourceRecord for the new build"
        String partitionValue = 'NonExistingJob-Trunk'
        Object offsetValue = '272'

        adapter.cache(partitionValue, offsetValue)

        then:
        adapter.containsPartition('NonExistingJob-Trunk') == true
        adapter.getOffset('NonExistingJob-Trunk').isPresent() == true
    }

    def "getOffset should return no result for non existing partitions"() {
        when:
        Optional<Map<String, Object>> result = adapter.getOffset('NonExistingJob1-Trunk')

        then:
        result.isPresent() == false
    }
}
