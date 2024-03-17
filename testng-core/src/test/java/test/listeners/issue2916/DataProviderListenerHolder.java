package test.listeners.issue2916;

import java.util.List;
import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;

public class DataProviderListenerHolder {

  private static final String PREFIX = DataProviderListenerHolder.class.getName() + "$";

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.beforeDataProviderExecution_getData",
        "MasterShifu.beforeDataProviderExecution_getData",
        "DragonWarrior.beforeDataProviderExecution_getData",
        "MasterOogway.afterDataProviderExecution_getData",
        "MasterShifu.afterDataProviderExecution_getData",
        "DragonWarrior.afterDataProviderExecution_getData",
        "MasterOogway.beforeDataProviderExecution_failingDataProvider",
        "MasterShifu.beforeDataProviderExecution_failingDataProvider",
        "DragonWarrior.beforeDataProviderExecution_failingDataProvider",
        "MasterOogway.onDataProviderFailure_failingDataProvider",
        "MasterShifu.onDataProviderFailure_failingDataProvider",
        "DragonWarrior.onDataProviderFailure_failingDataProvider"
      };

  public static final List<ITestNGListener> ALL =
      List.of(
          new DataProviderListenerHolder.DragonWarrior(),
          new DataProviderListenerHolder.MasterShifu(),
          new DataProviderListenerHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(
          new DataProviderListenerHolder.DragonWarrior(),
          new DataProviderListenerHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IDataProviderListener {

    @Override
    public void beforeDataProviderExecution(
        IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
      LogContainer.instance.log(
          getClass().getSimpleName()
              + ".beforeDataProviderExecution_"
              + dataProviderMethod.getMethod().getName());
    }

    @Override
    public void afterDataProviderExecution(
        IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
      LogContainer.instance.log(
          getClass().getSimpleName()
              + ".afterDataProviderExecution_"
              + dataProviderMethod.getMethod().getName());
    }

    @Override
    public void onDataProviderFailure(ITestNGMethod method, ITestContext ctx, RuntimeException t) {
      LogContainer.instance.log(
          getClass().getSimpleName()
              + ".onDataProviderFailure_"
              + method.getDataProviderMethod().getMethod().getName());
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
