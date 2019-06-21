package com.alauda.jenkins.plugins

import org.jenkinsci.plugins.workflow.cps.CpsScript

class AlaudaPlatformDSL extends AlaudaDevopsDSL implements Serializable {
    private CpsScript script;

    public AlaudaPlatformDSL(CpsScript script) {
        super(script);
        this.script = script;
    }

    @Override
    public <V> V withCluster(Object oname, Object ocredentialId, Closure<V> body) {
        return super.withCluster(oname, ocredentialId, body);
    }
}
