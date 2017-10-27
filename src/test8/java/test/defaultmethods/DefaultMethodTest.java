package test.defaultmethods;

import org.testng.Assert;
import org.testng.IResultMap;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.listeners.TestAndClassListener;

import java.util.Collection;

public class DefaultMethodTest extends SimpleBaseTest {

    @Test(description = "Test default methods defined in a interface should be run")
    public void testDefaultShouldRun() {
        TestNG tng = create(TestA.class);
        final TestCounter listener = new TestCounter();
        tng.addListener((ITestNGListener) listener);
        tng.run();

        Assert.assertEquals(listener.passedTests.size(), 1);
        final Collection<ITestNGMethod> methods = listener.passedTests.getAllMethods();
        Assert.assertEquals(methods.iterator().next().getMethodName(), "defaultMethodTest");
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

    public static class TestCounter extends TestAndClassListener {

        private IResultMap passedTests;
        private ITestClass testClass;

        @Override
        public void onBeforeClass(ITestClass testClass) {
            this.testClass = testClass;
        }

        @Override
        public void onFinish(ITestContext context) {
            passedTests = context.getPassedTests();
        }
    }

}
