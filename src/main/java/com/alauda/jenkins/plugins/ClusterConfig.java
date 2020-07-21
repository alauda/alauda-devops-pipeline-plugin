package com.alauda.jenkins.plugins;

import com.alauda.jenkins.plugins.util.CredentialsUtils;
import com.cloudbees.plugins.credentials.common.AbstractIdCredentialsListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClusterConfig extends AbstractDescribableImpl<ClusterConfig> implements Serializable {
    static final Logger LOGGER = Logger.getLogger(ClusterConfig.class.getName());

    private static final long serialVersionUID = 1L;
    // Human readable name for cluster. Used in drop down lists.
    private String name;

    // API server URL for the cluster.
    private String serverUrl;

    private String serverCertificateAuthority;

    private boolean skipTlsVerify;

    // If this cluster is reference, what project to assume, if any.
    private String defaultProject;
    private String credentialsId;
    /** indicate whether it's a proxy cluster config */
    private boolean proxy = false;

    @DataBoundConstructor
    public ClusterConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    @DataBoundSetter
    public void setServerUrl(String serverUrl) {
        this.serverUrl = Util.fixEmptyAndTrim(serverUrl);
    }

    public String getServerCertificateAuthority() {
        return serverCertificateAuthority;
    }

    @DataBoundSetter
    public void setServerCertificateAuthority(String serverCertificateAuthority) {
        this.serverCertificateAuthority = Util
                .fixEmptyAndTrim(serverCertificateAuthority);
    }

    public boolean isSkipTlsVerify() {
        return this.skipTlsVerify;
    }

    @DataBoundSetter
    public void setSkipTlsVerify(boolean skipTLSVerify) {
        this.skipTlsVerify = skipTLSVerify;
    }

    public String getDefaultProject() {
        return defaultProject;
    }

    @DataBoundSetter
    public void setDefaultProject(String defaultProject) {
        this.defaultProject = Util.fixEmptyAndTrim(defaultProject);
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
    }

    public boolean isProxy() {
        return proxy;
    }

    @DataBoundSetter
    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    @Override
    public String toString() {
        return String.format("Devops cluster [name:%s] [serverUrl:%s]",
                name, serverUrl);
    }

    /**
     * @return Returns a URL to contact the API server of the Devops cluster
     * running this node or throws an Exception if it cannot be
     * determined.
     */
    @Whitelisted
    public static String getHostClusterApiServerUrl() {
        String serviceHost = System.getenv("KUBERNETES_SERVICE_HOST");
        if (serviceHost == null) {
            throw new IllegalStateException(
                    "No clusterName information specified and unable to find `KUBERNETES_SERVICE_HOST` environment variable.");
        }
        String servicePort = System.getenv("KUBERNETES_SERVICE_PORT_HTTPS");
        if (servicePort == null) {
            throw new IllegalStateException(
                    "No clusterName information specified and unable to find `KUBERNETES_SERVICE_PORT_HTTPS` environment variable.");
        }
        return "https://" + serviceHost + ":" + servicePort;
    }

    // https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Plugin
    // http://javadoc.jenkins-ci.org/credentials/com/cloudbees/plugins/credentials/common/AbstractIdCredentialsListBoxModel.html
    // https://github.com/jenkinsci/kubernetes-plugin/blob/master/src/main/java/org/csanchez/jenkins/plugins/kubernetes/KubernetesCloud.java
    public static ListBoxModel doFillCredentialsIdItems(String credentialsId) {
        if (credentialsId == null) {
            credentialsId = "";
        }

        Jenkins jenkins = Jenkins.getInstance();
        AbstractIdCredentialsListBoxModel<StandardListBoxModel, StandardCredentials> defaultCredentials =
                new StandardListBoxModel().includeEmptyValue();

        if (!jenkins.hasPermission(Jenkins.ADMINISTER)) {
            // Important! Otherwise you expose credentials metadata to random
            // web requests.
            return defaultCredentials;
        }

        return defaultCredentials
                .includeAs(ACL.SYSTEM, jenkins, DevopsTokenCredentials.class) // TODO will remove this in later version
                .includeAs(ACL.SYSTEM, jenkins, StringCredentials.class);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ClusterConfig> {

        @Override
        public String getDisplayName() {
            return "Devops Cluster";
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doCheckServerUrl(@QueryParameter String value) {
            return FormValidation.validateRequired(value);
        }

        public FormValidation doVerifyConnect(@QueryParameter String serverUrl,
                                              @QueryParameter String credentialsId,
                                              @QueryParameter String serverCertificateAuthority,
                                              @QueryParameter boolean skipTlsVerify) {
            LOGGER.info("verify connection to " + serverUrl + ", skip tls " + skipTlsVerify);
            String token;
            try {
                token = CredentialsUtils.getToken(credentialsId);
            } catch (GeneralSecurityException e) {
                return FormValidation.error(String.format("Failed to connect to Cluster: %s", e.getMessage()));
            }

            SimpleKubernetesAvailabilityTestClient testClient =
                    new SimpleKubernetesAvailabilityTestClient(serverUrl, skipTlsVerify, serverCertificateAuthority, token);

            try {
                if (testClient.testConnection()) {
                    return FormValidation.ok(String.format("Connect to %s success.", serverUrl));
                } else {
                    return FormValidation.error("Failed to connect to cluster");
                }
            } catch (GeneralSecurityException | IOException e) {
                LOGGER.log(Level.SEVERE, "failed when do verify connect", e);
                return FormValidation.error(String.format("Failed to connect to Cluster: %s", e.getMessage()));
            }
        }

        public ListBoxModel doFillCredentialsIdItems(
                @QueryParameter String credentialsId) {
            // It is valid to choose no default credential, so enable
            // 'includeEmpty'
            return ClusterConfig.doFillCredentialsIdItems(credentialsId);
        }

    }

    /**
     * A very simple HTTP-Based client that only used to test connection availability.
     */
    private static class SimpleKubernetesAvailabilityTestClient {
        private String serverUrl;
        private String defaultProject;
        private boolean skipTlsVerify;
        private String serverCertificate;
        private String token;

        SimpleKubernetesAvailabilityTestClient(String serverUrl, boolean skipTlsVerify, String serverCertificate, String token) {
            this.serverUrl = serverUrl;
            this.defaultProject = defaultProject;
            this.skipTlsVerify = skipTlsVerify;
            this.serverCertificate = serverCertificate;
            this.token = token;
        }

        /**
         * Test connection availability
         *
         * @return true if the connection is available
         */
        boolean testConnection() throws GeneralSecurityException, IOException {
            OkHttpClient client;

            if (skipTlsVerify) {
                client = insecureHttpClient();
            } else {
                client = customCAHttpClient(serverCertificate);
            }

            serverUrl = serverUrl.endsWith("/") ? serverUrl : serverUrl + "/";
            String wholeUrl = serverUrl + "api/v1/namespaces";

            Request req = new Request.Builder()
                    .url(wholeUrl)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            Response res = client.newCall(req).execute();

            if (res.body() != null) {
                String jsonStr = res.body().string();
                try {
                    JSONObject jsonObject = JSONObject.fromObject(jsonStr);
                    if (jsonObject == null) {
                        return false;
                    }

                    return jsonObject.getString("kind").equals("NamespaceList");
                } catch (JSONException e) {
                    LOGGER.log(Level.SEVERE, "cannot parse json from " + jsonStr);
                    throw e;
                }
            }
            return false;
        }

        /**
         * Create an OKHttpClient which trust the self-signed certificate.
         *
         * @return OKHttpClient
         */
        private OkHttpClient insecureHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
            X509TrustManager acceptAllTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    if(x509Certificates == null) {
                        throw new CertificateException();
                    }
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    if(x509Certificates == null) {
                        throw new CertificateException();
                    }
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            SSLSocketFactory sslSocketFactory;

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{acceptAllTrustManager}, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, acceptAllTrustManager)
                    .hostnameVerifier((h, s) -> true)
                    .build();
        }

        /**
         * Create a OKHttpClient which trust the custom certificate
         *
         * @param serverCertificate certificate inputted by user
         * @return OKHttpClient
         */
        private OkHttpClient customCAHttpClient(String serverCertificate) throws IOException, GeneralSecurityException {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

            if (StringUtils.isEmpty(serverCertificate)) {
                return clientBuilder.build();
            }

            Buffer buffer = new Buffer();
            if (Files.exists(Paths.get(serverCertificate))) {
                buffer.write(Files.readAllBytes(Paths.get(serverCertificate)));
            } else {
                buffer.writeUtf8(serverCertificate);
            }

            X509TrustManager trustManager = trustManagerForCertificates(buffer.inputStream());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return clientBuilder
                    .sslSocketFactory(sslSocketFactory, trustManager)
                    .build();

        }

        /**
         * Create trust manager which trust the certificates.
         *
         * @param in InputStream to read the certificates
         * @return TrustManager which trust the certificates
         */
        private X509TrustManager trustManagerForCertificates(InputStream in)
                throws GeneralSecurityException {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }

            // Put the certificates a key store.
            char[] password = ("" + System.currentTimeMillis()).toCharArray(); // Any password will work.
            KeyStore keyStore = newEmptyKeyStore(password);
            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }

            // Use it to build an X509 trust manager.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        }

        private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
            try {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                InputStream in = null; // By convention, 'null' creates an empty key store.
                keyStore.load(in, password);
                return keyStore;
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }
}
