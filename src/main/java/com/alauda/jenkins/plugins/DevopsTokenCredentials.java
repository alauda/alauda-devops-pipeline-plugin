package com.alauda.jenkins.plugins;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

public class DevopsTokenCredentials extends BaseStandardCredentials {

    private final Secret secret;

    @DataBoundConstructor
    public DevopsTokenCredentials(CredentialsScope scope, String id,
                                  String description, Secret secret) {
        super(scope, id, description);
        this.secret = secret;
    }

    public String getToken() {
        return secret.getPlainText();
    }

    public Secret getSecret() {
        return secret;
    }

    @Extension
    public static class DescriptorImpl extends
            BaseStandardCredentialsDescriptor {
        @Override
        public String getDisplayName() {
            return "Devops Token for Devops Client Plugin";
        }
    }

}
