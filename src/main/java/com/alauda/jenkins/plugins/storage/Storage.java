package com.alauda.jenkins.plugins.storage;

import java.util.HashMap;
import java.util.Map;

public class Storage implements IStorage {

    private Map<String, IParam> params;

    @Override
    public void add(String key, Object value, String... labels) {
        this.addParam(key, new Param(value, labels));
    }

    private void addParam(String key, IParam param) {
        if (key == null) {
            return;
        }
        if(this.params == null) {
            this.params = new HashMap<String, IParam>();
        }
        this.params.put(key, param);
    }

    @Override
    public void addLabel(String key, String... labels) {
        if(this.params.containsKey(key)){
            IParam param = this.params.get(key);
            if(param != null) {
                param.addLabels(labels);
            }
        }
    }

    @Override
    public Object getObject(String key) {
        if(this.params == null) {
            return null;
        }
        return this.params.get(key).getValue();
    }

    @Override
    public Object getObject(String key, Object defaultValue) {
        if(this.params == null || ! params.containsKey(key)) {
            return defaultValue;
        }
        return this.params.get(key).getValue();
    }

    @Override
    public Map<String, Object> getByPartialLabels(String... labels) {
        Map<String, Object> res = new HashMap<String, Object>();
        if(this.params != null) {
            for(Map.Entry<String, IParam> entry : this.params.entrySet()){
                if(entry.getValue() != null && entry.getValue().hasPartialLabels(labels)) {
                    res.put(entry.getKey(), entry.getValue().getValue());
                }
            }
        }
        return res;
    }

    @Override
    public Map<String, Object> getByAllLabels(String... labels) {
        Map<String, Object> res = new HashMap<String, Object>();
        if(this.params != null) {
            for(Map.Entry<String, IParam> entry : this.params.entrySet()){
                if(entry.getValue() != null && entry.getValue().hasAllLabels(labels)) {
                    res.put(entry.getKey(), entry.getValue().getValue());
                }
            }
        }
        return res;
    }
}
