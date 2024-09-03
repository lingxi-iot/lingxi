package cn.gofree.lingxi.eventcenter.entity;

import io.openmessaging.KeyValue;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TargetKeyValue implements KeyValue, Serializable {
    /**
     * unique id
     */
    private String targetKeyId;

    /**
     * All data are reserved in this map.
     */
    private Map<String, String> properties;

    public TargetKeyValue() {
        properties = new ConcurrentHashMap<>();
        targetKeyId = UUID.randomUUID().toString();
    }

    public TargetKeyValue(Map<String, String> targetMap) {
        properties = new ConcurrentHashMap<>(targetMap);
        targetKeyId = UUID.randomUUID().toString();
    }

    @Override
    public KeyValue put(String key, int value) {
        properties.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public KeyValue put(String key, long value) {
        properties.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public KeyValue put(String key, double value) {
        properties.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public KeyValue put(String key, String value) {
        properties.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public int getInt(String key) {
        if (!properties.containsKey(key))
            return 0;
        return Integer.valueOf(properties.get(key));
    }

    @Override
    public int getInt(final String key, final int defaultValue) {
        return properties.containsKey(key) ? getInt(key) : defaultValue;
    }

    @Override
    public long getLong(String key) {
        if (!properties.containsKey(key))
            return 0;
        return Long.valueOf(properties.get(key));
    }

    @Override
    public long getLong(final String key, final long defaultValue) {
        return properties.containsKey(key) ? getLong(key) : defaultValue;
    }

    @Override
    public double getDouble(String key) {
        if (!properties.containsKey(key))
            return 0;
        return Double.valueOf(properties.get(key));
    }

    @Override
    public double getDouble(final String key, final double defaultValue) {
        return properties.containsKey(key) ? getDouble(key) : defaultValue;
    }

    @Override
    public String getString(String key) {
        return properties.get(key);
    }

    @Override
    public String getString(final String key, final String defaultValue) {
        return properties.containsKey(key) ? getString(key) : defaultValue;
    }

    @Override
    public Set<String> keySet() {
        return properties.keySet();
    }

    @Override
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public io.openmessaging.KeyValue putAll(Map<String, String> configProps) {
        this.properties.putAll(configProps);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TargetKeyValue that = (TargetKeyValue) o;
        return Objects.equals(targetKeyId, that.targetKeyId) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetKeyId, properties);
    }

    @Override
    public String toString() {
        return "TargetKeyValue{" +
                "targetKeyId='" + targetKeyId + '\'' +
                ", properties=" + properties +
                '}';
    }

    public String getTargetKeyId() {
        return targetKeyId;
    }


}
