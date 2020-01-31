package test.listeners.issue1632;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
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

}
