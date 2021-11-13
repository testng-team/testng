package test.configuration.issue2426;

import java.util.HashMap;
import java.util.Map;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;
import org.testng.annotations.*;

public class MyMethodListener implements IConfigurationListener {

  private final Map<Class<?>, Object[]> contents = new HashMap<>();

  @Override
  public void onConfigurationSuccess(ITestResult tr) {
    Object[] values = tr.getMethod().getFactoryMethodParamsInfo().getParameters();
    if (tr.getMethod().isBeforeSuiteConfiguration()) {
      contents.put(BeforeSuite.class, values);
    }
    if (tr.getMethod().isAfterSuiteConfiguration()) {
      contents.put(AfterSuite.class, values);
    }
    if (tr.getMethod().isBeforeTestConfiguration()) {
      contents.put(BeforeTest.class, values);
    }
    if (tr.getMethod().isAfterTestConfiguration()) {
      contents.put(AfterTest.class, values);
    }
    if (tr.getMethod().isBeforeClassConfiguration()) {
      contents.put(BeforeClass.class, values);
    }
    if (tr.getMethod().isAfterClassConfiguration()) {
      contents.put(AfterClass.class, values);
    }
    if (tr.getMethod().isBeforeMethodConfiguration()) {
      contents.put(BeforeMethod.class, values);
    }
    if (tr.getMethod().isAfterMethodConfiguration()) {
      contents.put(AfterMethod.class, values);
    }
  }

  public Map<Class<?>, Object[]> getContents() {
    return contents;
  }
}
