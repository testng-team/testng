package test.configuration;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import junit.framework.Assert;

public class BeforeClassWithDisabledTest extends SimpleBaseTest {

    @Test
    public void afterClassShouldRunEvenWithDisabledMethods() {
        TestNG tng = create(new Class[] { ConfigurationDisabledSampleTest.class });
        Assert.assertFalse(ConfigurationDisabledSampleTest.m_afterWasRun);
        tng.run();
        Assert.assertTrue(ConfigurationDisabledSampleTest.m_afterWasRun);
    }
}


