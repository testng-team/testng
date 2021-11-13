package test.parameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class ParamInheritanceTest extends SimpleBaseTest {

  @Test(
      description =
          "When verbose is set to >1, TNG prints test results on CLI which are printed "
              + "using SuiteResultCounts.calculateResultCounts(). This method has been throwing NPE "
              + "because it's unable to find SuiteRunner in HashMap, because the list of parameters in "
              + "SuiteRunner changed"
              + " during execution. This test makes sure we dont run into any NPEs")
  public void noNPEInCountingResults() {
    TestNG tng = create();
    tng.setTestSuites(Arrays.asList(getPathToResource("param-inheritance/parent-suite.xml")));

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);

    OutputStream os = new ByteArrayOutputStream();
    PrintStream out = System.out;
    PrintStream err = System.err;
    try {
      /*
       * Changing system print streams so that exception or results stmt is not logged
       * while running test (avoid confusing person running tests)
       */
      System.setOut(new PrintStream(os));
      System.setErr(new PrintStream(os));

      tng.run();

      Assert.assertEquals(tla.getPassedTests().size(), 1);
    } finally {
      try {
        os.close();
      } catch (IOException e) {
        // no need to handle this
      }
      System.setOut(out);
      System.setErr(err);
    }
  }

  @Test(description = "Checks to make sure parameters are inherited and overridden properly")
  public void parameterInheritanceAndOverriding() {
    TestNG tng = create();
    tng.setTestSuites(Arrays.asList(getPathToResource("parametertest/parent-suite.xml")));

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 3);
  }
}
