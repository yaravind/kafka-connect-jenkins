package org.aravind.oss.jenkins

import com.github.dreamhead.moco.HttpsCertificate
import com.github.dreamhead.moco.Runner
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import static com.github.dreamhead.moco.Moco.httpServer
import static com.github.dreamhead.moco.Moco.httpsServer
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer
import static com.github.dreamhead.moco.Runner.runner

import static com.github.dreamhead.moco.Moco.pathResource

//import com.github.dreamhead.moco.HttpsCertificate

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class JenkinsInstanceTest extends Specification {

    @Shared
    Runner mock

    def setupSpec() {
        def server = jsonHttpServer(9191, pathResource("jenkins-mock-server-cfg.json"))
        def httpsServer = httpsServer(9443, HttpsCertificate.certificate(pathResource("cert.jks"), "mocohttps", "mocohttps"))
        mock = runner(server)
        mock.start()
    }

    def "Supports Jenkins without authentication"() {
        when:
        def url = new URL("http://localhost:9191/")
        def jenkins = new JenkinsInstance(url)

        then:
        noExceptionThrown()
    }

    def "Supports GET"() {
        given:
        def url = new URL("http://localhost:9191/api/json")
        def jenkins = new JenkinsInstance(url)

        when:
        def allJobs = jenkins.get(256)

        then:
        allJobs != null
        allJobs.isPresent() == true
    }

    @Ignore("Figure out Basic Auth with Moco")
    def "Supports Jenkins with authentication"() {
        when:
        def url = new URL("http://localhost:9191")
        def jenkins = new JenkinsInstance(url)

        then:
        noExceptionThrown()
    }

    @Ignore("Fix me")
    def "Supports Jenkins with SSL"() {
        when:
        def url = new URL("https://localhost:9443")
        def jenkins = new JenkinsInstance(url)

        then:
        noExceptionThrown()
    }

    def cleanupSpec() {
        if (mock != null) mock.stop()
    }
}
