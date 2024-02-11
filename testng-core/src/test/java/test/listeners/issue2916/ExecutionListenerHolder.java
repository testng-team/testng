package test.listeners.issue2916;

import java.util.ArrayList;
import java.util.List;
import org.testng.IExecutionListener;
import org.testng.ITestNGListener;

public class ExecutionListenerHolder {

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.onExecutionStart",
        "MasterShifu.onExecutionStart",
        "DragonWarrior.onExecutionStart",
        "DragonWarrior.onExecutionFinish",
        "MasterShifu.onExecutionFinish",
        "MasterOogway.onExecutionFinish"
      };
  public static List<String> LOGS = new ArrayList<>();
  private static final String PREFIX = ExecutionListenerHolder.class.getName() + "$";

  public static final List<ITestNGListener> ALL =
      List.of(
          new ExecutionListenerHolder.DragonWarrior(),
          new ExecutionListenerHolder.MasterShifu(),
          new ExecutionListenerHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(
          new ExecutionListenerHolder.DragonWarrior(), new ExecutionListenerHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IExecutionListener {
    @Override
    public void onExecutionStart() {
      LOGS.add(getClass().getSimpleName() + ".onExecutionStart");
    }

    @Override
    public void onExecutionFinish() {
      LOGS.add(getClass().getSimpleName() + ".onExecutionFinish");
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
