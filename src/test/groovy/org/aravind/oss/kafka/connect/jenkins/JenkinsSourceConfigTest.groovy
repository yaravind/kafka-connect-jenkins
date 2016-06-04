package org.aravind.oss.kafka.connect.jenkins

import org.apache.kafka.common.config.ConfigException
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
class JenkinsSourceConfigTest extends Specification {

    @Shared
    def Map cfg

    def setupSpec() {
        cfg = ['jenkins.base.url'                  : 'https://builds.apache.org',
               'jenkins.username'                  : 'myuser',
               'jenkins.password.or.api.token'     : 'mypassword',
               'jenkins.connection.timeoutInMillis': '1000',
               'jenkins.read.timeoutInMillis'      : '5000',
               'jenkins.jobs.resource.path'        : '/api/json']
    }

    //Error scenarios

    def "Missing 'jenkins.base.url' property should throw exception"() {
        when:
        def props = [:]
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        thrown(ConfigException)
    }

    def "Not starting the 'jenkins.jobs.resource.path' property value with '/' should throw an exception"() {
        when:
        def props = ['client.jobs.resource.path': 'api/json']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        thrown(ConfigException)
    }

    def "GetJenkinsUrl should throw Exception with invalid URL"() {
        given: "Given invalid url"
        def props = ['jenkins.base.url': 'invalid url']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        when:
        jenkinsCfg.getJenkinsUrl()

        then:
        thrown(ConfigException)
    }

    def "GetJobsResource should throw Exception with invalid URL"() {
        given: "Given invalid url"
        def props = ['jenkins.base.url': 'invalid url']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        when:
        jenkinsCfg.getJobsResource()

        then:
        thrown(ConfigException)
    }

    //Happy scenarios

    def "'jenkins.base.url' is the only REQUIRED property"() {
        when:
        def props = ['jenkins.base.url': 'https://builds.apache.org']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        notThrown(ConfigException)
    }

    //Happy scenarios - Defaults

    def "Defaults - '/api/json' is chosen when 'jenkins.jobs.resource.path' property is not specified"() {
        when:
        def props = ['jenkins.base.url': 'https://builds.apache.org']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        jenkinsCfg.getJobsResource() == new URL('https://builds.apache.org/api/json')
    }

    def "Defaults - '500' millis is chosen when 'jenkins.connection.timeoutInMillis' property is not specified"() {
        when:
        def props = ['jenkins.base.url': 'https://builds.apache.org']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        jenkinsCfg.getJenkinsConnTimeout() == 500
    }

    def "Defaults - '3000' is chosen when 'jenkins.read.timeoutInMillis' property is not specified"() {
        when:
        def props = ['jenkins.base.url': 'https://builds.apache.org']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        jenkinsCfg.getJenkinsReadTimeout() == 3000
    }

    //Happy scenarios - Helper methods

    def "GetJenkinsUrl"() {
        when:
        def jenkinsCfg = new JenkinsSourceConfig(cfg)

        then: "an URL object is returned"
        jenkinsCfg.getJenkinsUrl() == new URL('https://builds.apache.org')
    }

    def "GetUsername"() {
        when:
        def jenkinsCfg = new JenkinsSourceConfig(cfg)

        then:
        jenkinsCfg.getUsername() == 'myuser'
    }

    def "GetPasswordOrApiToken"() {
        when:
        def jenkinsCfg = new JenkinsSourceConfig(cfg)

        then:
        jenkinsCfg.getPasswordOrApiToken() == 'mypassword'
    }

    def "IsProtected"() {
        when:
        def jenkinsCfg = new JenkinsSourceConfig(cfg)

        then:
        jenkinsCfg.isProtected() == true
    }

    def "GetJobsResource"() {
        when:
        def jenkinsCfg = new JenkinsSourceConfig(cfg)

        then:
        jenkinsCfg.getJobsResource() == new URL('https://builds.apache.org/api/json')
    }

    def "GetJenkinsConnTimeout"() {
        when:
        def jenkinsCfg = new JenkinsSourceConfig(cfg)

        then:
        jenkinsCfg.getJenkinsConnTimeout() == 1000
    }

    def "GetJenkinsReadTimeout"() {
        when:
        def jenkinsCfg = new JenkinsSourceConfig(cfg)

        then:
        jenkinsCfg.getJenkinsReadTimeout() == 5000
    }
}