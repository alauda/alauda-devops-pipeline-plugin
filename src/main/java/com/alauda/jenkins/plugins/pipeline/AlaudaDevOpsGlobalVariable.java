package com.alauda.jenkins.plugins.pipeline;

import groovy.lang.Binding;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import javax.annotation.Nonnull;

/**
 * Defines the "alaudaDevops" global variable in pipeline DSL scripts. It's
 * attributes are defined by the content of
 * resources/com/alauda/jenkins/plugins/AlaudaDevopsGlobalVariable.groovy
 */
@Extension
public class AlaudaDevOpsGlobalVariable extends GlobalVariable {

    @Nonnull
    @Override
    public String getName() {
        return "alaudaDevops";
    }

    @Nonnull
    @Override
    public Object getValue(@Nonnull CpsScript script) throws Exception {
        Binding binding = script.getBinding();
        script.println();
        Object alaudaDevops;
        if (binding.hasVariable(getName())) {
            alaudaDevops = binding.getVariable(getName());
        } else {
            // Note that if this were a method rather than a constructor, we
            // would need to mark it @NonCPS lest it throw
            // CpsCallableInvocation.
            alaudaDevops = script.getClass().getClassLoader()
                    .loadClass("com.alauda.jenkins.plugins.AlaudaDevopsDSL")
                    .getConstructor(CpsScript.class).newInstance(script);
            binding.setVariable(getName(), alaudaDevops);
        }
        return alaudaDevops;

    }
}
