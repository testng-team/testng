package test.github799;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class MethodsTestSample {
  @Test
  public void angry() {
    String methodName = Reporter.getCurrentTestResult().getMethod().getMethodName();
    Reporter.log(methodName);
    assertThat(methodName).isNotNull();
  }

  @Test
  public void birds() {
    String methodName = Reporter.getCurrentTestResult().getMethod().getMethodName();
    Reporter.log(methodName);
    assertThat(methodName).isNotNull();
  }

  @Test
  public void android() {
    String methodName = Reporter.getCurrentTestResult().getMethod().getMethodName();
    Reporter.log(methodName);
    assertThat(methodName).isNotNull();
  }
}
