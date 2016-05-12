package org.aravind.oss.jenkins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;

/**
 * Represents one single running instance of Jenkins. You will create one object per running Jenkins instance.
 *
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class JenkinsInstance {
    private static final int SO_TIMEOUT_IN_MILLIS = 3000;
    private static final int CONN_TIMEOUT_IN_MILLIS = 500;
    private final URL baseUrl;
    private Optional<String> userName;
    private Optional<String> passwordOrApiToken;
    private HttpURLConnection conn;

    /**
     * @param url url of the jenkins instance
     */
    public JenkinsInstance(URL url) throws JenkinsException {
        baseUrl = url;
        connect();
    }

    /**
     * @param url      url of the jenkins instance
     * @param uname    username if authentication is enabled in jenkins instance
     * @param password password or API token if authentication is enabled in jenkins instance
     */
    public JenkinsInstance(URL url, String uname, String password) throws JenkinsException {
        if (uname == null || uname.isEmpty()) {
            throw new JenkinsException("Missing Jenkins username for authentication");
        }
        if (password == null || password.isEmpty()) {
            throw new JenkinsException("Missing Jenkins password (or API token) for authentication");
        }
        baseUrl = url;
        userName = Optional.of(uname);
        passwordOrApiToken = Optional.of(password);

        connect();
        conn.setRequestProperty("Authorization", "Basic " + getAuthenticationString());
    }

    private void connect() throws JenkinsException {
        try {
            conn = (HttpURLConnection) baseUrl.openConnection();
            conn.setConnectTimeout(CONN_TIMEOUT_IN_MILLIS);
            conn.setReadTimeout(SO_TIMEOUT_IN_MILLIS);
            conn.connect();
        } catch (IOException e) {
            throw new JenkinsException("Error while opening a connection to " + baseUrl, e);
        }
    }

    private void disconnect() {
        if (conn != null)
            conn.disconnect();
    }

    private String getAuthenticationString() {
        String authString = userName.get() + ":" + passwordOrApiToken.get();
        return Base64.getEncoder().encodeToString(authString.getBytes());
    }

    public Optional<String> get(int bufferSize) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream is = conn.getInputStream();

            // read the response body
            byte[] buf = new byte[bufferSize];
            int ret = 0;
            while ((ret = is.read(buf)) > 0) {
                bos.write(buf);
            }

            // close the input stream so that the connection can be reused
            is.close();

            return Optional.of(bos.toString());
        } catch (IOException e) {
            e.printStackTrace();
            //Need to read even the error stream so that we can take advantage of socket reuse in Keep-Alive
            //http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html
            try {
                int respCode = ((HttpURLConnection) conn).getResponseCode();
                InputStream es = ((HttpURLConnection) conn).getErrorStream();

                if (es != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int ret = 0;

                    // read the error response body so that the connection can be reused
                    byte[] buf = new byte[bufferSize];
                    while ((ret = es.read(buf)) > 0) {
                        bos.write(buf);
                    }
                    // close the error stream so that the connection can be reused
                    es.close();
                }
            } catch (IOException ex) {
                // deal with the exception
            }
        }
        return Optional.empty();
    }
}
