package test;

import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Make sure we don't remove public API's from Reporter.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class ReporterApiTest {

  @Test
  public void testApi() {
    Reporter.log("foo");
    Reporter.log("foo", false);
    Reporter.log("foo", 2);
    Reporter.log("foo", 2, false);
  }
}
