package com.alauda.jenkins.plugins.storage;

import java.io.Serializable;
import java.util.Map;

public interface IStorage extends Serializable {
    /**
     * add Param by key, value, labels
     * @param key not null, if key is null, will do nothing.
     * @param value value
     * @param labels labels
     */
    public void add(String key, Object value, String... labels);

    /**
     *
     * @param key  if key is not exist, will do nothing.
     * @param labels labels
     */
    public void addLabel(String key, String... labels);

    /**
     *
     * @param key  if key is null or not exist, return null.
     * @return return the value.
     */
    public Object getObject(String key);

    /**
     *
     * @param key  key
     * @param defaultValue  if key is null or not exist, return defaultValue.
     * @return return the value.
     */
    public Object getObject(String key, Object defaultValue);

    /**
     * filter labels with or
     * @param labels labels
     * @return result as map which has one of ths labels
     */
    public Map<String, Object> getByPartialLabels(String... labels);

    /**
     * filter labels with and
     * @param labels labels
     * @return result as map which has all of ths labels
     */
    public Map<String, Object> getByAllLabels(String... labels);
}

