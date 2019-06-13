package com.alauda.jenkins.plugins.core;

public class NotSupportAuthException extends RuntimeException {
    public NotSupportAuthException(String kind) {
        super(String.format("Not support auth kind: %s", kind));
    }
}
