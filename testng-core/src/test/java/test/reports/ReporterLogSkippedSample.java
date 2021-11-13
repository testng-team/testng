package test.reports;

import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ReporterLogListener.class)
public class ReporterLogSkippedSample {

  @BeforeMethod
  public void beforeMethod() {
    throw new RuntimeException("Intentionally failed");
  }

  @Test
  public void test_onSkip() {
    Reporter.log("Log from test_onSkip");
  }
}
