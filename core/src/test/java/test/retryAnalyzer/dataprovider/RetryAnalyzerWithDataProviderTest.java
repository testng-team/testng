package test.retryAnalyzer.dataprovider;

import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class RetryAnalyzerWithDataProviderTest extends SimpleBaseTest {
  private static int countWithStringArray = 3;
  private static int countWithObjectAndStringArray = 3;
  private static int countWithSingleParam = 3;
  private static int countWithoutDataProvider = 3;

  @Test
  public void testRetryCounts() {
    TestNG tng = create(RetryCountTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);

    tng.run();

    assertEquals(tla.getPassedTests().size(), 1);
    assertEquals(tla.getPassedTests().get(0).getParameters(), new String[]{"c"});

    assertEquals(tla.getFailedTests().size(), 3);
    assertEquals(tla.getSkippedTests().size(), 9);
  }

  @DataProvider(name = "getTestData")
  public Object[][] getTestData() {
    return new String[][]{
            new String[]{"abc1", "cdf1"}};
  }

  @Test(dataProvider = "getTestData", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void testWithStringArray(String... values) {
    System.out.println("Test Run " + values);
    Assert.assertTrue(countWithStringArray-- == 0);
  }

  @DataProvider(name = "getObjectData")
  public Object[][] getObjectData() {
    return new Object[][]{
            new Object[]{false, "abc1", "cdf1"}};
  }

  @Test(dataProvider = "getObjectData", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void testWithObjectAndStringArray(boolean flag, String... values) {
    System.out.println("Test Run " + values + " with flag: " + flag);
    Assert.assertTrue(countWithObjectAndStringArray-- == 0);
  }

  @DataProvider(name = "getSingleParam", parallel = true)
  public Object[][] getSingleParam() {
    return new String[][]{
            new String[]{"abc"},
            new String[]{"bcd"}};
  }

  @Test(dataProvider = "getSingleParam", retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void testWithSingleParam(String value) {
    System.out.println("Test Run " + value);
    Assert.assertTrue(countWithSingleParam-- <= 0);
  }

  @Test(retryAnalyzer = DataProviderRetryAnalyzer.class)
  public void testWithoutDataProvider() {
    Assert.assertTrue(countWithoutDataProvider-- == 0);
  }

}
