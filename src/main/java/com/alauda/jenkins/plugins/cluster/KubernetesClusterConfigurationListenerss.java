package com.alauda.jenkins.plugins.cluster;

import com.alauda.jenkins.plugins.ClusterConfig;
import com.alauda.jenkins.plugins.Devops;
import hudson.Extension;
import io.alauda.jenkins.devops.support.KubernetesCluster;
import io.alauda.jenkins.devops.support.KubernetesClusterConfigurationListener;
import io.kubernetes.client.ApiClient;

/**
 *
 */
@Extension
public class KubernetesClusterConfigurationListenerss implements KubernetesClusterConfigurationListener {
    @Override
    public void onConfigChange(KubernetesCluster cluster, ApiClient apiClient) {
        if(!cluster.isManagerCluster()) {
            return;
        }

        Devops.DescriptorImpl clusterConfigMgr = new Devops.DescriptorImpl();

        ClusterConfig clusterConfig = clusterConfigMgr.getClusterConfig(Devops.DEFAULT_CLUSTER);
        if(clusterConfig == null) {
            clusterConfig = new ClusterConfig(Devops.DEFAULT_CLUSTER);
            clusterConfigMgr.addClusterConfig(clusterConfig);
        }

        clusterConfig.setServerUrl(cluster.getMasterUrl());
        clusterConfig.setSkipTlsVerify(cluster.isSkipTlsVerify());
        clusterConfig.setCredentialsId(cluster.getCredentialsId());
        clusterConfig.setServerCertificateAuthority(cluster.getServerCertificateAuthority());
        clusterConfigMgr.save();
    }

    @Override
    public void onConfigError(KubernetesCluster kubernetesCluster, Throwable throwable) {

    }
}
