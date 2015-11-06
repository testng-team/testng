package test.retryAnalyzer.github857;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;

@Listeners(GitHub857Listener.class)
public class GitHub857Sample {

  @BeforeTest(alwaysRun = true)
  public void beforeTest(ITestContext context) {
    for (ITestNGMethod method : context.getAllTestMethods()) {
      method.setRetryAnalyzer(new GitHub857Retry());
    }
  }

  @Test
  public void test() {
    fail("Failing");
  }
}
