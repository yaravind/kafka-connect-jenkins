package org.aravind.oss.kafka.connect.jenkins;

import org.aravind.oss.jenkins.domain.Job;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JobTaskConfigExtractor implements TaskConfigExtractor<Job> {

    @Override
    public String extract(Job input) {
        return input.getUrl();
    }
}
