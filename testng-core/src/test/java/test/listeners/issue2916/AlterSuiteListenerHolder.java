package test.listeners.issue2916;

import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.ITestNGListener;
import org.testng.xml.XmlSuite;

public class AlterSuiteListenerHolder {

  public static final String[] EXPECTED_LOGS =
      new String[] {"MasterOogway.alter", "MasterShifu.alter", "DragonWarrior.alter"};
  private static final String PREFIX = AlterSuiteListenerHolder.class.getName() + "$";

  public static final List<ITestNGListener> ALL =
      List.of(
          new AlterSuiteListenerHolder.DragonWarrior(),
          new AlterSuiteListenerHolder.MasterShifu(),
          new AlterSuiteListenerHolder.MasterOogway());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements IAlterSuiteListener {
    @Override
    public void alter(List<XmlSuite> suites) {
      LogContainer.instance.log(getClass().getSimpleName() + ".alter");
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
