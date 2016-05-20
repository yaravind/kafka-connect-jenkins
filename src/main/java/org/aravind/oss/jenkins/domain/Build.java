package org.aravind.oss.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Build extends JenkinsItem {
    private Long number;
    private String url;

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

    @Override
    public String toString() {
        return "Build{" +
                "number=" + number +
                ", url='" + url + '\'' +
                '}';
    }
}
