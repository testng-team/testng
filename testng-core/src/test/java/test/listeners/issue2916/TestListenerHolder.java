package test.listeners.issue2916;

import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.Reporter;

public class TestListenerHolder {

  public static final String[] EXPECTED_LOGS =
      new String[] {
        "MasterOogway.onStart_ITestContext",
        "MasterShifu.onStart_ITestContext",
        "DragonWarrior.onStart_ITestContext",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestSuccess",
        "MasterShifu.onTestSuccess",
        "MasterOogway.onTestSuccess",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestSuccess",
        "MasterShifu.onTestSuccess",
        "MasterOogway.onTestSuccess",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestFailure",
        "MasterShifu.onTestFailure",
        "MasterOogway.onTestFailure",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestSkipped",
        "MasterShifu.onTestSkipped",
        "MasterOogway.onTestSkipped",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestFailedButWithinSuccessPercentage",
        "MasterShifu.onTestFailedButWithinSuccessPercentage",
        "MasterOogway.onTestFailedButWithinSuccessPercentage",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestFailedButWithinSuccessPercentage",
        "MasterShifu.onTestFailedButWithinSuccessPercentage",
        "MasterOogway.onTestFailedButWithinSuccessPercentage",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestFailedButWithinSuccessPercentage",
        "MasterShifu.onTestFailedButWithinSuccessPercentage",
        "MasterOogway.onTestFailedButWithinSuccessPercentage",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestFailure",
        "MasterShifu.onTestFailure",
        "MasterOogway.onTestFailure",
        "MasterOogway.onTestStart",
        "MasterShifu.onTestStart",
        "DragonWarrior.onTestStart",
        "DragonWarrior.onTestFailedWithTimeout",
        "MasterShifu.onTestFailedWithTimeout",
        "MasterOogway.onTestFailedWithTimeout",
        "DragonWarrior.onFinish_ITestContext",
        "MasterShifu.onFinish_ITestContext",
        "MasterOogway.onFinish_ITestContext"
      };
  private static final String PREFIX = TestListenerHolder.class.getName() + "$";

  public static final List<ITestNGListener> ALL =
      List.of(
          new TestListenerHolder.DragonWarrior(),
          new TestListenerHolder.MasterShifu(),
          new TestListenerHolder.MasterOogway());

  public static final List<ITestNGListener> SUBSET =
      List.of(new TestListenerHolder.DragonWarrior(), new TestListenerHolder.MasterShifu());

  public static final List<String> ALL_STRING =
      List.of(PREFIX + "DragonWarrior", PREFIX + "MasterShifu", PREFIX + "MasterOogway");

  public abstract static class KungFuWarrior implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
      LogContainer.instance.log(getClass().getSimpleName() + ".onStart_ITestContext");
    }

    @Override
    public void onTestStart(ITestResult result) {
      LogContainer.instance.log(getClass().getSimpleName() + ".onTestStart");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
      Reporter.log(result.getMethod().getQualifiedName() + " passed", true);
      LogContainer.instance.log(getClass().getSimpleName() + ".onTestSuccess");
    }

    @Override
    public void onTestFailure(ITestResult result) {
      Reporter.log(result.getMethod().getQualifiedName() + " failed", true);
      LogContainer.instance.log(getClass().getSimpleName() + ".onTestFailure");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
      Reporter.log(result.getMethod().getQualifiedName() + " skipped", true);
      LogContainer.instance.log(getClass().getSimpleName() + ".onTestSkipped");
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
      Reporter.log(result.getMethod().getQualifiedName() + " failed due to timeout", true);
      LogContainer.instance.log(getClass().getSimpleName() + ".onTestFailedWithTimeout");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
      Reporter.log(
          result.getMethod().getQualifiedName() + " test failed but within success percentage",
          true);
      LogContainer.instance.log(
          getClass().getSimpleName() + ".onTestFailedButWithinSuccessPercentage");
    }

    @Override
    public void onFinish(ITestContext context) {
      LogContainer.instance.log(getClass().getSimpleName() + ".onFinish_ITestContext");
    }
  }

  @RunOrder(1)
  public static class MasterOogway extends KungFuWarrior {}

  @RunOrder(2)
  public static class MasterShifu extends KungFuWarrior {}

  public static class DragonWarrior extends KungFuWarrior {}
}
