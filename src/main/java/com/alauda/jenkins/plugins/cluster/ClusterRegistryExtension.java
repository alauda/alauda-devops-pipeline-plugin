package com.alauda.jenkins.plugins.cluster;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

import java.util.Collection;
import java.util.Optional;

public interface ClusterRegistryExtension extends ExtensionPoint {
    ClusterRegistry getClusterRegistry(String name);

    Collection<ClusterRegistry> getClusterRegistries();

    class ClusterRegistry {
        private String name;
        private String token;
        private String serverCertificateAuthority;
        private boolean skipTlsVerify;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getServerCertificateAuthority() {
            return serverCertificateAuthority;
        }

        public void setServerCertificateAuthority(String serverCertificateAuthority) {
            this.serverCertificateAuthority = serverCertificateAuthority;
        }

        public boolean isSkipTlsVerify() {
            return skipTlsVerify;
        }

        public void setSkipTlsVerify(boolean skipTlsVerify) {
            this.skipTlsVerify = skipTlsVerify;
        }
    }

    static ExtensionList<ClusterRegistryExtension> getAll() {
        return Jenkins.getInstance().getExtensionList(ClusterRegistryExtension.class);
    }

    static ClusterRegistryExtension findByName(String name) {
        Optional<ClusterRegistryExtension> options = getAll().stream().filter(extension -> extension.getClusterRegistry(name) != null).findFirst();
        if(options.isPresent()) {
            return options.get();
        } else {
            return null;
        }
    }
}
