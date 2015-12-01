package test.annotationtransformer;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class MySuiteListener2 implements ISuiteListener {

  public static boolean triggered = false;

  @Override
  public void onStart(ISuite suite) {
    triggered = true;
  }

  @Override
  public void onFinish(ISuite suite) {}
}
