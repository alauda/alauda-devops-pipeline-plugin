package com.alauda.jenkins.plugins.cluster;

import hudson.Extension;
import hudson.model.Action;
import hudson.util.HttpResponses;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.CheckForNull;

@Extension
@Symbol("clusterRegistry")
@ExportedBean
public class ClusterRegistryAction implements Action {
    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "clusterRegistry";
    }

    @Exported
    public HttpResponse doList() {
        JSONArray clusterRegistries = new JSONArray();
        ClusterRegistryExtension.getAll().forEach( ex -> {
            ex.getClusterRegistries().forEach(clusterRegistry -> {
                JSONObject cluster = new JSONObject();
                cluster.put("provider", ex.getClass().getSimpleName());
                cluster.put("name", clusterRegistry.getName());

                clusterRegistries.add(cluster);
            });
        });

        return HttpResponses.okJSON(clusterRegistries);
    }
}
