package test.listeners.issue2055;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestClassSample.class)
public class TestClassSample implements ITestListener, ISuiteListener {

  @Override
  public void onStart(ISuite suite) {
    suite.addListener(new DynamicTestListener());
  }

  @Test
  public void testMethod() {}
}
