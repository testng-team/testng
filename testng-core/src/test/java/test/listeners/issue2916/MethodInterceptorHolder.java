package test.listeners.issue2916;

import java.util.ArrayList;
import java.util.List;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGListener;

public class MethodInterceptorHolder {

  public static List<String> LOGS = new ArrayList<>();
  private static final String PREFIX = MethodInterceptorHolder.class.getName() + "$";

  public static final String[] EXPECTED_LOGS =
      new String[] {"MasterOogway.intercept", "MasterShifu.intercept", "DragonWarrior.intercept"};

  public static final List<ITestNGListener> ALL =
      List.of(
          new MethodInterceptorHolder.DragonWarrior(),
          new MethodInterceptorHolder.MasterShifu(),
          new MethodInterceptorHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(
          new MethodInterceptorHolder.DragonWarrior(), new MethodInterceptorHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IMethodInterceptor {

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
      LOGS.add(getClass().getSimpleName() + ".intercept");
      return methods;
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
