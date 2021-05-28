package test.listeners;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.listeners.SuiteAndConfigurationListenerTest.MyListener;

/**
 * Check that if a listener implements IConfigurationListener additionally to
 * ISuiteListener, ISuiteListener gets invoked exactly once.
 *
 * @author Mihails Volkovs
 */
@Listeners(MyListener.class)
public class SuiteAndConfigurationListenerTest {
  public static class MyListener implements ISuiteListener, IConfigurationListener {

    private static volatile AtomicInteger started = new AtomicInteger(0);

    public MyListener() {
    }

    @Override
    public void onStart(ISuite suite) {
      started.incrementAndGet();
    }

    @Override
    public void onFinish(ISuite suite) {
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
    }

  }

  @Test
  public void bothListenersShouldRun() {
    Assert.assertEquals(MyListener.started.get(), 1, "ISuiteListener was not invoked exactly once:");
  }

}
