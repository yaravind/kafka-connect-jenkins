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

    //API Contracts

    def "taskClass()"() {
        when:
        Class taskClass = connector.taskClass()

        then:
        taskClass == JenkinsSourceTask.class
    }

    //Error scenarios

    def "Config - Wrong jenkins url should throw exception"() {
        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'https://wrong.domain.name')
        connector.start(sourceProps)

        then:
        thrown(ConnectException)
    }

    //Happy scenarios - Config

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

    def "Config - The 'topic' property should be forwarded to each task"() {
        given:
        def server = jsonHttpServer(9295, pathResource("jenkins-mock-server-single-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        def MAX_TASKS_IS_THREE = 3

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9295')
        sourceProps.put(JenkinsSourceConfig.TOPIC_CONFIG, 'test.topic.name')
        connector.start(sourceProps)

        def taskCfgs = connector.taskConfigs(MAX_TASKS_IS_THREE)

        then:
        Map<String, String> taskProps = taskCfgs.get(0)
        taskProps != null
        taskProps[JenkinsSourceConfig.TOPIC_CONFIG] == 'test.topic.name'

        mock.stop()
    }

    //Happy scenarios - Defaults

    def "Defaults - 'jenkins.jobs.resource.path' config is optional"() {
        given:
        def server = jsonHttpServer(9296, pathResource("jenkins-mock-server-single-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9296')
        connector.start(sourceProps)

        then: "Default value is used"
        connector.getJenkinsCfg().getString(JenkinsSourceConfig.JOBS_RESOURCE_PATH_CONFIG) == '/api/json'
    }

    def "Defaults - 'topic' config is optional"() {
        given:
        def server = jsonHttpServer(9297, pathResource("jenkins-mock-server-single-job-cfg.json"))
        def mock = runner(server)
        mock.start()

        when:
        sourceProps.put(JenkinsSourceConfig.JENKINS_BASE_URL_CONFIG, 'http://localhost:9297')
        connector.start(sourceProps)

        then: "Default value is used"
        connector.getJenkinsCfg().getString(JenkinsSourceConfig.TOPIC_CONFIG) == 'jenkins.connector.topic'
    }

    //Happy scenarios - Partitioning

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

        Map<String, String> taspProps = taskCfgs.get(0)
        taspProps != null
        taspProps[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/'

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

        Map<String, String> taskProps = taskCfgs.get(0)
        taskProps != null
        taskProps[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/,https://builds.apache.org/job/Accumulo-1.8/,https://builds.apache.org/job/Allura/'

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

        Map<String, String> task1Props = taskCfgs.get(0)
        task1Props != null
        task1Props[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/,https://builds.apache.org/job/Accumulo-1.8/'

        Map<String, String> task2Props = taskCfgs.get(1)
        task2Props != null
        task2Props[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Allura/'

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

        Map<String, String> task1Props = taskCfgs.get(0)
        task1Props != null
        task1Props[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/'

        Map<String, String> task2Props = taskCfgs.get(1)
        task2Props != null
        task2Props[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Accumulo-1.8/'

        Map<String, String> task3Props = taskCfgs.get(2)
        task3Props != null
        task3Props[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Allura/'

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

        Map<String, String> taskProps = taskCfgs.get(0)
        taskProps != null
        taskProps[JenkinsSourceTask.JOB_URLS] == 'https://builds.apache.org/job/Abdera-trunk/'

        mock.stop()
    }
}
