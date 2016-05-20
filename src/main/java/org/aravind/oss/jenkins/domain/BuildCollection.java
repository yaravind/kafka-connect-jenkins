package org.aravind.oss.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildCollection {
    private List<Build> builds;

    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }

    @Override
    public String toString() {
        return "BuildCollection{" +
                "builds=" + builds +
                '}';
    }
}
