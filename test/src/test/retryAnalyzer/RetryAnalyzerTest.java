package test.retryAnalyzer;

import org.testng.Assert;
import org.testng.util.RetryAnalyzerCount;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * retryAnalyzer parameter unit tests.
 * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
 *
 */
public final class RetryAnalyzerTest extends RetryAnalyzerCount {

  private static int r = 1;
  private static int r2 = 1;
  private static int r3 = 1;
  private static int value = 42;

  public RetryAnalyzerTest() {
    setCount(2);
  }

  @Test(retryAnalyzer=RetryAnalyzerTest.class)
  public void testAnnotation() {
    Assert.assertTrue(true);
  }

  @Test(retryAnalyzer=RetryAnalyzerTest.class)
  public void testAnnotationWithOneRetry() {
    if (r == 1) {
      r--;
      Assert.assertTrue(false);
    }
    if (r == 0) {
      Assert.assertTrue(true);
    }
  }

  @DataProvider(name="dataProvider")
  private Object[][] dataProvider() {
    return new Object[][] { { 1, false }, { 0, true }, { 0, true },
        { 1, false } };
  }

  @DataProvider(name="dataProvider2")
  private Object[][] dataProvider2() {
    value = 42;

    return new Object[][] { { true }, { true } };
  }

  @Test(retryAnalyzer=RetryAnalyzerTest.class, dataProvider="dataProvider")
  public void testAnnotationWithDataProvider(int paf, boolean test) {
    if (paf == 1 && test == false) {
      if (r2 >= 1) {
        r2--;
        Assert.assertTrue(false);
      } else if (r2 == 0) {
        Assert.assertTrue(true);
      }
    }
    else if (paf == 0 || test == true) {
      Assert.assertTrue(true);
    }
  }

  @Test(retryAnalyzer=RetryAnalyzerTest.class, dataProvider="dataProvider2")
  public void testAnnotationWithDataProviderAndRecreateParameters(boolean dummy) {
    if (r3 == 1) {
      this.value = 0;
      r3--;
      Assert.assertTrue(false);
    } else if (r3 == 0) {
      Assert.assertEquals(this.value, 42);
    }
  }

  public boolean retryMethod(ITestResult result) {
    return true;
  }
}
