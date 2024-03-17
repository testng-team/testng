package test.listeners.issue2916;

import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;

public class InvokedMethodListenerHolder {

  private static final String PREFIX = InvokedMethodListenerHolder.class.getName() + "$";

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.beforeInvocation_beforeSuite",
        "MasterShifu.beforeInvocation_beforeSuite",
        "DragonWarrior.beforeInvocation_beforeSuite",
        "DragonWarrior.afterInvocation_beforeSuite",
        "MasterShifu.afterInvocation_beforeSuite",
        "MasterOogway.afterInvocation_beforeSuite",
        "MasterOogway.beforeInvocation_beforeTest",
        "MasterShifu.beforeInvocation_beforeTest",
        "DragonWarrior.beforeInvocation_beforeTest",
        "DragonWarrior.afterInvocation_beforeTest",
        "MasterShifu.afterInvocation_beforeTest",
        "MasterOogway.afterInvocation_beforeTest",
        "MasterOogway.beforeInvocation_beforeClass",
        "MasterShifu.beforeInvocation_beforeClass",
        "DragonWarrior.beforeInvocation_beforeClass",
        "DragonWarrior.afterInvocation_beforeClass",
        "MasterShifu.afterInvocation_beforeClass",
        "MasterOogway.afterInvocation_beforeClass",
        "MasterOogway.beforeInvocation_beforeMethod",
        "MasterShifu.beforeInvocation_beforeMethod",
        "DragonWarrior.beforeInvocation_beforeMethod",
        "DragonWarrior.afterInvocation_beforeMethod",
        "MasterShifu.afterInvocation_beforeMethod",
        "MasterOogway.afterInvocation_beforeMethod",
        "MasterOogway.beforeInvocation_testMethod",
        "MasterShifu.beforeInvocation_testMethod",
        "DragonWarrior.beforeInvocation_testMethod",
        "DragonWarrior.afterInvocation_testMethod",
        "MasterShifu.afterInvocation_testMethod",
        "MasterOogway.afterInvocation_testMethod",
        "MasterOogway.beforeInvocation_afterMethod",
        "MasterShifu.beforeInvocation_afterMethod",
        "DragonWarrior.beforeInvocation_afterMethod",
        "DragonWarrior.afterInvocation_afterMethod",
        "MasterShifu.afterInvocation_afterMethod",
        "MasterOogway.afterInvocation_afterMethod",
        "MasterOogway.beforeInvocation_beforeMethod",
        "MasterShifu.beforeInvocation_beforeMethod",
        "DragonWarrior.beforeInvocation_beforeMethod",
        "DragonWarrior.afterInvocation_beforeMethod",
        "MasterShifu.afterInvocation_beforeMethod",
        "MasterOogway.afterInvocation_beforeMethod",
        "MasterOogway.beforeInvocation_testMethod",
        "MasterShifu.beforeInvocation_testMethod",
        "DragonWarrior.beforeInvocation_testMethod",
        "DragonWarrior.afterInvocation_testMethod",
        "MasterShifu.afterInvocation_testMethod",
        "MasterOogway.afterInvocation_testMethod",
        "MasterOogway.beforeInvocation_afterMethod",
        "MasterShifu.beforeInvocation_afterMethod",
        "DragonWarrior.beforeInvocation_afterMethod",
        "DragonWarrior.afterInvocation_afterMethod",
        "MasterShifu.afterInvocation_afterMethod",
        "MasterOogway.afterInvocation_afterMethod",
        "MasterOogway.beforeInvocation_afterClass",
        "MasterShifu.beforeInvocation_afterClass",
        "DragonWarrior.beforeInvocation_afterClass",
        "DragonWarrior.afterInvocation_afterClass",
        "MasterShifu.afterInvocation_afterClass",
        "MasterOogway.afterInvocation_afterClass",
        "MasterOogway.beforeInvocation_afterTest",
        "MasterShifu.beforeInvocation_afterTest",
        "DragonWarrior.beforeInvocation_afterTest",
        "DragonWarrior.afterInvocation_afterTest",
        "MasterShifu.afterInvocation_afterTest",
        "MasterOogway.afterInvocation_afterTest",
        "MasterOogway.beforeInvocation_afterSuite",
        "MasterShifu.beforeInvocation_afterSuite",
        "DragonWarrior.beforeInvocation_afterSuite",
        "DragonWarrior.afterInvocation_afterSuite",
        "MasterShifu.afterInvocation_afterSuite",
        "MasterOogway.afterInvocation_afterSuite"
      };

  public static final List<ITestNGListener> ALL =
      List.of(
          new InvokedMethodListenerHolder.DragonWarrior(),
          new InvokedMethodListenerHolder.MasterShifu(),
          new InvokedMethodListenerHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(
          new InvokedMethodListenerHolder.DragonWarrior(),
          new InvokedMethodListenerHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
      IInvokedMethodListener.super.beforeInvocation(method, testResult);
      LogContainer.instance.log(
          getClass().getSimpleName()
              + ".beforeInvocation_"
              + method.getTestMethod().getMethodName());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      LogContainer.instance.log(
          getClass().getSimpleName()
              + ".afterInvocation_"
              + method.getTestMethod().getMethodName());
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
