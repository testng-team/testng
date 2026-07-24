package test.listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteListener implements ISuiteListener {
  public static int start;
  public static int finish;

  @Override
  public void onFinish(ISuite suite) {
    finish++;
  }

  @Override
  public void onStart(ISuite suite) {
    start++;
  }
}
