package test.skipex;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

/** This class/interface */
public class SkippedExceptionTest {
  @Test
  public void skippedExceptionInConfigurationMethods() {
    TestListenerAdapter listener = new TestListenerAdapter();
    TestNG test = new TestNG(false);
    test.addListener((ITestNGListener) listener);
    test.setTestClasses(new Class[] {ConfigurationSkippedExceptionTest.class});
    test.run();
    List<ITestResult> confSkips = listener.getConfigurationSkips();
    List<ITestResult> testSkips = listener.getSkippedTests();
    assertThat(testSkips.size()).isEqualTo(1);
    assertThat(testSkips.get(0).getMethod().getMethodName()).isEqualTo("dummyTest");

    assertThat(confSkips.size()).isEqualTo(1);
    assertThat(confSkips.get(0).getMethod().getMethodName())
        .isEqualTo("configurationLevelSkipException");
  }

  @Test
  public void skippedExceptionInTestMethods() {
    TestListenerAdapter listener = new TestListenerAdapter();
    TestNG test = new TestNG(false);
    test.addListener(listener);
    test.setTestClasses(new Class[] {TestSkippedExceptionTest.class});
    test.run();
    List<ITestResult> skips = listener.getSkippedTests();
    List<ITestResult> failures = listener.getFailedTests();
    List<ITestResult> passed = listener.getPassedTests();
    assertThat(skips.size()).isEqualTo(1);
    assertThat(failures.size()).isEqualTo(1);
    assertThat(passed.size()).isEqualTo(1);
    assertThat(skips.get(0).getMethod().getMethodName()).isEqualTo("genericSkipException");
    assertThat(failures.get(0).getMethod().getMethodName()).isEqualTo("timedSkipException");
    assertThat(passed.get(0).getMethod().getMethodName()).isEqualTo("genericExpectedSkipException");
  }
}
