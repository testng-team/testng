package test.reports;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Generates multiple permutations of TestNG output to see how things look in EmailableReporter.
 *
 * @author Paul Mendelson
 * @since 5.3
 * @version $Revision$
 */
@Test
public class EmailableReportDriver {

  public void doFailureSansLog() {
    Assert.fail("show failure in report");
  }
  public void doFailureNested() {
    Assert.fail("show failure in report",new Exception("Real cuase"));
  }
  public void doFailureWithLog() {
    Reporter.log("Preparing to fail");
    Assert.fail("show failure in report");
  }
  @Test(expectedExceptions={NumberFormatException.class})
  public void doExpectedExceptionSansLog() {
    Reporter.log("step 1");
    Reporter.log("step 2");
    Integer.parseInt("BAD TEXT");
  }
  @Test(expectedExceptions={NumberFormatException.class})
  public void doExpectedExceptionWithLog() {
    Integer.parseInt("BAD TEXT");
  }

}
