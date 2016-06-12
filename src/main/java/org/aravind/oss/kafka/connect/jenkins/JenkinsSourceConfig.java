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
    public static final String JENKINS_BASE_URL_DISPLAY = "Jenkins server url.";
    private static final String JENKINS_BASE_URL_DOC = "This is the URL of the home page of your Jenkins installation. "
            + "For e.g. https://builds.apache.org/ is the base url of the Apache Jenkins public instance. " +
            "In some installations, a context/prefix might have been specified (using the --prefix; For e.g. --prefix=/jenkins). " +
            "If so then the url should include the prefix as well. ";

    public static final String JOBS_RESOURCE_PATH_CONFIG = "jenkins.jobs.resource.path";
    private static final String JOBS_RESOURCE_PATH_DEFAULT = "/api/json";
    private static final String JOBS_RESOURCE_PATH_DISPLAY = "Path to API. Default (/api/json)";
    private static final String JOBS_RESOURCE_PATH_DOC = "This is the REST resource path to retrieve all jobs defined in the Jenkins instance. " +
            "This is an optional configuration property. If not specified the default \"/api/json\" will be used.";

    public static final String JENKINS_USERNAME_CONFIG = "jenkins.username";
    public static final String JENKINS_USERNAME_DISPLAY = "Jenkins Username.";
    private static final String JENKINS_USERNAME_CONFIG_DOC = "Username to use when connecting to protected Jenkins.";

    public static final String JENKINS_PASSWORD_OR_API_TOKEN_CONFIG = "jenkins.password.or.api.token";
    public static final String JENKINS_PASSWORD_OR_API_TOKEN_DISPLAY = "Password (or API Token)";
    private static final String JENKINS_PASSWORD_OR_API_TOKEN_DOC = "Password (or API Token) to use when connecting to protected Jenkins.";

    public static final String JENKINS_CONN_TIMEOUT_CONFIG = "jenkins.connection.timeoutInMillis";
    public static final String JENKINS_CONN_TIMEOUT_DISPLAY = "Connection timeout in milliseconds.";
    public static final int JENKINS_CONN_TIMEOUT_DEFAULT = 500;
    public static final String JENKINS_CONN_TIMEOUT_DOC = "Connection timeout in milliseconds. " +
            "This denotes the time elapsed before the connection established or Server responded to connection request.";

    public static final String JENKINS_READ_TIMEOUT_CONFIG = "jenkins.read.timeoutInMillis";
    public static final String JENKINS_READ_TIMEOUT_DISPLAY = "Response read timeout in milliseconds.";
    public static final int JENKINS_READ_TIMEOUT_DEFAULT = 3000;
    public static final String JENKINS_READ_TIMEOUT_DOC = "Response read timeout in milliseconds. After establishing the connection, " +
            "the client socket waits for response after sending the request. " +
            "This is the elapsed time since the client has sent request to the server before server responds.";

    public static final String TOPIC_CONFIG = "topic";
    public static final String TOPIC_DISPLAY = "Topic to persist build events.";
    public static final String TOPIC_CONFIG_DOC = "This is the name of the Kafka Topic to which the source records containing Jenkins Build details are written to.";
    public static final String TOPIC_CONFIG_DEFAULT = "jenkins.connector.topic";

    public static final String JENKINS_POLL_INTERVAL_MS_CONFIG = "jenkins.pollIntervalInMillis";
    private static final String JENKINS_POLL_INTERVAL_MS_DOC = "Frequency in ms to poll for new build events in Jenkins.";
    public static final int JENKINS_POLL_INTERVAL_MS_DEFAULT = 60000;//every minute
    private static final String JENKINS_POLL_INTERVAL_MS_DISPLAY = "Poll Interval in milliseconds";

    public static final String JENKINS_GROUP = "Jenkins";
    public static final String CONNECTOR_GROUP = "Connector";

    public static final ConfigDef DEFS = new ConfigDef();

    static {
        DEFS
                .define(JENKINS_BASE_URL_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, JENKINS_BASE_URL_DOC, JENKINS_GROUP, 1, ConfigDef.Width.LONG, JENKINS_BASE_URL_DISPLAY)
                .define(JENKINS_USERNAME_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JENKINS_USERNAME_CONFIG_DOC, JENKINS_GROUP, 5, ConfigDef.Width.MEDIUM, JENKINS_USERNAME_DISPLAY)
                .define(JENKINS_PASSWORD_OR_API_TOKEN_CONFIG, ConfigDef.Type.STRING, "", ConfigDef.Importance.LOW, JENKINS_PASSWORD_OR_API_TOKEN_DOC, JENKINS_GROUP, 6, ConfigDef.Width.MEDIUM, JENKINS_PASSWORD_OR_API_TOKEN_DISPLAY)
                .define(JENKINS_CONN_TIMEOUT_CONFIG, ConfigDef.Type.INT, JENKINS_CONN_TIMEOUT_DEFAULT, ConfigDef.Importance.LOW, JENKINS_CONN_TIMEOUT_DOC, JENKINS_GROUP, 3, ConfigDef.Width.SHORT, JENKINS_CONN_TIMEOUT_DISPLAY)
                .define(JENKINS_READ_TIMEOUT_CONFIG, ConfigDef.Type.INT, JENKINS_READ_TIMEOUT_DEFAULT, ConfigDef.Importance.LOW, JENKINS_READ_TIMEOUT_DOC, JENKINS_GROUP, 4, ConfigDef.Width.SHORT, JENKINS_READ_TIMEOUT_DISPLAY)
                .define(JOBS_RESOURCE_PATH_CONFIG, ConfigDef.Type.STRING, JOBS_RESOURCE_PATH_DEFAULT, ConfigDef.Importance.LOW, JOBS_RESOURCE_PATH_DOC, JENKINS_GROUP, 7, ConfigDef.Width.MEDIUM, JOBS_RESOURCE_PATH_DISPLAY)
                .define(JENKINS_POLL_INTERVAL_MS_CONFIG, ConfigDef.Type.LONG, JENKINS_POLL_INTERVAL_MS_DEFAULT, ConfigDef.Importance.LOW, JENKINS_POLL_INTERVAL_MS_DOC, JENKINS_GROUP, 2, ConfigDef.Width.SHORT, JENKINS_POLL_INTERVAL_MS_DISPLAY)
                .define(TOPIC_CONFIG, ConfigDef.Type.STRING, TOPIC_CONFIG_DEFAULT, ConfigDef.Importance.LOW, TOPIC_CONFIG_DOC, CONNECTOR_GROUP, 1, ConfigDef.Width.LONG, TOPIC_DISPLAY);
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

    public int getJenkinsConnTimeout() {
        return getInt(JENKINS_CONN_TIMEOUT_CONFIG);
    }

    public int getJenkinsReadTimeout() {
        return getInt(JENKINS_READ_TIMEOUT_CONFIG);
    }
}
