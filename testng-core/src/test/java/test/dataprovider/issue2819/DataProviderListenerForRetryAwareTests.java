package test.dataprovider.issue2819;

import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class DataProviderListenerForRetryAwareTests implements IDataProviderListener {

  private int beforeInvocations = 0;
  private int afterInvocations = 0;
  private int failureInvocations = 0;

  public int getBeforeInvocations() {
    return beforeInvocations;
  }

  public int getFailureInvocations() {
    return failureInvocations;
  }

  public int getAfterInvocations() {
    return afterInvocations;
  }

  @Override
  public void beforeDataProviderExecution(
      IDataProviderMethod dp, ITestNGMethod tm, ITestContext ctx) {
    beforeInvocations++;
  }

  @Override
  public void afterDataProviderExecution(
      IDataProviderMethod dp, ITestNGMethod tm, ITestContext ctx) {
    afterInvocations++;
  }

  @Override
  public void onDataProviderFailure(ITestNGMethod method, ITestContext ctx, RuntimeException t) {
    failureInvocations++;
  }
}
