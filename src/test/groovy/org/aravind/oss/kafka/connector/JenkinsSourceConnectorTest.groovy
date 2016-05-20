package org.aravind.oss.kafka.connector

import org.apache.kafka.connect.connector.ConnectorContext
import org.apache.kafka.connect.errors.ConnectException
import spock.lang.Shared
import spock.lang.Specification

import static com.github.dreamhead.moco.Moco.pathResource
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer
import static com.github.dreamhead.moco.Runner.runner

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class JenkinsSourceConnectorTest extends Specification {
    @Shared
    ConnectorContext context = Mock()

    Map sourceProps = [:]
    JenkinsSourceConnector connector

    def setup() {
        connector = new JenkinsSourceConnector();
        connector.initialize(context)
    }

    def cleanup() {
        if (connector != null) connector.stop()
    }
    //Error scenarios

    def "Config - Wrong jenkins url should throw exception"() {
        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'https://wrong.domain.name')
        connector.start(sourceProps)

        then:
        thrown(ConnectException)
    }

    //Happy scenarios

    def "Config - Correct config should start the connector"() {
        given:
        def server = jsonHttpServer(8181, pathResource("jenkins-mock-server-cfg.json"))
        def mock = runner(server)
        mock.start()

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:8181')
        connector.start(sourceProps)

        then:
        noExceptionThrown()

        mock.stop()
    }

    def "Partitioning - Single job"() {
        given:
        def server = jsonHttpServer(9292, pathResource("jenkins-mock-server-single-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        def MAX_TASKS_IS_ONE = 1

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9292')
        connector.start(sourceProps)

        def taskCfgs = connector.taskConfigs(MAX_TASKS_IS_ONE)

        then:
        taskCfgs.size() == MAX_TASKS_IS_ONE

        Map<String, String> cfg = taskCfgs.get(0)
        cfg != null
        cfg[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/'

        mock.stop()
    }

    def "Partitioning - Multiple jobs"() {
        given:
        def server = jsonHttpServer(9293, pathResource("jenkins-mock-server-three-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        def MAX_TASKS_IS_ONE = 1

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9293')
        connector.start(sourceProps)

        def taskCfgs = connector.taskConfigs(MAX_TASKS_IS_ONE)

        then:
        taskCfgs.size() == MAX_TASKS_IS_ONE

        Map<String, String> cfg = taskCfgs.get(0)
        cfg != null
        cfg[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/,https://builds.apache.org/job/Accumulo-1.8/,https://builds.apache.org/job/Allura/'

        mock.stop()
    }

    def "Partitioning - Multiple jobs with 2 MAX_TASKS"() {
        given:
        def server = jsonHttpServer(9293, pathResource("jenkins-mock-server-three-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        def MAX_TASKS_IS_TWO = 2

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9293')
        connector.start(sourceProps)

        def taskCfgs = connector.taskConfigs(MAX_TASKS_IS_TWO)

        then:
        taskCfgs.size() == MAX_TASKS_IS_TWO

        Map<String, String> cfg = taskCfgs.get(0)
        cfg != null
        cfg[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/,https://builds.apache.org/job/Accumulo-1.8/'

        Map<String, String> cfg1 = taskCfgs.get(1)
        cfg1 != null
        cfg1[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Allura/'

        mock.stop()
    }

    def "Partitioning - Multiple jobs with 3 MAX_TASKS"() {
        given:
        def server = jsonHttpServer(9293, pathResource("jenkins-mock-server-three-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        def MAX_TASKS_IS_THREE = 3

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9293')
        connector.start(sourceProps)

        def taskCfgs = connector.taskConfigs(MAX_TASKS_IS_THREE)

        then:
        taskCfgs.size() == MAX_TASKS_IS_THREE

        Map<String, String> cfg = taskCfgs.get(0)
        cfg != null
        cfg[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/'

        Map<String, String> cfg1 = taskCfgs.get(1)
        cfg1 != null
        cfg1[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Accumulo-1.8/'

        Map<String, String> cfg2 = taskCfgs.get(2)
        cfg2 != null
        cfg2[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Allura/'

        mock.stop()
    }

    def "Partitioning - Less jobs and more MAX_TASKS"() {
        given:
        def server = jsonHttpServer(9294, pathResource("jenkins-mock-server-single-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        def MAX_TASKS_IS_THREE = 3

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9294')
        connector.start(sourceProps)

        def taskCfgs = connector.taskConfigs(MAX_TASKS_IS_THREE)

        then:
        taskCfgs.size() == 1

        Map<String, String> cfg = taskCfgs.get(0)
        cfg != null
        cfg[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/'

        mock.stop()
    }
}
