package test.listeners.github2558;

import org.testng.ITestContext;
import org.testng.ITestListener;

public class TestListenersHolder {

  public static class TestListenerA implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
      CallHolder.addCall(getClass().getName() + ".onStart(ctx)");
    }

    @Override
    public void onFinish(ITestContext context) {
      CallHolder.addCall(getClass().getName() + ".onFinish(ctx)");
    }
  }

  public static class TestListenerB implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
      CallHolder.addCall(getClass().getName() + ".onStart(ctx)");
    }

    @Override
    public void onFinish(ITestContext context) {
      CallHolder.addCall(getClass().getName() + ".onFinish(ctx)");
    }
  }
}
