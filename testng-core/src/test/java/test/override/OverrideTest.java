package test.override;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.reporters.Files;
import test.SimpleBaseTest;
import test.TestHelper;

/**
 * Verify that command line switches override parameters in testng.xml.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class OverrideTest extends SimpleBaseTest {

  private static File createTempFile(String content) {
    try {
      // Create temp file.
      File result = File.createTempFile("testng-tmp", "");

      // Delete temp file when program exits.
      result.deleteOnExit();
      Files.writeFile(content, result);

      return result;
    } catch (IOException e) {
      throw new TestNGException(e);
    }
  }

  private void runTest(String include, String exclude) {
    File f =
        createTempFile(
            TestHelper.SUITE_XML_HEADER
                + "<suite name=\"S\">"
                + "  <test name=\"T\">"
                + "    <classes>"
                + "      <class name=\"test.override.OverrideSampleTest\" />"
                + "    </classes>"
                + "  </test>"
                + "</suite>");
    TestNG tng = create();
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    if (include != null) tng.setGroups(include);
    if (exclude != null) tng.setExcludedGroups(exclude);
    tng.setTestSuites(Collections.singletonList(f.getAbsolutePath()));
    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }

  @Test(description = "Override -groups")
  public void overrideIncludeShouldWork() {
    runTest("goodGroup", null);
  }

  @Test(description = "Override -excludegroups")
  public void overrideExcludeShouldWork() {
    runTest(null, "badGroup");
  }

  @Test(description = "Override -groups and -excludegroups")
  public void overrideIncludeAndExcludeShouldWork() {
    runTest("goodGroup", "badGroup");
  }
}
