package com.alauda.jenkins.plugins

import com.alauda.jenkins.plugins.storage.IStorage
import com.alauda.jenkins.plugins.storage.Storage
import com.cloudbees.groovy.cps.NonCPS
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution
import org.jenkinsci.plugins.workflow.cps.CpsScript
import org.jenkinsci.plugins.workflow.cps.CpsThread

import java.util.logging.Level
import java.util.logging.Logger

class StorageDSL implements Serializable {

    static final Logger LOGGER = Logger.getLogger(StorageDSL.class.getName());
    private CpsScript script
    private IStorage storage;

    @NonCPS
    public static void logToTaskListener(String s) {
        CpsThread thread = CpsThread.current();
        CpsFlowExecution execution = thread.getExecution();

        try {
            execution.getOwner().getListener().getLogger().println(s);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "logToTaskListener", e);
        }
    }

    public StorageDSL(CpsScript script) {
        this.script = script
        this.storage = new Storage();
    }

    public void add(String key, Object value, String... labels) {
        storage.add(key, value, labels)
    }

    public void addLabel(String key, String... labels) {
        storage.addLabel(key, labels)
    }

    public Object getObject(String key) {
        return storage.getObject(key)
    }

    public Object getObject(String key, Object defaultValue) {
        return storage.getObject(key, defaultValue)
    }

    public Map<String, Object> getByPartialLabels(String... labels) {
        return storage.getByPartialLabels(labels)
    }

    public Map<String, Object> getByAllLabels(String... labels) {
        return storage.getByAllLabels(labels)
    }

}
