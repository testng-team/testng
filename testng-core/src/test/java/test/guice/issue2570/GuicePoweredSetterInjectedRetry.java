package test.guice.issue2570;

import com.google.inject.Inject;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.Guice;
import test.guice.issue2570.GuiceModule.Terminator;

@Guice(modules = GuiceModule.class)
public class GuicePoweredSetterInjectedRetry implements IRetryAnalyzer {

  private int count = 1;

  private static String terminator;

  @Inject
  public void setName(@Terminator String name) {
    setValue(name);
  }

  private static void setValue(String warriorName) {
    terminator = warriorName;
  }

  public static String getTerminator() {
    return terminator;
  }

  @Override
  public boolean retry(ITestResult result) {
    return !result.isSuccess() && count-- > 0;
  }
}
