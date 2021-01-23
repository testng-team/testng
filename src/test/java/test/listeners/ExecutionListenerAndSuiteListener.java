package test.listeners;

import org.testng.IExecutionListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class ExecutionListenerAndSuiteListener implements ISuiteListener, IExecutionListener {

  private static String tmp;
  private String testString;

  public static String getTmpString() {
    return tmp;
  }

  @Override
  public void onExecutionStart() {
    testString = "initialized";
  }

  @Override
  public void onExecutionFinish() {
  }

  @Override
  public void onStart(ISuite suite) {
    tmp = testString.toUpperCase();
  }

  @Override
  public void onFinish(ISuite suite) {
  }

}
