package test.override;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.Utils;
import org.xml.sax.SAXException;

import test.SimpleBaseTest;

import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Verify that command line switches override parameters in testng.xml.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class OverrideTest extends SimpleBaseTest {

  private void runTest(String include, String exclude) {
    File f = Utils.createTempFile(
        "<suite name=\"S\">"
        + "  <test name=\"T\">"
        + "    <classes>"
        + "      <class name=\"test.override.OverrideSampleTest\" />"
        + "    </classes>"
        + "  </test>"
        + "</suite>"
        );
    TestNG tng = create();
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    if (include != null) tng.setGroups(include);
    if (exclude != null) tng.setExcludedGroups(exclude);
    tng.setTestSuites(Arrays.asList(f.getAbsolutePath()));
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }

  @Test(description = "Override -groups")
  public void overrideIncludeShouldWork()
      throws ParserConfigurationException, SAXException, IOException {
    runTest("goodGroup", null);
  }

  @Test(description = "Override -excludegroups")
  public void overrideExcludeShouldWork()
      throws ParserConfigurationException, SAXException, IOException {
    runTest(null, "badGroup");
  }

  @Test(description = "Override -groups and -excludegroups")
  public void overrideIncludeAndExcludeShouldWork()
      throws ParserConfigurationException, SAXException, IOException {
    runTest("goodGroup", "badGroup");
  }
}
