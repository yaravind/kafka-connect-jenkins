package org.aravind.oss.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.aravind.oss.jenkins.JenkinsClient;
import org.aravind.oss.jenkins.JenkinsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Build extends JenkinsItem {
    private Long number;
    private String url;
    private static final Logger logger = LoggerFactory.getLogger(Build.class);

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBuildDetailsResource() {
        return getUrl() != null ? getUrl() + "api/json" : null;
    }

    public Optional<String> getDetails() {
        try {
            setClient(new JenkinsClient(new URL(getBuildDetailsResource())));
            return getClient().get();
        } catch (MalformedURLException | JenkinsException e) {
            logger.error("WARNING only. Unable to get the build details from {}", getUrl(), e);
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "Build{" +
                "number=" + number +
                ", url='" + url + '\'' +
                '}';
    }
}
