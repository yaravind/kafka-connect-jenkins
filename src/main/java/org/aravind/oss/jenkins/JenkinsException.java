package org.aravind.oss.jenkins;

/**
 * Wrapper exception for everything from Jenkins sub-system
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsException extends Exception {
    public JenkinsException(String msg, Exception e) {
        super(msg, e);
    }

    public JenkinsException(String msg) {
        super(msg);
    }
}
