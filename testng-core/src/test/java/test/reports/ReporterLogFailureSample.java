package test.reports;

import org.testng.Reporter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ReporterLogListener.class)
public class ReporterLogFailureSample {

  @Test
  public void test_onFailure() {
    Reporter.log("Log from test_onFailure");
    throw new RuntimeException("Intentionally failed");
  }
}
