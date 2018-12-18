package com.alauda.jenkins.plugins.storage;

import java.io.Serializable;

public interface IParam extends Serializable {
    /**
     *
     * @return param value
     */
    public Object getValue();

    /**
     * if has labels with or
     * @param labels labels
     * @return true if has one of the labels
     */
    public boolean hasPartialLabels(String... labels);

    /**
     * if has labels with and
     * @param labels labels
     * @return true if has all of the labels
     */
    public boolean hasAllLabels(String... labels);

    /**
     *
     * @param labels labels
     */
    public void addLabels(String... labels);
}
