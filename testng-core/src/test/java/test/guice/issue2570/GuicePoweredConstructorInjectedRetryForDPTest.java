package test.guice.issue2570;

import com.google.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Guice;
import test.guice.issue2570.GuiceModule.DragonWarrior;

@Guice(modules = GuiceModule.class)
public class GuicePoweredConstructorInjectedRetryForDPTest implements IRetryAnalyzer {

  private static AtomicInteger counter = new AtomicInteger(0);

  public static int getCounter() {
    return counter.get();
  }

  @Inject
  public GuicePoweredConstructorInjectedRetryForDPTest(@DragonWarrior String name) {
    counter.incrementAndGet();
  }

  private int count = 1;

  @Override
  public boolean retry(ITestResult result) {
    return !result.isSuccess() && count-- > 0;
  }
}
