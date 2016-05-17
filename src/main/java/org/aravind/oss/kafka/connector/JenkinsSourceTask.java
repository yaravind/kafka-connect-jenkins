package org.aravind.oss.kafka.connector;

import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.List;
import java.util.Map;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsSourceTask extends SourceTask {
    public static final String JOB_URLS = "job.urls";
    public static final String JOB_NAME = "job.name";

    @Override
    public String version() {
        return Version.get();
    }

    @Override
    public void start(Map<String, String> props) {

    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        return null;
    }

    @Override
    public void stop() {

    }
}
