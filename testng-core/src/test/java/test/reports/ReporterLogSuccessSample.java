package test.reports;

import org.testng.Reporter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ReporterLogListener.class)
public class ReporterLogSuccessSample {

  @Test
  public void test_onSuccess() {
    Reporter.log("Log from test_onSuccess");
  }
}
