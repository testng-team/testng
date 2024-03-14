package test.factory.issue3079;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.internal.IInstanceIdentity;

public class FactoryTestCase {

  public static Map<UUID, Set<Object>> objectMap = new ConcurrentHashMap<>();

  @Factory(dataProvider = "dp")
  public FactoryTestCase(int ignored) {}

  @DataProvider(parallel = true)
  public static Object[][] dp() {
    return IntStream.rangeClosed(1, 100)
        .boxed()
        .map(it -> new Object[] {it})
        .toArray(size -> new Object[size][1]);
  }

  @BeforeClass
  public void beforeClass() {
    record();
  }

  @BeforeMethod
  public void beforeMethod() {
    record();
  }

  @Test(dataProvider = "dp")
  public void t1(int ignored) {
    record();
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
