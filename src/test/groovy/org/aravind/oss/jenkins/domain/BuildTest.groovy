package org.aravind.oss.jenkins.domain

import spock.lang.Shared
import spock.lang.Specification

import static com.github.dreamhead.moco.Moco.pathResource
import static com.github.dreamhead.moco.MocoJsonRunner.jsonHttpServer
import static com.github.dreamhead.moco.Runner.runner

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class BuildTest extends Specification {
    @Shared
    def mock

    def setupSpec() {
        def server = jsonHttpServer(1081, pathResource("BuildTest-mock-server-cfg.json"))
        mock = runner(server)
        mock.start()
    }

    def cleanupSpec() {
        mock.stop()
    }

    def "getDetails"() {
        given:
        def build = new Build()
        build.setNumber(2546)
        build.setUrl('http://localhost:1081/job/Abdera-trunk/2546/')

        when:
        Optional<String> details = build.getDetails()

        then:
        build.getBuildDetailsResource() == 'http://localhost:1081/job/Abdera-trunk/2546/api/json'
        details.isPresent() == true
    }

    def "getDetails - Error condition"() {
        given:
        def build = new Build()
        build.setNumber(2546)
        build.setUrl('http://localhost:1081/job/Abdera-trunk/0000/')

        when:
        Optional<String> details = build.getDetails()

        then:
        build.getBuildDetailsResource() == 'http://localhost:1081/job/Abdera-trunk/0000/api/json'
        details.isPresent() == false
    }
}
