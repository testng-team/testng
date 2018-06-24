package test.inject;

import org.testng.Assert;
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
    Assert.assertEquals(result.getAttribute("class").toString(), getClass().getName());
    Assert.assertNotNull(result.getAttribute("toString"));
  }
}
