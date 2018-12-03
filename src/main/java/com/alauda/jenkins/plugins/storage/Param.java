package com.alauda.jenkins.plugins.storage;

import java.util.ArrayList;
import java.util.List;

public class Param implements IParam {

    private List<String> labels;
    private Object value;

    public Param(Object value, String... labels) {
        this.value = value;
        this.addLabels(labels);
    }

    @Override
    public Object getValue() {
        return value;
    }

    private boolean hasLabel(String label) {
        if (this.labels == null) {
            return false;
        }
        return this.labels.contains(label);
    }

    @Override
    public boolean hasPartialLabels(String... labels) {
        for(String label : labels) {
            if(this.hasLabel(label)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllLabels(String... labels) {
        for(String label : labels) {
            if(!this.hasLabel(label)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addLabels(String... labels) {
        if (this.labels == null) {
            this.labels = new ArrayList<String>();
        }
        for(String label : labels){
            this.labels.add(label);
        }
    }
}
