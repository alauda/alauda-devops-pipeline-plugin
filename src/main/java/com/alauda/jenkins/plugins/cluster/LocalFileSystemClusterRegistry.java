package com.alauda.jenkins.plugins.cluster;

import hudson.Extension;
import hudson.FilePath;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Store the cluster registry to local file system
 */
@Extension
public class LocalFileSystemClusterRegistry implements ClusterRegistryExtension {
    private static final Logger LOGGER = Logger.getLogger(LocalFileSystemClusterRegistry.class.getName());

    private Map<String, ClusterRegistry> clusterRegistryMap = new HashMap();

    @Override
    public ClusterRegistry getClusterRegistry(String name) {
        return clusterRegistryMap.get(name);
    }

    @Override
    public Collection<ClusterRegistry> getClusterRegistries() {
        return clusterRegistryMap.values();
    }

    @Initializer(after= InitMilestone.PLUGINS_STARTED, fatal=false)
    public void localConfigFiles() {
        FilePath clusterRegistryFile = Jenkins.getInstance().getRootPath().child("clusterRegistry.json");
        final String absPath;
        try {
            absPath = clusterRegistryFile.getRemote();
            if(clusterRegistryFile.isDirectory()) {
                LOGGER.warning(String.format("%s is a directory.", absPath));
                return;
            }

            if(!clusterRegistryFile.exists()) {
                LOGGER.fine(String.format("%s does not exists.", absPath));
                return;
            }

            ByteArrayOutputStream dataBuf = new ByteArrayOutputStream();
            clusterRegistryFile.copyTo(dataBuf);

            JSONArray array = JSONArray.fromObject(dataBuf.toString("utf-8"));
            int size = array.size();
            for(int i = 0; i < size; i++) {
                JSONObject obj = array.getJSONObject(i);

                String name = obj.getString("name");
                String token = obj.getString("token");
                String serverCertificateAuthority = obj.getString("serverCertificateAuthority");
                boolean skipTlsVerify = obj.getBoolean("skipTlsVerify");

                ClusterRegistry clusterRegistry = new ClusterRegistry();
                clusterRegistry.setName(name);
                clusterRegistry.setToken(token);
                clusterRegistry.setServerCertificateAuthority(serverCertificateAuthority);
                clusterRegistry.setSkipTlsVerify(skipTlsVerify);
                clusterRegistryMap.put(name, clusterRegistry);
            }

            LOGGER.info(String.format("found clusterRegistry [%d] from local file system, %s",
                    clusterRegistryMap.size(), absPath));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
