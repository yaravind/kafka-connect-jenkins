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

    @Shared
    def Map<String, String> keys = ['jobName': 'Abdera-Trunk']
    @Shared
    def Map<String, Object> values = ['buildNumber': '72']

    def setupSpec() {
        offsets.put(keys, values)
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

    def "getOffset"() {
        when:
        Optional<Map<String, Object>> result = adapter.getOffset('Abdera-Trunk')

        then:
        result.get() == ['buildNumber': '72']
    }

    def "Negative - getOffset"() {
        when:
        Optional<Map<String, Object>> result = adapter.getOffset('MyJob-Trunk')

        then:
        result.isPresent() == false
    }
}
