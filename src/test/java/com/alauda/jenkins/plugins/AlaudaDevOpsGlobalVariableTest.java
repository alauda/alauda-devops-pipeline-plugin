package com.alauda.jenkins.plugins;

import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.cps.Snippetizer;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class AlaudaDevOpsGlobalVariableTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void helpLink() throws IOException, SAXException {
        JenkinsRule.WebClient client = j.createWebClient();

        GlobalVariable globalVariable = null;
        Iterable<GlobalVariable> vars = GlobalVariable.forJob(null);
        assertNotNull(vars);
        Iterator<GlobalVariable> varsIt = vars.iterator();
        while(varsIt.hasNext()) {
            GlobalVariable var = varsIt.next();

            if("alaudaDevops".equals(var.getName())) {
                globalVariable = var;
                break;
            }
        }

        assertNotNull("AlaudaDevOpsGlobalVariable is missing.", globalVariable);

        String html = client.goTo(Snippetizer.ACTION_URL + "/globals")
                .getWebResponse().getContentAsString();
        assertThat("AlaudaDevOpsGlobalVariable help page is missing", html,
                containsString("offers convenient access to AlaudaDevops-related functions"));
    }
}
