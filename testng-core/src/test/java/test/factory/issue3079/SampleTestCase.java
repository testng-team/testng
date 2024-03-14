package test.factory.issue3079;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.IInstanceIdentity;

public class SampleTestCase {
  public static Map<UUID, Set<Object>> objectMap = new ConcurrentHashMap<>();

  @BeforeClass
  public void beforeClass() {
    record();
  }

  @BeforeMethod
  public void beforeMethod() {
    record();
  }

  @Test
  public void test1() {
    record();
  }

  @Test
  public void test2() {
    record();
  }

  @Test(dataProvider = "dp")
  public void test3(int i) {
    record();
  }

  @DataProvider(name = "dp", parallel = true)
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }

  @AfterMethod
  public void afterMethod() {
    record();
  }

  @AfterClass
  public void afterClass() {
    record();
  }

  private static void record() {
    ITestResult itr = Reporter.getCurrentTestResult();
    ITestNGMethod itm = itr.getMethod();
    objectMap
        .computeIfAbsent(
            (UUID) IInstanceIdentity.getInstanceId(itm), k -> ConcurrentHashMap.newKeySet())
        .add(itm.getInstance());
  }
}
