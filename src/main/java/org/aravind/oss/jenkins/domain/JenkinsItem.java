package org.aravind.oss.jenkins.domain;

import org.aravind.oss.jenkins.JenkinsClient;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsItem {

    protected JenkinsClient client;

    public JenkinsClient getClient() {
        return client;
    }

    public void setClient(JenkinsClient client) {
        this.client = client;
    }
}
