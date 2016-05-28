package org.aravind.oss.kafka.connector

import org.apache.kafka.connect.source.SourceTaskContext
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static com.github.dreamhead.moco.Moco.pathResource
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer
import static com.github.dreamhead.moco.Runner.runner

/**
 * @author Aravind R Yarram
 * @since <<add version>>
 */
class JenkinsSourceTaskTest extends Specification {
    @Shared
    SourceTaskContext taskContext = Mock()

    @Shared
    def mock

    JenkinsSourceTask sourceTask

    def setupSpec() {
        def server = jsonHttpServer(8181, pathResource("JenkinsSourceTaskTest-mock-server-cfg.json"))
        mock = runner(server)
        mock.start()
    }

    def cleanupSpec() {
        mock.stop()
    }

    def setup() {
        sourceTask = new JenkinsSourceTask()
        sourceTask.initialize(taskContext)
    }

    def cleanup() {
        if (sourceTask != null) sourceTask.stop()
    }

    def "Should support single job url as taskProps"() {
        given:
        def taskProps = ['job.urls': 'http://localhost:8181/job/Abdera-trunk/']
        sourceTask.start(taskProps)

        when:
        def sourceRecords = sourceTask.poll()

        then:
        sourceRecords != null
        sourceRecords.size() == 1
    }

    def "Should support multiple comma separated job urls as taskProps"() {
        given:
        def taskProps = ['job.urls': 'http://localhost:8181/job/Abdera-trunk/,http://localhost:8181/job/Accumulo-1.8/']
        sourceTask.start(taskProps)

        when:
        def sourceRecords = sourceTask.poll()

        then:
        sourceRecords != null
        sourceRecords.size() == 2
    }

    @Ignore
    //Negative tests
    def "Wrong URL should continue without any errors"() {
        given:
        def taskProps = ['job.urls': 'http://wrong.host.name:8181/job/Abdera-trunk/']
        sourceTask.start(taskProps)

        when:
        def sourceRecords = sourceTask.poll()
        sourceTask.stop()

        then:
        sourceRecords != null
    }
}
