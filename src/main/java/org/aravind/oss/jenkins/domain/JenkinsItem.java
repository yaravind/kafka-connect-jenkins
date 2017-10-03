package org.aravind.oss.jenkins.domain;

import org.aravind.oss.jenkins.JenkinsClient;

/**
 * Base abstraction that all Jenkin's domain classes extend from.
 * This encapsulates the properties required to fetch dependent parts of the whole.
 * For e.g. A {@link Build} need to obtain {@link Build#getDetails()}.
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsItem {
    protected JenkinsClient client;
    protected int connTimeoutInMillis;
    protected int readTimeoutInMillis;
    protected String username;
    protected String password;

    public int getReadTimeoutInMillis() {
        return readTimeoutInMillis;
    }

    public void setReadTimeoutInMillis(int readTimeoutInMillis) {
        this.readTimeoutInMillis = readTimeoutInMillis;
    }

    public int getConnTimeoutInMillis() {
        return connTimeoutInMillis;
    }

    public void setConnTimeoutInMillis(int connTimeoutInMillis) {
        this.connTimeoutInMillis = connTimeoutInMillis;
    }

    public JenkinsClient getClient() {
        return client;
    }

    public void setClient(JenkinsClient client) {
        this.client = client;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username  = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
