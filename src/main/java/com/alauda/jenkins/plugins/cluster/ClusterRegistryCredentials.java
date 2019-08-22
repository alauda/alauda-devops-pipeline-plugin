package com.alauda.jenkins.plugins.cluster;

import hudson.Extension;
import hudson.model.ItemGroup;
import io.alauda.jenkins.plugins.credentials.metadata.CredentialsWithMetadata;
import io.alauda.jenkins.plugins.credentials.scope.KubernetesSecretScope;

import java.util.ArrayList;
import java.util.List;

@Extension
public class ClusterRegistryCredentials implements KubernetesSecretScope {
    private static List<String> namespacedList = new ArrayList<>();

    @Override
    public boolean isInScope(ItemGroup owner) {
        return true;
    }

    @Override
    public boolean shouldShowInScope(ItemGroup owner, CredentialsWithMetadata credentials) {
        return namespacedList.contains(credentials.getCredentials().getId());
    }

    public static void addNamespaced(String item) {
        namespacedList.add(item);
    }

    public static void removeNamespaced(String item) {
        namespacedList.remove(item);
    }

    public static boolean containNamespaced(String item) {
        return namespacedList.contains(item);
    }
}
