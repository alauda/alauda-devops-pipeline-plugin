package com.alauda.jenkins.plugins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

/**
 * An installation of the Devops Client Tools.
 */
public class DevopsClientTools extends ToolInstallation implements
        EnvironmentSpecific<DevopsClientTools>,
        NodeSpecific<DevopsClientTools> {

    @DataBoundConstructor
    public DevopsClientTools(String name, String home,
                             List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    @Override
    public DevopsClientTools forEnvironment(EnvVars environment) {
        return new DevopsClientTools(getName(),
                environment.expand(getHome()), getProperties());
    }

    @Override
    public DevopsClientTools forNode(Node node, TaskListener log)
            throws IOException, InterruptedException {
        return new DevopsClientTools(getName(), translateFor(node, log),
                getProperties().toList());
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        if (getHome() != null) {
            env.put("PATH+OC", getHome());
        }
    }

    @Extension
    @Symbol("oc")
    public static class DescriptorImpl extends
            ToolDescriptor<DevopsClientTools> {

        @Override
        public String getDisplayName() {
            return "Devops Client Tools";
        }

        @Override
        public DevopsClientTools[] getInstallations() {
            load();
            return super.getInstallations();
        }

        @Override
        public void setInstallations(DevopsClientTools... installations) {
            super.setInstallations(installations);
            save();
        }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return super.getDefaultInstallers();
        }

    }

}
