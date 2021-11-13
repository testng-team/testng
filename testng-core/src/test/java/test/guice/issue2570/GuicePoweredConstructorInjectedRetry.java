package test.guice.issue2570;

import com.google.inject.Inject;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Guice;
import test.guice.issue2570.GuiceModule.DragonWarrior;

@Guice(modules = GuiceModule.class)
public class GuicePoweredConstructorInjectedRetry implements IRetryAnalyzer {

  private static String dragonWarrior;

  private static void setName(String warriorName) {
    dragonWarrior = warriorName;
  }

  public static String getDragonWarrior() {
    return dragonWarrior;
  }

  @Inject
  public GuicePoweredConstructorInjectedRetry(@DragonWarrior String name) {
    setName(name);
  }

  private int count = 1;

  @Override
  public boolean retry(ITestResult result) {
    return !result.isSuccess() && count-- > 0;
  }
}
