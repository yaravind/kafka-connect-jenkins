package org.aravind.oss.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildCollection {
    private String name;
    private List<Build> builds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(List<Build> builds) {
        this.builds = builds;
    }


    @Override
    public String toString() {
        return "BuildCollection{" +
                "name='" + name + '\'' +
                ", builds=" + builds +
                '}';
    }
}
