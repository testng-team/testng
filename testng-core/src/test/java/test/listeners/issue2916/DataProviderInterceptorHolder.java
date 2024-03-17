package test.listeners.issue2916;

import java.util.Iterator;
import java.util.List;
import org.testng.*;

public class DataProviderInterceptorHolder {

  private static final String PREFIX = DataProviderInterceptorHolder.class.getName() + "$";

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.intercept_getData",
        "MasterShifu.intercept_getData",
        "DragonWarrior.intercept_getData"
      };

  public static final List<ITestNGListener> ALL =
      List.of(
          new DataProviderInterceptorHolder.DragonWarrior(),
          new DataProviderInterceptorHolder.MasterShifu(),
          new DataProviderInterceptorHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(
          new DataProviderInterceptorHolder.DragonWarrior(),
          new DataProviderInterceptorHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IDataProviderInterceptor {

    @Override
    public Iterator<Object[]> intercept(
        Iterator<Object[]> original,
        IDataProviderMethod dp,
        ITestNGMethod method,
        ITestContext iTestContext) {
      LogContainer.instance.log(
          getClass().getSimpleName() + ".intercept_" + dp.getMethod().getName());
      return original;
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
