package test.defaultmethods;

import org.testng.Assert;
import org.testng.ITestClass;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.listeners.TestAndClassListener;

public class DefaultMethodTest extends SimpleBaseTest {

    @Test(description = "Test default methods defined in a interface should be run")
    public void testDefaultShouldRun() {
        TestNG tng = create(TestA.class);
        final TestCounter listener = new TestCounter();
        tng.addListener((ITestNGListener) listener);
        tng.run();

        final ITestNGMethod[] testMethods = listener.testClass.getTestMethods();
        Assert.assertEquals(testMethods.length, 1);
        Assert.assertEquals(testMethods[0].getMethodName(), "defaultMethodTest");
    }

    @Test(description = "Before class default methods defined in a interface should be run")
    public void beforeClassDefaultShouldRun() {
        TestNG tng = create(TestA.class);
        final TestCounter listener = new TestCounter();
        tng.addListener((ITestNGListener) listener);
        tng.run();

        final ITestNGMethod[] beforeClassMethods = listener.testClass.getBeforeClassMethods();
        Assert.assertEquals(beforeClassMethods.length, 1);
        Assert.assertEquals(beforeClassMethods[0].getMethodName(), "beforeClassRun");

    }

    @Test(description = "After class default methods defined in a interface should be run")
    public void afterClassDefaultShouldRun() {
        TestNG tng = create(TestA.class);
        final TestCounter listener = new TestCounter();
        tng.addListener((ITestNGListener) listener);
        tng.run();

        final ITestNGMethod[] beforeClassMethods = listener.testClass.getAfterClassMethods();
        Assert.assertEquals(beforeClassMethods.length, 1);
        Assert.assertEquals(beforeClassMethods[0].getMethodName(), "afterClassRun");
    }

    @Test(description = "Before method default methods defined in a interface should be run")
    public void beforeMethodDefaultShouldRun() {
        TestNG tng = create(TestA.class);
        final TestCounter listener = new TestCounter();
        tng.addListener((ITestNGListener) listener);
        tng.run();

        final ITestNGMethod[] beforeClassMethods = listener.testClass.getBeforeTestMethods();
        Assert.assertEquals(beforeClassMethods.length, 1);
        Assert.assertEquals(beforeClassMethods[0].getMethodName(), "beforeMethodRun");
    }

    @Test(description = "After method default methods defined in a interface should be run")
    public void afterMethodDefaultShouldRun() {
        TestNG tng = create(TestA.class);
        final TestCounter listener = new TestCounter();
        tng.addListener((ITestNGListener) listener);
        tng.run();

        final ITestNGMethod[] beforeClassMethods = listener.testClass.getAfterTestMethods();
        Assert.assertEquals(beforeClassMethods.length, 1);
        Assert.assertEquals(beforeClassMethods[0].getMethodName(), "afterMethodRun");
    }

    public static class TestCounter extends TestAndClassListener {

        private ITestClass testClass;

        @Override
        public void onBeforeClass(ITestClass testClass) {
            this.testClass = testClass;
        }

    }

}
