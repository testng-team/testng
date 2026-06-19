package test.inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Github1298Test {
  @BeforeMethod
  public void setUp(ITestResult tr) {
    tr.setAttribute("class", getClass().getName());
    tr.setAttribute("toString", tr.toString());
  }

  @Test
  public void testPlugin() {
    ITestResult result = Reporter.getCurrentTestResult();
    assertThat(result.getAttribute("class")).hasToString(getClass().getName());
    assertThat(result.getAttribute("toString")).isNotNull();
  }
}
