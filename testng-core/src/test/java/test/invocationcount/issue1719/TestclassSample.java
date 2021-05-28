package test.invocationcount.issue1719;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestclassSample {

  @Test(successPercentage = 0, dataProvider = "dp")
  public void dataDrivenTestMethod(int i) {
    Assert.fail("Failing iteration:" + i);
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }

  @Test(successPercentage = 0)
  public void simpleTestMethod() {
    Assert.fail();
  }

  @Test(successPercentage = 0, invocationCount = 2)
  public void testMethodWithMultipleInvocations() {
    Assert.fail();
  }
}
