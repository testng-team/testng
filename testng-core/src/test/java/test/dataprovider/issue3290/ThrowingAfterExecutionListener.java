package test.dataprovider.issue3290;

import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

/**
 * A data provider listener that fails in {@link #afterDataProviderExecution}, i.e. after the data
 * provider has been invoked but before TestNG hands the parameters over for consumption. Used to
 * confirm that a resource-backed {@code Stream} is still closed when parameter setup aborts.
 */
public class ThrowingAfterExecutionListener implements IDataProviderListener {

  @Override
  public void afterDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    throw new RuntimeException("Deliberate failure from afterDataProviderExecution");
  }
}
