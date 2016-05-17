package org.aravind.oss.jenkins.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * Domain object representing the all jobs resource: /api/json
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Jenkins {
    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = Collections.unmodifiableList(jobs);
    }

    public int getJobCount() {
        return getJobs() == null || getJobs().isEmpty() ? 0 : getJobs().size();
    }
}