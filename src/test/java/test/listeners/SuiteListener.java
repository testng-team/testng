package test.listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteListener implements ISuiteListener {
  public static boolean start = false;
  public static boolean finish = false;

  @Override
  public void onFinish(ISuite suite) {
    finish = true;
  }

  @Override
  public void onStart(ISuite suite) {
    start = true;
  }

}
