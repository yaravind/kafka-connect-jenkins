package org.aravind.oss.kafka.connect.lib;

/**
 * @author Aravind R Yarram
 * @since 0.5.0
 */
public interface TaskConfigExtractor<T> {
    public String extract(T input);
}