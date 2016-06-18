package org.aravind.oss.kafka.connect.lib;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class SourcePartition {
    private final String key;
    private final String value;

    public SourcePartition(String k, String v) {
        key = k.intern();
        value = v.intern();
    }
}