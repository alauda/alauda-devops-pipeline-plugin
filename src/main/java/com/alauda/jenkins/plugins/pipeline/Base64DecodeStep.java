package com.alauda.jenkins.plugins.pipeline;


import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;

public class Base64DecodeStep extends Step {

    private final String encodedString;

    @DataBoundConstructor
    public Base64DecodeStep(String encodedString) {
        this.encodedString = encodedString;
    }

    public String getEncodedString() {
        return encodedString;
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(encodedString, stepContext);
    }


    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "base64Decode";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Decode Base64 string";
        }
    }

    public static class Execution extends AbstractSynchronousStepExecution<String> {

        private final String encodedString;

        public Execution(String encodedString, StepContext context) {
            super(context);
            this.encodedString = encodedString;
        }

        @Override
        protected String run() throws Exception {
            byte[] decodeBytes = Base64.getDecoder().decode(encodedString);
            return new String(decodeBytes, StandardCharsets.UTF_8);
        }
    }
}
