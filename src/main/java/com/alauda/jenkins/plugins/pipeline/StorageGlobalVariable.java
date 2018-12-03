package com.alauda.jenkins.plugins.pipeline;

import groovy.lang.Binding;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;

@Extension
public class StorageGlobalVariable extends GlobalVariable {

    @Nonnull
    @Override
    public String getName() {
        return "alaudaStorage";
    }

    @Nonnull
    @Override
    public Object getValue(@Nonnull CpsScript script) throws Exception {
        Binding binding = script.getBinding();
        Object storage;
        if (binding.hasVariable(getName())) {
            storage = binding.getVariable(getName());
        } else {
            // Note that if this were a method rather than a constructor, we
            // would need to mark it @NonCPS lest it throw
            // CpsCallableInvocation.
            storage = script.getClass().getClassLoader()
                    .loadClass("com.alauda.jenkins.plugins.StorageDSL")
                    .getConstructor(CpsScript.class).newInstance(script);
            binding.setVariable(getName(), storage);
        }
        return storage;
    }
}
