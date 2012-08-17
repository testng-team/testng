package test.reports;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.List;

/**
 * Make sure that Reporter.log() in listeners don't get discarded.
 */
public class ReporterLogTest extends SimpleBaseTest {

  @Test
  public void shouldLogFromListener() {
    TestNG tng = create(ReporterLogSampleTest.class);
    tng.run();
    List<String> output = Reporter.getOutput();
    boolean success = false;
    for(String s : output) {
      if (s.contains("Log from listener")) {
        success = true;
        break;
      }
    }
    Assert.assertTrue(success);
//    System.out.println(output);
  }
}
