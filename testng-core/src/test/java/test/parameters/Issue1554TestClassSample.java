package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class Issue1554TestClassSample {
  private String browser;
  private ITestContext context;

  @Parameters({"browser"})
  @BeforeTest
  public void setUpTest(String browser, ITestContext context) {
    this.browser = browser;
    this.context = context;
  }

  @Test()
  public void aTest() {
    assertThat(browser).isNotNull();
    assertThat(context).isNotNull();
  }
}
