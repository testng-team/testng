package test.guice.issue2570;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestClass {

  @Test(retryAnalyzer = GuicePoweredConstructorInjectedRetry.class)
  public void test() {
    Assert.fail();
  }

  @Test(retryAnalyzer = GuicePoweredSetterInjectedRetry.class)
  public void anotherTest() {
    Assert.fail();
  }

  @Test(dataProvider = "dp", retryAnalyzer = GuicePoweredConstructorInjectedRetryForDPTest.class)
  public void dataDrivenTest(int i) {
    Assert.fail();
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
