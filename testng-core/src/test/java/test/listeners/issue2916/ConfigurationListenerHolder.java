package test.listeners.issue2916;

import java.util.List;
import org.testng.IConfigurationListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class ConfigurationListenerHolder {

  private static final String PREFIX = ConfigurationListenerHolder.class.getName() + "$";

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.beforeConfiguration_beforeSuite",
        "MasterShifu.beforeConfiguration_beforeSuite",
        "DragonWarrior.beforeConfiguration_beforeSuite",
        "DragonWarrior.onConfigurationSuccess_beforeSuite",
        "MasterShifu.onConfigurationSuccess_beforeSuite",
        "MasterOogway.onConfigurationSuccess_beforeSuite",
        "MasterOogway.beforeConfiguration_beforeTest",
        "MasterShifu.beforeConfiguration_beforeTest",
        "DragonWarrior.beforeConfiguration_beforeTest",
        "DragonWarrior.onConfigurationSuccess_beforeTest",
        "MasterShifu.onConfigurationSuccess_beforeTest",
        "MasterOogway.onConfigurationSuccess_beforeTest",
        "MasterOogway.beforeConfiguration_beforeClass",
        "MasterShifu.beforeConfiguration_beforeClass",
        "DragonWarrior.beforeConfiguration_beforeClass",
        "DragonWarrior.onConfigurationSuccess_beforeClass",
        "MasterShifu.onConfigurationSuccess_beforeClass",
        "MasterOogway.onConfigurationSuccess_beforeClass",
        "MasterOogway.beforeConfiguration_beforeMethod",
        "MasterShifu.beforeConfiguration_beforeMethod",
        "DragonWarrior.beforeConfiguration_beforeMethod",
        "DragonWarrior.onConfigurationFailure_beforeMethod",
        "MasterShifu.onConfigurationFailure_beforeMethod",
        "MasterOogway.onConfigurationFailure_beforeMethod"
      };

  public static final List<ITestNGListener> ALL =
      List.of(
          new ConfigurationListenerHolder.DragonWarrior(),
          new ConfigurationListenerHolder.MasterShifu(),
          new ConfigurationListenerHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(
          new ConfigurationListenerHolder.DragonWarrior(),
          new ConfigurationListenerHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IConfigurationListener {

    @Override
    public void beforeConfiguration(ITestResult tr, ITestNGMethod tm) {
      LogContainer.instance.log(
          getClass().getSimpleName() + ".beforeConfiguration_" + tr.getMethod().getMethodName());
    }

    @Override
    public void onConfigurationSuccess(ITestResult tr, ITestNGMethod tm) {
      LogContainer.instance.log(
          getClass().getSimpleName() + ".onConfigurationSuccess_" + tr.getMethod().getMethodName());
    }

    @Override
    public void onConfigurationFailure(ITestResult tr, ITestNGMethod tm) {
      LogContainer.instance.log(
          getClass().getSimpleName() + ".onConfigurationFailure_" + tr.getMethod().getMethodName());
    }

    @Override
    public void onConfigurationSkip(ITestResult tr, ITestNGMethod tm) {
      LogContainer.instance.log(
          getClass().getSimpleName() + ".onConfigurationSkip_" + tr.getMethod().getMethodName());
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
