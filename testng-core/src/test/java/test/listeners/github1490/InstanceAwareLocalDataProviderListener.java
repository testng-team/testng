package test.listeners.github1490;

import java.util.HashSet;
import java.util.Set;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

public class InstanceAwareLocalDataProviderListener extends LocalDataProviderListener {
  public static Set<Object> instanceCollectionBeforeExecution = new HashSet<>();
  public static Set<Object> instanceCollectionAfterExecution = new HashSet<>();

  public InstanceAwareLocalDataProviderListener() {
    instanceCollectionBeforeExecution.clear();
    instanceCollectionAfterExecution.clear();
  }

  @Override
  public void beforeDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    super.beforeDataProviderExecution(dataProviderMethod, method, iTestContext);
    if (dataProviderMethod.getInstance() != null) {
      instanceCollectionBeforeExecution.add(dataProviderMethod.getInstance());
    }
  }

  @Override
  public void afterDataProviderExecution(
      IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
    super.afterDataProviderExecution(dataProviderMethod, method, iTestContext);
    if (dataProviderMethod.getInstance() != null) {
      instanceCollectionAfterExecution.add(dataProviderMethod.getInstance());
    }
  }
}
