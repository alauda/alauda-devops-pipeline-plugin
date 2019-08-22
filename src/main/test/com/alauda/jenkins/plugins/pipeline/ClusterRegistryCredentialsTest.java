package com.alauda.jenkins.plugins.pipeline;

import com.alauda.jenkins.plugins.cluster.ClusterRegistryCredentials;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClusterRegistryCredentialsTest {
    @Test
    public void namespacedListTest() {
        final String guest = "mock";

        ClusterRegistryCredentials.addNamespaced(guest);
        assertTrue(ClusterRegistryCredentials.containNamespaced(guest));

        ClusterRegistryCredentials.removeNamespaced(guest);
        assertFalse(ClusterRegistryCredentials.containNamespaced(guest));
    }
}
