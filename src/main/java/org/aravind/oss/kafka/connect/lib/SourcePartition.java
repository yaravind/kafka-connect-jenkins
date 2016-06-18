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

    public static SourcePartition make(String k, String v) {
        return new SourcePartition(k, v);
    }

    private SourcePartition(String k, String v) {
        key = k.intern();
        value = v.intern();
        encoded = Collections.singletonMap(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourcePartition that = (SourcePartition) o;

        if (!key.equals(that.key)) return false;
        return value.equals(that.value);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}