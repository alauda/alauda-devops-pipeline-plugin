package com.alauda.jenkins.plugins;

public class ManagerClusterCache {
    private ManagerClusterCache(){}
    private static final ManagerClusterCache managerClusterCache = new ManagerClusterCache();

    public static ManagerClusterCache getInstance() {
        return managerClusterCache;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    private String credentialId;
}
