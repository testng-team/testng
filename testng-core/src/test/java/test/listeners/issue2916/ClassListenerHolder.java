package test.listeners.issue2916;

import java.util.List;
import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestNGListener;

public class ClassListenerHolder {

  private static final String PREFIX = ClassListenerHolder.class.getName() + "$";

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.onBeforeClass",
        "MasterShifu.onBeforeClass",
        "DragonWarrior.onBeforeClass",
        "MasterOogway.onAfterClass",
        "MasterShifu.onAfterClass",
        "DragonWarrior.onAfterClass"
      };

  public static final List<ITestNGListener> ALL =
      List.of(
          new ClassListenerHolder.DragonWarrior(),
          new ClassListenerHolder.MasterShifu(),
          new ClassListenerHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(new ClassListenerHolder.DragonWarrior(), new ClassListenerHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IClassListener {

    @Override
    public void onBeforeClass(ITestClass testClass) {
      LogContainer.instance.log(getClass().getSimpleName() + ".onBeforeClass");
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
      LogContainer.instance.log(getClass().getSimpleName() + ".onAfterClass");
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
