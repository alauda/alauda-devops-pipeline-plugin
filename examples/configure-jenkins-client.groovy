import jenkins.model.Jenkins

import com.alauda.jenkins.plugins.Devops
import com.alauda.jenkins.plugins.ClusterConfig

Devops.DescriptorImpl alaudaDSL = (Devops.DescriptorImpl)Jenkins.getInstance().getDescriptor("com.alauda.jenkins.plugins.Devops")

ClusterConfig cluster1 = new ClusterConfig("cluster1")
cluster1.setServerUrl("https://cluster:8443")
cluster1.setServerCertificateAuthority(new File("path/to/file").getText("UTF-8"))

alaudaDSL.addClusterConfig(cluster1)
alaudaDSL.save()
