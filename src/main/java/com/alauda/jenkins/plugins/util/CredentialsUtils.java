package com.alauda.jenkins.plugins.util;

import com.alauda.jenkins.plugins.DevopsTokenCredentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.security.GeneralSecurityException;
import java.util.Collections;

public class CredentialsUtils {

    public static String getToken(String credentialId) throws GeneralSecurityException {
        if (StringUtils.isEmpty(credentialId)) {
            return "";
        }

        DevopsTokenCredentials secretCredentials =
                CredentialsMatchers.firstOrNull(
                        CredentialsProvider.lookupCredentials(DevopsTokenCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections.emptyList()),
                        CredentialsMatchers.withId(credentialId));

        if (secretCredentials == null) {
            throw new GeneralSecurityException(String.format("Credential with id %s not found", credentialId));
        }

        return secretCredentials.getToken();
    }

}
