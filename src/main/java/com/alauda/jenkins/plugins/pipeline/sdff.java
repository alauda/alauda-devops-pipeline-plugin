package com.alauda.jenkins.plugins.pipeline;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

@Extension
public class sdff implements GraphListener {

    @Override
    public void onNewHead(FlowNode node) {
        System.out.printf(node.getDisplayName());

        node.addAction(new LabelAction(""));
    }
}
