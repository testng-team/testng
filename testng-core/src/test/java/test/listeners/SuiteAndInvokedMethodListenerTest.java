package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.listeners.SuiteAndInvokedMethodListenerTest.MyListener;

/**
 * Make sure that if a listener implements both IInvokedMethodListener and ISuiteListener, both
 * listeners get invoked.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
@Listeners(MyListener.class)
public class SuiteAndInvokedMethodListenerTest {
  public static class MyListener implements IInvokedMethodListener, ISuiteListener {

    private static boolean m_before = false;
    private static boolean m_start = false;

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult result) {
      m_before = true;
    }

    @Override
    public void onStart(ISuite suite) {
      m_start = true;
    }
  }

  @Test
  public void bothListenersShouldRun() {
    assertThat(MyListener.m_before)
        .withFailMessage("IInvokedMethodListener was not invoked")
        .isTrue();
    assertThat(MyListener.m_start).withFailMessage("ISuiteListener was not invoked").isTrue();
  }
}
