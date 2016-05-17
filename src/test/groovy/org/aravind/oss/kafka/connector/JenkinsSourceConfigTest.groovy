package org.aravind.oss.kafka.connector

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
        cfg = ['jenkins.base.url'             : 'https://builds.apache.org',
               'jenkins.username'             : 'myuser',
               'jenkins.password.or.api.token': 'mypassword',
               'jenkins.jobs.resource.path'   : '/api/json']
    }

    //Error scenarios

    def "Missing 'jenkins.base.url' property should throw exception"() {
        when:
        def props = [:]
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        thrown(ConfigException)
    }

    def "Default value of '/api/json' is chosen when 'jenkins.jobs.resource.path' property is not specified"() {
        when:
        def props = ['jenkins.base.url': 'https://builds.apache.org']
        def jenkinsCfg = new JenkinsSourceConfig(props)

        then:
        jenkinsCfg.getJobsResource() == new URL('https://builds.apache.org/api/json')
    }

    def "Not starting the 'jenkins.jobs.resource.path' property value with '/' should throw an exception"() {
        when:
        def props = ['client.jobs.resource.path': 'api/json']
        def jenkinsCfg = new JenkinsSourceConfig(props)

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
}