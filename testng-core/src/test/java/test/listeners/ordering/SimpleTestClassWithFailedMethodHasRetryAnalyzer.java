package test.listeners.ordering;

import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class SimpleTestClassWithFailedMethodHasRetryAnalyzer {

  @Test(retryAnalyzer = OnceMore.class)
  public void testWillFail() {
    Assert.fail();
  }

  public static class OnceMore implements IRetryAnalyzer {
    private int counter = 1;

    @Override
    public boolean retry(ITestResult result) {
      return counter++ != 2;
    }
  }
}
