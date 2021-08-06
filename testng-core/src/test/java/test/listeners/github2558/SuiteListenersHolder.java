package test.listeners.github2558;

import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteListenersHolder {

  public static class SuiteListenerA implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
      CallHolder.addCall(getClass().getName() + ".onStart()");
    }

    @Override
    public void onFinish(ISuite suite) {
      CallHolder.addCall(getClass().getName() + ".onFinish()");
    }
  }

  public static class SuiteListenerB implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
      CallHolder.addCall(getClass().getName() + ".onStart()");
    }

    @Override
    public void onFinish(ISuite suite) {
      CallHolder.addCall(getClass().getName() + ".onFinish()");
    }
  }
}
