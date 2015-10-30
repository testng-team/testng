package test.name;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class NameTest extends SimpleBaseTest {

  @Test
  public void itest_test() {
    TestNG tng = create(SimpleITestSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "NAME");
    Assert.assertEquals(result.getTestName(), "NAME");
  }

  @Test
  public void testName_test() {
    TestNG tng = create(NameSample.class);
    TestListenerAdapter adapter = new TestListenerAdapter();
    tng.addListener(adapter);

    tng.run();

    Assert.assertTrue(adapter.getFailedTests().isEmpty());
    Assert.assertTrue(adapter.getSkippedTests().isEmpty());
    Assert.assertEquals(adapter.getPassedTests().size(), 1);
    ITestResult result = adapter.getPassedTests().get(0);
    Assert.assertEquals(result.getMethod().getMethodName(), "test");
    Assert.assertEquals(result.getName(), "NAME");
    Assert.assertEquals(result.getTestName(), "NAME");
  }
}
