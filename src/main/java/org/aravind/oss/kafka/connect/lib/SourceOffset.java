package org.aravind.oss.kafka.connect.lib;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public class SourceOffset {
    public final String key;
    public final Object value;
    public final Map<String, Object> encoded;

    public static SourceOffset make(String k, Object v) {
        return new SourceOffset(k, v);
    }

    public static SourceOffset decode(Map<String, Object> in) {
        if (in != null) {
            Set<Map.Entry<String, Object>> entries = in.entrySet();
            assert entries.size() == 1;
            Map.Entry<String, Object> entry = entries.iterator().next();
            return new SourceOffset(entry.getKey(), entry.getValue());
        }
        return null;
    }

    private SourceOffset(String k, Object v) {
        key = k.intern();
        value = v;
        encoded = Collections.singletonMap(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceOffset offset = (SourceOffset) o;

        if (!key.equals(offset.key)) return false;
        return value.equals(offset.value);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SourceOffset{" +
                "key='" + key + '\'' +
                ", value=" + value +
                ", encoded=" + encoded +
                '}';
    }
}