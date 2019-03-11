package com.alauda.jenkins.plugins.pipeline;

import org.junit.Assert;
import org.junit.Test;

public class Base64DecodeStepTest {


    @Test
    public void testBase64DecodeStepExecution() throws Exception {
        Base64DecodeStep step = new Base64DecodeStep("YWJjZGVmZw==");
        String decodedString = ((Base64DecodeStep.Execution)step.start(null)).run();

        Assert.assertEquals("abcdefg", decodedString);
    }

}
