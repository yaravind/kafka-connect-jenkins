package org.aravind.oss.kafka.connect.lib;

import java.util.Collections;
import java.util.Map;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class SourcePartition {
    public final String key;
    public final String value;
    public final Map<String, ?> encoded;

    public SourcePartition(String k, String v) {
        key = k.intern();
        value = v.intern();
        encoded = Collections.singletonMap(key, value);
    }

}