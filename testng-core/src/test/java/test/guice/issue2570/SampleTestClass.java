package test.guice.issue2570;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestClass {

  @Test(retryAnalyzer = GuicePoweredConstructorInjectedRetry.class)
  public void test() {
    fail();
  }

  @Test(retryAnalyzer = GuicePoweredSetterInjectedRetry.class)
  public void anotherTest() {
    fail();
  }

  @Test(dataProvider = "dp", retryAnalyzer = GuicePoweredConstructorInjectedRetryForDPTest.class)
  public void dataDrivenTest(int i) {
    fail();
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
