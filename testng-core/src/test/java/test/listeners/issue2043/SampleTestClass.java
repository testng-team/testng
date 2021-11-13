package test.listeners.issue2043;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.listeners.issue2043.listeners.FailFastListener;

@Listeners({SampleTestClass.class})
public class SampleTestClass implements ITestListener, ISuiteListener {

  @Override
  public void onStart(ISuite suite) {
    suite.addListener(new FailFastListener());
  }

  @BeforeClass
  public void beforeClass() {}

  @Test(dataProviderClass = SampleDataProvider.class, dataProvider = "dp1master")
  public <T extends Object> void test1(Class<T> clazz) {}

  @Test(dataProviderClass = SampleDataProvider.class, dataProvider = "dp1master")
  public <T extends Object> void test2(Class<T> clazz) {}
}
