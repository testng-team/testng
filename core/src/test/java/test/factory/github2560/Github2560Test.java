package test.factory.github2560;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class Github2560Test extends SimpleBaseTest {

    @Test
    public void staticFactory() {
        TestNG testng = create(FactoryTest.class);
        testng.setDefaultSuiteName("Static @Factory tests");
        InvokedMethodListener invokedMethodListener = new InvokedMethodListener();
        testng.addListener(invokedMethodListener);

        testng.run();

        ImmutableMap<Integer, ImmutableList<String>> expected = ImmutableMap.of(
                0, ImmutableList.of("beforeClass", "beforeMethod", "test", "afterMethod", "afterClass"),
                1, ImmutableList.of("beforeClass", "beforeMethod", "test", "afterMethod", "afterClass"),
                2, ImmutableList.of("beforeClass", "beforeMethod", "test", "afterMethod", "afterClass")
        );
        Assert.assertEquals(invokedMethodListener.capturedBeforeInvocations, expected, "beforeInvocation");
        Assert.assertEquals(invokedMethodListener.capturedAfterInvocations, expected, "afterInvocation");
    }

    @Test
    public void constructorFactory() {
        TestNG testng = create(ConstructorTest.class);
        testng.setDefaultSuiteName("Constructor @Factory tests");
        InvokedMethodListener invokedMethodListener = new InvokedMethodListener();
        testng.addListener(invokedMethodListener);

        testng.run();

        ImmutableMap<Integer, ImmutableList<String>> expected = ImmutableMap.of(
                0, ImmutableList.of("beforeClass", "beforeMethod", "test", "afterMethod", "afterClass"),
                1, ImmutableList.of("beforeClass", "beforeMethod", "test", "afterMethod", "afterClass"),
                2, ImmutableList.of("beforeClass", "beforeMethod", "test", "afterMethod", "afterClass")
        );
        Assert.assertEquals(invokedMethodListener.capturedBeforeInvocations, expected, "beforeInvocation");
        Assert.assertEquals(invokedMethodListener.capturedAfterInvocations, expected, "afterInvocation");
    }
}
