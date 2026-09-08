package org.testng.skip.github1632;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.skip.samples.github1632.AfterListenerSkipSample;
import org.testng.skip.samples.github1632.BeforeListenerSkipSample;
import org.testng.skip.samples.github1632.ListenerMarksMethodAsSkippedSample;
import org.testng.skip.samples.github1632.NoConfigAfterListenerSample;
import org.testng.skip.samples.github1632.NoConfigBeforeListenerSample;
import org.testng.skip.samples.github1632.SkipMonitoringListener;
import org.testng.skip.samples.github1632.TestClassSample;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1632")
  public void runTest() {
    TestNG testng = create(TestClassSample.class);
    SkipMonitoringListener listener = new SkipMonitoringListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getStatus().get("skippingMethod")).isEqualTo(ITestResult.SKIP);
    assertThat(listener.getStatus().get("passingMethod")).isEqualTo(ITestResult.SUCCESS);
    assertThat(listener.getStatus().get("failingMethod")).isEqualTo(ITestResult.FAILURE);
    assertThat(listener.getStatus().get("anotherFailingMethod")).isEqualTo(ITestResult.FAILURE);
  }

  @Test(description = "GITHUB-1632")
  public void runBeforeListenerSkipSample() {
    InvokedMethodNameListener listener = run(BeforeListenerSkipSample.class);
    assertThat(listener.getResult("shouldNotBeExecuted").getStatus()).isEqualTo(ITestResult.SKIP);
  }

  @Test(description = "GITHUB-1632")
  public void runNoConfigBeforeListenerSample() {
    InvokedMethodNameListener listener = run(NoConfigBeforeListenerSample.class);
    assertThat(listener.getResult("shouldNotBeExecuted").getStatus()).isEqualTo(ITestResult.SKIP);
  }

  @Test(description = "GITHUB-1632")
  public void runAfterListenerSkipSample() {
    InvokedMethodNameListener listener = run(AfterListenerSkipSample.class);
    assertThat(listener.getResult("shouldNotBeExecuted").getStatus()).isEqualTo(ITestResult.SKIP);
  }

  @Test(description = "GITHUB-1632")
  public void runNoConfigAfterListenerSample() {
    InvokedMethodNameListener listener = run(NoConfigAfterListenerSample.class);
    assertThat(listener.getResult("shouldNotBeExecuted").getStatus()).isEqualTo(ITestResult.SKIP);
  }

  @Test(description = "GITHUB-1632")
  public void runBeforeListenerSkipSample2() {
    InvokedMethodNameListener listener = run(ListenerMarksMethodAsSkippedSample.class);
    assertThat(listener.getResult("shouldNotBeExecuted").getStatus()).isEqualTo(ITestResult.SKIP);
  }
}
