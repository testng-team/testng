package test.listeners;

import java.util.Locale;
import org.testng.IExecutionListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class ExecutionListenerAndSuiteListener implements ISuiteListener, IExecutionListener {

  private String testString;
  private static String tmp;

  @Override
  public void onExecutionStart() {
    testString = "initialized";
  }

  @Override
  public void onExecutionFinish() {}

  @Override
  public void onStart(ISuite suite) {
    tmp = testString.toUpperCase(Locale.ROOT);
  }

  @Override
  public void onFinish(ISuite suite) {}

  public static String getTmpString() {
    return tmp;
  }
}
