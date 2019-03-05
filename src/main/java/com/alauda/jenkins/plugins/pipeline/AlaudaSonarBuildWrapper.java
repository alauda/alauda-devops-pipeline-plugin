package com.alauda.jenkins.plugins.pipeline;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.sonar.SonarBuildWrapper;
import hudson.plugins.sonar.SonarGlobalConfiguration;
import hudson.plugins.sonar.SonarInstallation;
import hudson.plugins.sonar.model.TriggersConfig;
import hudson.tasks.BuildWrapperDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AlaudaSonarBuildWrapper extends SonarBuildWrapper {

    private static final String DEFAULT_PREFIX = "devops-alauda-sonar";

    private AlaudaSonarBuildWrapper(@Nullable String installationName) {
        super(installationName);
    }

    @DataBoundConstructor
    public AlaudaSonarBuildWrapper(String namespace, String sonarBindingName, String serverUrl, String encodedServerAuthenticationToken) {
        this(DEFAULT_PREFIX + namespace + sonarBindingName);

        String token = new String(Base64.getDecoder().decode(encodedServerAuthenticationToken));
        SonarInstallation installation = new SonarInstallation(String.format("%s-%s-%s", DEFAULT_PREFIX, namespace, sonarBindingName),
                serverUrl, token,
                "", "", new TriggersConfig(), "");

        synchronized (AlaudaSonarBuildWrapper.class) {
            List<SonarInstallation> installations = new ArrayList<>();
            for (SonarInstallation i : SonarGlobalConfiguration.get().getInstallations()) {
                if (!i.getName().equals(installation.getName())) {
                    installations.add(i);
                }
            }
            installations.add(installation);
            SonarGlobalConfiguration.get().setInstallations(installations.toArray(new SonarInstallation[]{}));
        }
    }


    @Symbol("withDestructuringParameterSonarEnv")
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return SonarGlobalConfiguration.get().isBuildWrapperEnabled();
        }

        /**
         * @return all configured {@link hudson.plugins.sonar.SonarInstallation}
         */
        public SonarInstallation[] getSonarInstallations() {
            return SonarInstallation.all();
        }

    }
}
