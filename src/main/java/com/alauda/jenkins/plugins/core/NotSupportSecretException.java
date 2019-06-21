package com.alauda.jenkins.plugins.core;

/**
 * Represent the case of not supporting a kind of secret
 */
public class NotSupportSecretException extends RuntimeException {
    public NotSupportSecretException(String type) {
        super(String.format("Not support secret type: %s", type));
    }
}
