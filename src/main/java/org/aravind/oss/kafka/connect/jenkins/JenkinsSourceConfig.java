package org.aravind.oss.kafka.connect.jenkins;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Convenient class to hold configuration properties of the JenkinsSourceConnector
 *
 * @author Aravind R Yarram
 * @since 0.5
 */
public class JenkinsSourceConfig extends AbstractConfig {

    public static final String JENKINS_BASE_URL_CONFIG = "jenkins.base.url";
    private static final String JENKINS_BASE_URL_DOC = "This is the URL of the home page of your Jenkins installation. "
            + "For e.g. https://builds.apache.org/ is the base url of the Apache Jenkins public instance. " +
            "In some installations, a context/prefix might have been specified (using the --prefix; For e.g. --prefix=/jenkins). " +
            "If so then the url should include the prefix as well. ";

    public static final String JOBS_RESOURCE_PATH_CONFIG = "jenkins.jobs.resource.path";
    private static final String JOBS_RESOURCE_PATH_DEFAULT = "/api/json";
    private static final String JOBS_RESOURCE_PATH_DOC = "This is the REST resource path to retrieve all jobs defined in the Jenkins instance. " +
            "This is an optional configuration property. If not specified the default \"/api/json\" will be used.";

    public static final String JENKINS_USERNAME_CONFIG = "jenkins.username";
    private static final String JENKINS_USERNAME_CONFIG_DOC = "Username to use when connecting to protected Jenkins.";

    public static final String JENKINS_PASSWORD_OR_API_TOKEN_CONFIG = "jenkins.password.or.api.token";
    private static final String JENKINS_PASSWORD_OR_API_TOKEN_DOC = "Password (or API Token) to use when connecting to protected Jenkins.";

    public static final String TOPIC_CONFIG = "topic";
    public static final String TOPIC_CONFIG_DOC = "This is the name of the Kafka Topic to which the source records containing Jenkins Build details are written to.";
    public static final String TOPIC_CONFIG_DEFAULT = "jenkins.connector.topic";

    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(JENKINS_BASE_URL_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, JENKINS_BASE_URL_DOC)
                .define(JENKINS_USERNAME_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JENKINS_USERNAME_CONFIG_DOC)
                .define(JENKINS_PASSWORD_OR_API_TOKEN_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JENKINS_PASSWORD_OR_API_TOKEN_DOC)
                .define(JOBS_RESOURCE_PATH_CONFIG, ConfigDef.Type.STRING, JOBS_RESOURCE_PATH_DEFAULT, ConfigDef.Importance.LOW, JOBS_RESOURCE_PATH_DOC)
                .define(TOPIC_CONFIG, ConfigDef.Type.STRING, TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TOPIC_CONFIG_DOC);
    }

    public JenkinsSourceConfig(Map<String, String> originals) {
        super(DEFS, originals);
    }

    public URL getJenkinsUrl() {
        try {
            return new URL(getString(JENKINS_BASE_URL_CONFIG));
        } catch (MalformedURLException e) {
            throw new ConfigException("Couldn't create the URL from " + getString(JENKINS_BASE_URL_CONFIG), e);
        }
    }

    public String getUsername() {
        return getString(JENKINS_USERNAME_CONFIG);
    }

    public String getPasswordOrApiToken() {
        return getString(JENKINS_PASSWORD_OR_API_TOKEN_CONFIG);
    }

    public boolean isProtected() {
        return getString(JENKINS_USERNAME_CONFIG) != null && !getString(JENKINS_USERNAME_CONFIG).isEmpty();
    }

    public URL getJobsResource() {
        try {
            return new URL(getString(JENKINS_BASE_URL_CONFIG) + JOBS_RESOURCE_PATH_DEFAULT);
        } catch (MalformedURLException e) {
            throw new ConfigException("Couldn't create the URL from " + getString(JENKINS_BASE_URL_CONFIG), e);
        }
    }
}
