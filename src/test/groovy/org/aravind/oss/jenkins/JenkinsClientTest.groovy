package org.aravind.oss.jenkins

import com.github.dreamhead.moco.HttpsCertificate
import com.github.dreamhead.moco.Runner
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static com.github.dreamhead.moco.Moco.and
import static com.github.dreamhead.moco.Moco.by
import static com.github.dreamhead.moco.Moco.exist
import static com.github.dreamhead.moco.Moco.header
import static com.github.dreamhead.moco.Moco.httpsServer
import static com.github.dreamhead.moco.Moco.log
import static com.github.dreamhead.moco.Moco.uri
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer
import static com.github.dreamhead.moco.Runner.runner
import static com.github.dreamhead.moco.Moco.pathResource

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class JenkinsClientTest extends Specification {

    public static final int CONN_TIMEOUT = 100
    public static final int READ_TIMEOUT = 500
    @Shared
    Runner mock

    @Shared
    Runner httpsMock

    def setupSpec() {
        def server = jsonHttpServer(9191, pathResource("jenkins-mock-server-cfg.json"))
        mock = runner(server)
        mock.start()

        def httpsServer = httpsServer(9443, HttpsCertificate.certificate(pathResource("keystore.jks"), "password", "password"))
        httpsMock = runner(httpsServer)
        httpsMock.start()
    }

    //Error scenarios

    def "Wrong authentication credentials 'username' should return empty response"() {
        given: "Wrong username and correct password"
        def server = jsonHttpServer(9495, pathResource("jenkins-mock-server-with-authuentication-cfg.json"))
        mock = runner(server)
        mock.start()

        def userName = "wronguser"
        def password = "password"

        when: "A request is submitted"
        def url = new URL("http://localhost:9495/api/json")
        def jenkins = new JenkinsClient(url, userName, password, CONN_TIMEOUT, READ_TIMEOUT)
        def response = jenkins.get()

        then: "response should be empty and an exception is swallowed but logged"
        response.isPresent() == false
        noExceptionThrown()

        mock.stop()
    }

    def "Wrong authentication credentials 'passwordOrApiToken' should return empty response"() {
        given: "Wrong username and correct password"
        def server = jsonHttpServer(9495, pathResource("jenkins-mock-server-with-authuentication-cfg.json"))
        mock = runner(server)
        mock.start()

        def userName = "user"
        def password = "wrongpassword"

        when: "A request is submitted"
        def url = new URL("http://localhost:9495/api/json")
        def jenkins = new JenkinsClient(url, userName, password, CONN_TIMEOUT, READ_TIMEOUT)
        def response = jenkins.get()

        then: "response should be empty and an exception is swallowed but logged"
        response.isPresent() == false
        noExceptionThrown()

        mock.stop()
    }

    //Happy scenarios

    def "Supports Jenkins without authentication"() {
        when:
        def url = new URL("http://localhost:9191/")
        def jenkins = new JenkinsClient(url, CONN_TIMEOUT, READ_TIMEOUT)
        def response = jenkins.get()

        then:
        noExceptionThrown()
    }

    def "Supports GET"() {
        given:
        def url = new URL("http://localhost:9191/api/json")
        def jenkins = new JenkinsClient(url, CONN_TIMEOUT, READ_TIMEOUT)

        when:
        def allJobs = jenkins.get()

        then:
        allJobs != null
        allJobs.isPresent() == true
    }

    def "Supports Jenkins with Basic Authentication"() {
        given: "Correct username and password"
        def server = jsonHttpServer(9494, pathResource("jenkins-mock-server-with-authuentication-cfg.json"))
        mock = runner(server)
        mock.start()

        def userName = "user"
        def password = "password"

        when: "A request is submitted"
        def url = new URL("http://localhost:9494/api/json")
        def jenkins = new JenkinsClient(url, userName, password, CONN_TIMEOUT, READ_TIMEOUT)
        def response = jenkins.get()

        then: "A valid response should be present"
        response.isPresent() == true

        mock.stop()
    }

    def "Supports Jenkins with SSL"() {
        when:
        def url = new URL("https://localhost:9443")
        def jenkins = new JenkinsClient(url, CONN_TIMEOUT, READ_TIMEOUT)
        def response = jenkins.get()

        then:
        noExceptionThrown()
    }

    @Ignore("Ignore until we figure out how to mock https + basic auth with java api")
    def "Supports Jenkins with SSL and Basic Authentication"() {
        given: "Correct username and password"

        def httpsServer = httpsServer(10443, HttpsCertificate.certificate(pathResource("keystore.jks"), "password", "password"))
        httpsServer.request(
                exist(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
        )
                .response(pathResource("single-job.json"))
        mock = runner(httpsServer)
        mock.start()

        def userName = "user"
        def password = "password"

        when:
        def url = new URL("https://localhost:10443")
        def jenkins = new JenkinsClient(url, userName, password, CONN_TIMEOUT, READ_TIMEOUT)
        def response = jenkins.get()

        then: "A valid response should be present"
        response.isPresent() == true

        mock.stop()
    }

    def cleanupSpec() {
        if (mock != null) mock.stop()
        if (httpsMock != null) httpsMock.stop();
    }
}
