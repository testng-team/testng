package test.retryAnalyzer;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.util.RetryAnalyzerCount;

import test.SimpleBaseTest;

/**
 * retryAnalyzer parameter unit tests.
 * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
 *
 */
public final class RetryAnalyzerTest extends SimpleBaseTest {

  @Test
  public void testAnnotation() {
    TestNG tng = create(RetryTestAnalyzer.NoFail.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(RetryTestAnalyzer.NoFail.COUNT, 1);
    Assert.assertEquals(tla.getPassedTests().size(), 1);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }

  @Test
  public void testAnnotationWithOneRetry() {
    TestNG tng = create(RetryTestAnalyzer.FailOnce.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(RetryTestAnalyzer.FailOnce.COUNT, 2);
    Assert.assertEquals(tla.getPassedTests().size(), 1);
    Assert.assertEquals(tla.getFailedTests().size(), 1);
  }

  @Test
  public void testAnnotationWithDataProvider() {
    TestNG tng = create(RetryTestAnalyzer.FourTestsAndSecondFailOnce.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(RetryTestAnalyzer.FourTestsAndSecondFailOnce.COUNT, 5);
    Assert.assertEquals(RetryTestAnalyzer.FourTestsAndSecondFailOnce.DATA_PROVIDER_COUNT, 2);
    Assert.assertEquals(tla.getPassedTests().size(), 4);
    Assert.assertEquals(tla.getFailedTests().size(), 1);
  }

  @Test
  public void withFactory() {
    TestNG tng = create(MyFactory.class);
    tng.run();

    Assert.assertEquals(FactoryTest.COUNT, 4);
  }
}
