package test.retryAnalyzer.github857;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;

@Listeners(GitHub857Listener.class)
public class GitHub857Sample {

  @Test(retryAnalyzer = GitHub857Retry.class)
  public void test() {
    fail("Failing");
  }
}
