package com.alauda.jenkins.plugins.cluster;

import hudson.Extension;

/**
 * Store for the kubernetes cluster
 */
@Extension
public class DefaultClusterRegistry implements ClusterRegistryExtension {
    @Override
    public ClusterRegistry getClusterRegistry(String name) {
        return null;
    }
}
