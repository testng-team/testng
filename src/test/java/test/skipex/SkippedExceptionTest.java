package test.skipex;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.util.List;


/**
 * This class/interface
 */
public class SkippedExceptionTest {
  @Test
  public void skippedExceptionInConfigurationMethods() {
    TestListenerAdapter listener= new TestListenerAdapter();
    TestNG test= new TestNG(false);
    test.addListener(listener);
    test.setVerbose(0);
    test.setTestClasses(new Class[] {ConfigurationSkippedExceptionTest.class});
    test.run();
    List<ITestResult> confSkips= listener.getConfigurationSkips();
    List<ITestResult> testSkips= listener.getSkippedTests();
    Assert.assertEquals(testSkips.size(), 1);
    Assert.assertEquals(testSkips.get(0).getMethod().getMethodName(), "dummyTest");

    Assert.assertEquals(confSkips.size(), 1);
    Assert.assertEquals(confSkips.get(0).getMethod().getMethodName(), "configurationLevelSkipException");
  }


  @Test
  public void skippedExceptionInTestMethods() {
    TestListenerAdapter listener= new TestListenerAdapter();
    TestNG test= new TestNG(false);
    test.addListener(listener);
    test.setTestClasses(new Class[] {TestSkippedExceptionTest.class});
    test.run();
    List<ITestResult> skips= listener.getSkippedTests();
    List<ITestResult> failures= listener.getFailedTests();
    List<ITestResult> passed = listener.getPassedTests();
    Assert.assertEquals(skips.size(), 1);
    Assert.assertEquals(failures.size(), 1);
    Assert.assertEquals(passed.size(), 1);
    Assert.assertEquals(skips.get(0).getMethod().getMethodName(), "genericSkipException");
    Assert.assertEquals(failures.get(0).getMethod().getMethodName(), "timedSkipException");
    Assert.assertEquals(passed.get(0).getMethod().getMethodName(), "genericExpectedSkipException");
  }
}
