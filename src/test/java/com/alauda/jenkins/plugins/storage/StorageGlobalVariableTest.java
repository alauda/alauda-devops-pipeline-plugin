package com.alauda.jenkins.plugins.storage;

import org.apache.commons.compress.utils.IOUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;

public class StorageGlobalVariableTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void basic() throws IOException {
        WorkflowJob wf = r.jenkins.createProject(WorkflowJob.class, "test");

        wf.setDefinition(new CpsFlowDefinition(loadPipelineScript("storage-global-var.groovy"), true));

        try {
            WorkflowRun build = r.buildAndAssertSuccess(wf);

            r.assertLogContains("value", build);

            r.assertLogContains("defaultValue", build);

            r.assertLogContains("key=value", build);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadPipelineScript(String name) {
        return loadPipelineScript(this.getClass(), name);
    }

    private String loadPipelineScript(Class<?> clazz, String name) {
        try {
            return new String(IOUtils.toByteArray(clazz.getResourceAsStream(name)));
        } catch (Throwable t) {
            throw new RuntimeException("Could not read resource:[" + name + "].");
        }
    }
}
