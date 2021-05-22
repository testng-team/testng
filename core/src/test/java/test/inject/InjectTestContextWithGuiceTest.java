package test.inject;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.CurrentTestClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

@Guice
public class InjectTestContextWithGuiceTest {
    @Inject
    public ITestContext testContext;

    @Inject
    @CurrentTestClass
    public Class<?> currentClass;

    @Inject
    @CurrentTestClass
    @SuppressWarnings("rawtypes")
    public Class currentClassRaw;

    @Test
    public void verifyTestContext() {
        Assert.assertNotNull(testContext, "testContext");
    }

    @Test
    public void verifyGenericTestClass() {
        Assert.assertNotNull(currentClass, "currentClass");
    }

    @Test
    public void verifyRawTestClass() {
        Assert.assertNotNull(currentClassRaw, "currentClassRaw");
    }
}
