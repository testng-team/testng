package test.annotationtransformer;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class MySuiteListener implements ISuiteListener {

  public static boolean triggered;

  @Override
  public void onStart(ISuite suite) {
    triggered = true;
  }
}
