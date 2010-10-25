package test.reports;

import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Regression test:  if a timeOut is provided, getReporter(testResult) returns
 * null.
 *
 * Created on Sep 21, 2006
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class ReporterSampleTest {

  @DataProvider(name = "dp")
  public Object[][] createParameters() {
    return new Object[][] {
        new Object[] { "param1"},
        new Object[] {"param2"}
    };
  }

  @Test(dataProvider = "dp", timeOut = 10000)
  public void report(String p) {
    Reporter.log("IN THE REPORTER: " + p);
  }

}
