package com.alauda.jenkins.plugins.cluster;

import com.alauda.jenkins.plugins.Devops;
import com.alauda.jenkins.plugins.Devops.DescriptorImpl;
import com.alauda.jenkins.plugins.core.NotSupportAuthException;
import com.alauda.jenkins.plugins.core.NotSupportSecretException;
import hudson.Extension;
import io.alauda.devops.java.clusterregistry.client.apis.ClusterregistryK8sIoV1alpha1Api;
import io.alauda.devops.java.clusterregistry.client.models.V1alpha1AuthInfo;
import io.alauda.devops.java.clusterregistry.client.models.V1alpha1Cluster;
import io.alauda.devops.java.clusterregistry.client.models.V1alpha1ClusterList;
import io.alauda.devops.java.clusterregistry.client.models.V1alpha1ObjectReference;
import io.alauda.jenkins.devops.support.KubernetesCluster;
import io.alauda.jenkins.devops.support.KubernetesClusterConfigurationListener;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.util.CallGeneratorParams;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Store for the kubernetes cluster
 */
@Extension
public class DefaultClusterRegistry implements ClusterRegistryExtension, KubernetesClusterConfigurationListener {
    private static final Logger LOGGER = Logger.getLogger(LocalFileSystemClusterRegistry.class.getName());

    private Map<String, ClusterRegistry> clusterMap = new ConcurrentHashMap<>();
    private SharedInformerFactory previousFactory;
    private KubernetesCluster kubernetesCluster;
    private ApiClient apiClient;

    @Override
    public ClusterRegistry getClusterRegistry(String name) {
        if(!clusterMap.containsKey(name)) {
            return null;
        }
        return clusterMap.get(name);
    }

    @Override
    public Collection<ClusterRegistry> getClusterRegistries() {
        return clusterMap.values();
    }

    @Override
    public void onConfigChange(KubernetesCluster kubernetesCluster, ApiClient apiClient) {
        clusterMap.clear();
        this.kubernetesCluster = kubernetesCluster;
        this.apiClient = apiClient;

        setDefaultCluster(kubernetesCluster);

        watch();
    }

    private void setDefaultCluster(KubernetesCluster cluster) {
        Devops.DescriptorImpl descriptor = (DescriptorImpl) new Devops().getDescriptor();
       descriptor.setDefaultCluster(cluster);
    }

    public void watch() {
        if(previousFactory != null) {
            previousFactory.stopAllRegisteredInformers();
        }

        apiClient.getHttpClient().setReadTimeout(0, TimeUnit.SECONDS); // infinite timeout
        SharedInformerFactory factory = new SharedInformerFactory();
        previousFactory = factory;
        ClusterregistryK8sIoV1alpha1Api coreV1Api = new ClusterregistryK8sIoV1alpha1Api(apiClient);

        String namespace = new Devops.DescriptorImpl().getNamespace();

        // Node informer
        SharedIndexInformer<V1alpha1Cluster> nodeInformer =
                factory.sharedIndexInformerFor(
                        (CallGeneratorParams params) -> {
                            try {
                                return coreV1Api.listNamespacedClusterCall(namespace, null, null, null,
                                        "", null, null,
                                        params.resourceVersion,
                                        params.timeoutSeconds,
                                        params.watch, null, null);
                            } catch (ApiException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        V1alpha1Cluster.class,
                        V1alpha1ClusterList.class);

        nodeInformer.addEventHandler(new ClusterRegistryResourceEventHandler());
        factory.startAllRegisteredInformers();
    }

    @Override
    public void onConfigError(KubernetesCluster kubernetesCluster, Throwable e) {
        // TODO should take care of exception here
    }

    private ClusterRegistry turn(V1alpha1Cluster cluster) {
        String clusterName = cluster.getMetadata().getName();

        ClusterRegistry clusterRegistry = new ClusterRegistry();
        clusterRegistry.setName(clusterName);
        clusterRegistry.setSkipTlsVerify(true); // TODO should take from cluster

        try {
            clusterRegistry.setToken(getToken(cluster.getSpec().getAuthInfo()));
        } catch (NotSupportAuthException | NotSupportSecretException e) {
            String msg = String.format("Error happened when fetching secret for cluster registry %s", clusterName);
            LOGGER.log(Level.WARNING, msg, e);
        }

        return clusterRegistry;
    }

    private String getToken(V1alpha1AuthInfo authInfo) {
        V1alpha1ObjectReference controller = authInfo.getController();
        if(controller == null) {
            return null;
        }

        String kind = controller.getKind();
        String namespace = controller.getNamespace();
        String name = controller.getName();
        if(!"secret".equalsIgnoreCase(kind)) {
            throw new NotSupportAuthException(kind);
        }

        return String.format("%s-%s", namespace, name);
    }

    private class ClusterRegistryResourceEventHandler implements ResourceEventHandler<V1alpha1Cluster> {

        @Override
        public void onAdd(V1alpha1Cluster cluster) {
            String clusterName = cluster.getMetadata().getName();

            LOGGER.log(Level.FINE, "Add event for ClusterRegistry: {0}", clusterName);
            clusterMap.put(clusterName, turn(cluster));
        }

        @Override
        public void onUpdate(V1alpha1Cluster oldCluster, V1alpha1Cluster newCluster) {
            String clusterName = oldCluster.getMetadata().getName();

            LOGGER.log(Level.FINE, "Update event for ClusterRegistry: {0}", clusterName);
            clusterMap.put(clusterName, turn(newCluster));
        }

        @Override
        public void onDelete(V1alpha1Cluster cluster, boolean b) {
            String clusterName = cluster.getMetadata().getName();

            LOGGER.log(Level.FINE, "Delete event for ClusterRegistry: {0}", clusterName);
            clusterMap.remove(cluster.getMetadata().getName());
        }
    }
}
