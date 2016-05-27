package org.aravind.oss.jenkins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.aravind.oss.jenkins.domain.Jenkins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

/**
 * Represents one single running instance of Jenkins. You will create one object per running Jenkins instance.
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsClient {
    private static final int SO_TIMEOUT_IN_MILLIS = 3000;
    private static final int CONN_TIMEOUT_IN_MILLIS = 500;
    private final URL resourceUrl;
    private Optional<String> userName = Optional.empty();
    private Optional<String> passwordOrApiToken = Optional.empty();
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JenkinsClient.class);

    /**
     * @param url resource url of the jenkins item
     */
    public JenkinsClient(URL url) {
        resourceUrl = url;
    }

    /**
     * @param url      url of the jenkins instance
     * @param uname    username if authentication is enabled in jenkins instance
     * @param password password or API token if authentication is enabled in jenkins instance
     */
    public JenkinsClient(URL url, String uname, String password) throws JenkinsException {
        if (uname.isEmpty()) {
            throw new JenkinsException("Missing Jenkins username for authentication");
        }
        if (password.isEmpty()) {
            throw new JenkinsException("Missing Jenkins password (or API token) for authentication");
        }
        resourceUrl = url;
        userName = Optional.of(uname);
        passwordOrApiToken = Optional.of(password);
    }

    public HttpURLConnection connect() throws JenkinsException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) resourceUrl.openConnection();
            conn.setConnectTimeout(CONN_TIMEOUT_IN_MILLIS);
            conn.setReadTimeout(SO_TIMEOUT_IN_MILLIS);
            if (userName.isPresent()) {
                conn.setRequestProperty("Authorization", "Basic " + getAuthenticationString());
            }

            conn.connect();

            return conn;
        } catch (IOException e) {
            logger.error("Error while connecting to {}", resourceUrl, e);
            throw new JenkinsException("Error while opening a connection to " + resourceUrl, e);
        }
    }

    private String getAuthenticationString() {
        String authString = userName.get() + ":" + passwordOrApiToken.get();
        return Base64.getEncoder().encodeToString(authString.getBytes());
    }

    public Optional<String> get() throws JenkinsException {
        logger.debug("GET to {}", resourceUrl);
        HttpURLConnection conn = connect();

        try {
            InputStream is = conn.getInputStream();
            String resp = IOUtils.toString(is, Charset.forName("UTF-8"));

            // close the input stream so that the connection can be reused
            is.close();

            return Optional.of(resp);
        } catch (IOException e) {
            logger.warn("IGNORING this exception. Just a WARNING to debug this issue. Error while HTTP GET to {}", resourceUrl, e);
            //Need to read even the error stream so that we can take advantage of socket reuse in Keep-Alive
            //http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html
            try {
                int respCode = ((HttpURLConnection) conn).getResponseCode();
                logger.warn("HTTP response code {}", respCode);
                InputStream es = ((HttpURLConnection) conn).getErrorStream();

                if (es != null) {
                    // read the error response body so that the connection can be reused
                    String resp = IOUtils.toString(es, Charset.forName("UTF-8"));

                    // close the error stream so that the connection can be reused
                    es.close();
                }
            } catch (IOException ex) {
                // ignore the exception
            }
        }
        return Optional.empty();
    }

    public Optional<Jenkins> getJenkins() throws JenkinsException {
        Optional<String> resp = get();

        if (resp.isPresent()) {
            try {
                Jenkins j = mapper.readValue(resp.get(), Jenkins.class);
                return Optional.of(j);
            } catch (IOException e) {
                logger.error("Error while parsing the JSON {}", resp.get(), e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
