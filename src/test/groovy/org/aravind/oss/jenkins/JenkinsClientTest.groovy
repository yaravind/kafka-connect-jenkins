package org.aravind.oss.jenkins

import com.github.dreamhead.moco.HttpsCertificate
import com.github.dreamhead.moco.Runner
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import static com.github.dreamhead.moco.Moco.httpsServer
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer
import static com.github.dreamhead.moco.Runner.runner
import static com.github.dreamhead.moco.Moco.pathResource
import static org.aravind.oss.SSLClasspathTrustStoreLoader.setTrustStore

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class JenkinsClientTest extends Specification {

    @Shared
    Runner mock

    @Shared
    Runner httpsMock

    def setupSpec() {
        def server = jsonHttpServer(9191, pathResource("jenkins-mock-server-cfg.json"))
        mock = runner(server)
        mock.start()

        //trust the self-signed cert.jks
        setTrustStore("/cert.jks", "mocohttps")

        def httpsServer = httpsServer(9443, HttpsCertificate.certificate(pathResource("cert.jks"), "mocohttps", "mocohttps"))
        httpsMock = runner(httpsServer)
        httpsMock.start()
    }

    def "Supports Jenkins without authentication"() {
        when:
        def url = new URL("http://localhost:9191/")
        def jenkins = new JenkinsClient(url)

        then:
        noExceptionThrown()
    }

    def "Supports GET"() {
        given:
        def url = new URL("http://localhost:9191/api/json")
        def jenkins = new JenkinsClient(url)

        when:
        def allJobs = jenkins.get()

        then:
        allJobs != null
        allJobs.isPresent() == true
    }

    @Ignore("Figure out Basic Auth with Moco")
    def "Supports Jenkins with authentication"() {
        when:
        def url = new URL("http://localhost:9191")
        def jenkins = new JenkinsClient(url)

        then:
        noExceptionThrown()
    }

    def "Supports Jenkins with SSL"() {
        when:
        def url = new URL("https://localhost:9443")
        def jenkins = new JenkinsClient(url)

        then:
        noExceptionThrown()
    }

    def cleanupSpec() {
        if (mock != null) mock.stop()
        if (httpsMock != null) httpsMock.stop();
    }
}
