package org.testng.internal.paramhandler;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ParameterizedSampleTestClass {
    @Test
    @Parameters("foo")
    public void testMethod(String foo) {}
}
