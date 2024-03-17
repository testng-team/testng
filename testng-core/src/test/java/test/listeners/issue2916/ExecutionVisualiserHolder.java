package test.listeners.issue2916;

import java.util.List;
import org.testng.*;

public class ExecutionVisualiserHolder {

  private static final String PREFIX = ExecutionVisualiserHolder.class.getName() + "$";

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.consumeDotDefinition",
        "MasterShifu.consumeDotDefinition",
        "DragonWarrior.consumeDotDefinition"
      };

  public static final List<ITestNGListener> ALL =
      List.of(
          new ExecutionVisualiserHolder.DragonWarrior(),
          new ExecutionVisualiserHolder.MasterShifu(),
          new ExecutionVisualiserHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(
          new ExecutionVisualiserHolder.DragonWarrior(),
          new ExecutionVisualiserHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IExecutionVisualiser {

    @Override
    public void consumeDotDefinition(String dotDefinition) {
      LogContainer.instance.log(getClass().getSimpleName() + ".consumeDotDefinition");
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
