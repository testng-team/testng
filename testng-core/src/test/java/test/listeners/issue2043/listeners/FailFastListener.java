package test.listeners.issue2043.listeners;

import java.util.Set;
import org.testng.IConfigurationListener;
import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Sets;

public class FailFastListener
    implements IInvokedMethodListener, IConfigurationListener, IDataProviderListener {
  public static final Set<String> msgs = Sets.newHashSet();

  @Override
  public synchronized void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    msgs.add(getClass().getSimpleName() + ":afterInvocation");
  }

  @Override
  public void beforeConfiguration(ITestResult testResult) {
    msgs.add(getClass().getSimpleName() + ":beforeConfiguration");
  }

  @Override
  public void beforeDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    msgs.add(getClass().getSimpleName() + ":beforeDataProviderExecution");
  }
}
