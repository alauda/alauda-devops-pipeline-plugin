package com.alauda.jenkins.plugins;

import com.alauda.jenkins.plugins.cluster.ClusterRegistryExtension;
import com.alauda.jenkins.plugins.cluster.DefaultClusterRegistry;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import io.alauda.jenkins.devops.support.KubernetesCluster;
import io.alauda.jenkins.devops.support.KubernetesClusterConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Devops extends AbstractDescribableImpl<Devops> {
    public static final String DEFAULT_CLUSTER = "default";

    private static final Logger LOGGER = Logger.getLogger(Devops.class.getName());
    public static final String DEFAULT_LOGLEVEL = "0";

    @Extension
    @Symbol("alaudaDevOpsClientConfiguration")
    public static class DescriptorImpl extends Descriptor<Devops> {
        private static final long serialVersionUID = 1L;

        // Store a config version so we're able to migrate config.
        public Long configVersion;

        public List<ClusterConfig> clusterConfigs;

        public String tool = "kubectl";
        private String proxy;
        private String namespace = "system";

        public DescriptorImpl() {
            configVersion = 1L;
            load();
        }

        @Override
        public String getDisplayName() {
            return "Devops Configuration";
        }

        public String getClientToolName() {
            return tool;
        }

        public void removeClusterConfig(ClusterConfig clusterConfig) throws IllegalArgumentException {
            if (clusterConfigs == null || clusterConfigs.size() <= 0) {
                throw new IllegalArgumentException("ClusterConfigs is null or empty");
            }

            clusterConfigs.remove(clusterConfig);
        }

        public void addClusterConfig(ClusterConfig clusterConfig) {
            if (clusterConfigs == null) {
                clusterConfigs = new ArrayList<>(1);
            }

            clusterConfigs.add(clusterConfig);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json)
                throws FormException {

            /**
             * If all cluster configurations are deleted in the UI and saved,
             * binJSON does not set the list. So clear out the list before bind.
             */
            clusterConfigs = null;

            req.bindJSON(this, json.getJSONObject("alaudaDevops"));

            KubernetesClusterConfiguration config = KubernetesClusterConfiguration.get();
            if(config != null) {
                KubernetesCluster cluster = config.getCluster();

                setDefaultCluster(cluster);
            }

            save();

            // should re-watch cluster
            ExtensionList<DefaultClusterRegistry> defaultClusterRegistry =
                    Jenkins.getInstance().getExtensionList(DefaultClusterRegistry.class);
            if(defaultClusterRegistry != null && defaultClusterRegistry.size() > 0) {
                if(defaultClusterRegistry.get(0) != null) {
                    defaultClusterRegistry.get(0).watch();
                    LOGGER.info("DefaultClusterRegistry is reloaded.");
                }
            }

            return true;
        }

        public void setDefaultCluster(KubernetesCluster cluster) {
            ClusterConfig clusterConfig = getLocalClusterConfig(Devops.DEFAULT_CLUSTER);
            if(clusterConfig == null) {
                clusterConfig = new ClusterConfig(Devops.DEFAULT_CLUSTER);
                addClusterConfig(clusterConfig);
            }

            clusterConfig.setServerUrl(cluster.getMasterUrl());
            clusterConfig.setSkipTlsVerify(cluster.isSkipTlsVerify());
            clusterConfig.setCredentialsId(cluster.getCredentialsId());
            clusterConfig.setServerCertificateAuthority(cluster.getServerCertificateAuthority());

            ManagerClusterCache.getInstance().setCredentialId(cluster.getCredentialsId());
        }

        // Creates a model that fills in logLevel options in configuration UI
        public ListBoxModel doFillLogLevelItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("0 - Least Logging", "0");
            for (int i = 1; i < 10; i++) {
                items.add("" + i, "" + i);
            }
            items.add("10 - Most Logging", "10");
            return items;
        }

        public List<ClusterConfig> getClusterConfigs() {
            if (clusterConfigs == null) {
                return new ArrayList<>(0);
            }
            return Collections.unmodifiableList(clusterConfigs);
        }

        @DataBoundSetter
        public void setClusterConfigs(List<ClusterConfig> clusterConfigs) {
            this.clusterConfigs = clusterConfigs;
        }

        public String getProxy() {
            return proxy;
        }

        @DataBoundSetter
        public void setProxy(String proxy) {
            this.proxy = proxy;
        }

        public String getNamespace() {
            return namespace;
        }

        @DataBoundSetter
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public ClusterConfig getLocalClusterConfig(String name) {
            final String clusterName = Util.fixEmptyAndTrim(name);

            Optional<ClusterConfig> clusterConfigOpt = Optional.empty();

            if (clusterConfigs != null) {
                clusterConfigOpt = clusterConfigs.stream()
                        .filter(cc  -> cc.getName().equalsIgnoreCase(clusterName))
                        .findFirst();
            }

            if (!clusterConfigOpt.isPresent()) {
                LOGGER.log(Level.WARNING, "Cannot find cluster {0} from both system configuration and cluster registry");
                return null;
            }

            return clusterConfigOpt.get();
        }

        /**
         * Determines if a cluster has been configured with a given name. If a
         * cluster has been configured with the name, its definition is
         * returned.
         * 
         * @param name
         *            The name of the cluster config to find
         * @return A ClusterConfig for the supplied parameters OR null.
         */
        public ClusterConfig getClusterConfig(String name) {
            final String clusterName = Util.fixEmptyAndTrim(name);

            Optional<ClusterConfig> clusterConfigOpt = Optional.empty();

            if (clusterConfigs != null) {
                clusterConfigOpt = clusterConfigs.stream()
                        .filter(cc  -> cc.getName().equalsIgnoreCase(clusterName))
                        .findFirst();
            }

            if (!clusterConfigOpt.isPresent()) {
                LOGGER.info(String.format("Cannot find %s from system configuration, try to find from cluster registry.", name));
                clusterConfigOpt = Optional.ofNullable(findFromClusterRegistry(clusterName));
            }

            if (!clusterConfigOpt.isPresent()) {
                LOGGER.log(Level.WARNING, "Cannot find cluster {0} from both system configuration and cluster registry", name);
                return null;
            }

            return clusterConfigOpt.get();
        }

        private ClusterConfig findFromClusterRegistry(String clusterName) {
            ClusterRegistryExtension extension = ClusterRegistryExtension.findByName(clusterName);
            if(extension == null) {
                LOGGER.fine(String.format("no matched ClusterRegistryExtension for cluster: %s", clusterName));
                return null;
            }

            ClusterRegistryExtension.ClusterRegistry clusterRegistry = extension.getClusterRegistry(clusterName);
            if(clusterRegistry == null) {
                LOGGER.fine(String.format("no matched ClusterRegistry for cluster: %s", clusterName));
                return null;
            }
            ClusterConfig clusterConfig = new ClusterConfig(clusterName);
            clusterConfig.setCredentialsId(clusterRegistry.getToken());
            clusterConfig.setServerUrl(getProxy() + "/" + clusterRegistry.getName());
            clusterConfig.setServerCertificateAuthority(clusterRegistry.getServerCertificateAuthority());
            clusterConfig.setSkipTlsVerify(clusterRegistry.isSkipTlsVerify());
            clusterConfig.setProxy(true);

            LOGGER.fine(String.format("cluster server url is: %s", clusterConfig.getServerUrl()));
            return clusterConfig;
        }
    }
}
