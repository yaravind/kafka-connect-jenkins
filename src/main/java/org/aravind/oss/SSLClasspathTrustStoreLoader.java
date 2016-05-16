package org.aravind.oss;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class SSLClasspathTrustStoreLoader {
    public static void setTrustStore(String trustStore, String password) throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream keystoreStream = SSLClasspathTrustStoreLoader.class.getResourceAsStream(trustStore);
        keystore.load(keystoreStream, password.toCharArray());
        trustManagerFactory.init(keystore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustManagers, null);
        SSLContext.setDefault(sc);
    }
}
