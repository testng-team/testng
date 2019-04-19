package test.listeners.issue2061;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class DynamicSuiteListener implements ISuiteListener {

  @Override
  public void onStart(ISuite suite) {
    suite.addListener(new DynamicTestListener());
  }
}
