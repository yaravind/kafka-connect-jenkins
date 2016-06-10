package org.aravind.oss.kafka.connect.jenkins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static String extractJobName(String jobUrl) {
        //For input - https://builds.apache.org/job/Accumulo-Master/
        //This method should return - Accumulo-Master
        String[] tokens = jobUrl.split("/");
        return tokens[tokens.length - 1];
    }

    public static String urlDecode(String jobName) {
        try {
            return URLDecoder.decode(jobName, String.valueOf(Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            logger.warn("Ignoring the error and returning the original job name. Unable to URLDecode {}", jobName);
            return jobName;
        }
    }
}
