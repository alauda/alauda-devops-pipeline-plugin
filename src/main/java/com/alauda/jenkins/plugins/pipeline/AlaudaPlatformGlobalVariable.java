package com.alauda.jenkins.plugins.pipeline;

import groovy.lang.Binding;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;

/**
 * Provide the ability of manipulating the Alauda DevOps platform
 */
@Extension
public class AlaudaPlatformGlobalVariable extends GlobalVariable {
    @Nonnull
    @Override
    public String getName() {
        return "alaudaPlatform";
    }

    @Nonnull
    @Override
    public Object getValue(@Nonnull CpsScript cpsScript) throws Exception {
        Binding binding = cpsScript.getBinding();
        Object alaudaPlatform;
        if (binding.hasVariable(getName())) {
            alaudaPlatform = binding.getVariable(getName());
        } else {
            alaudaPlatform = cpsScript.getClass().getClassLoader()
                    .loadClass("com.alauda.jenkins.plugins.AlaudaPlatformDSL")
                    .getConstructor(CpsScript.class).newInstance(cpsScript);
            binding.setVariable(getName(), alaudaPlatform);
        }

        return alaudaPlatform;
    }
}
