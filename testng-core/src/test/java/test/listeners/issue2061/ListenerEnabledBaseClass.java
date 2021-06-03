package test.listeners.issue2061;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.annotations.Listeners;

@Listeners(ListenerEnabledBaseClass.class)
public class ListenerEnabledBaseClass implements ITestListener, ISuiteListener {

  @Override
  public void onStart(ISuite suite) {
    suite.addListener(new DynamicSuiteListener());
  }
}
