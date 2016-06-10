package org.aravind.oss.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;

import static org.aravind.oss.kafka.connect.jenkins.Util.urlDecode;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildCollection {
    private String name;
    private List<Build> builds;
    private Build lastBuild;
    private static final Logger logger = LoggerFactory.getLogger(BuildCollection.class);

    public String getName() {
        return name;
    }

    public String getUrlDecodedName() {
        return urlDecode(name);
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

    public Build getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(Build lastBuild) {
        this.lastBuild = lastBuild;
    }

    @Override
    public String toString() {
        return "BuildCollection{" +
                "name='" + name + '\'' +
                ", builds=" + builds +
                ", lastBuild=" + lastBuild +
                '}';
    }
}
