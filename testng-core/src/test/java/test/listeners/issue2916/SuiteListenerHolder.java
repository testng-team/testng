package test.listeners.issue2916;

import java.util.ArrayList;
import java.util.List;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGListener;

public class SuiteListenerHolder {

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.onStart",
        "MasterShifu.onStart",
        "DragonWarrior.onStart",
        "DragonWarrior.onFinish",
        "MasterShifu.onFinish",
        "MasterOogway.onFinish"
      };
  public static List<String> LOGS = new ArrayList<>();
  private static final String PREFIX = SuiteListenerHolder.class.getName() + "$";

  public static final List<ITestNGListener> ALL =
      List.of(
          new SuiteListenerHolder.DragonWarrior(),
          new SuiteListenerHolder.MasterShifu(),
          new SuiteListenerHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(new SuiteListenerHolder.DragonWarrior(), new SuiteListenerHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
      LOGS.add(getClass().getSimpleName() + ".onStart");
    }

    @Override
    public void onFinish(ISuite suite) {
      LOGS.add(getClass().getSimpleName() + ".onFinish");
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
