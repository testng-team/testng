package test.groupinvocation;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Test that <suite> tags can have g.
 */
public class GroupSuiteTest extends SimpleBaseTest {

  private static final File PARENT = new File(getPathToResource("groupinvocation"));

  @DataProvider
  private static Object[][] dp() {
    return new Object[][]{{true}, {false}};
  }

  @Test(dataProvider = "dp")
  public void includeFromSuite0(boolean withSuiteFiles) {
    runWithSuite(withSuiteFiles, "a", "b", "c", "a2", "b2", "c2");
  }

  @Test(dataProvider = "dp")
  public void includeFromSuite1(boolean withSuiteFiles) {
    runWithSuite(withSuiteFiles, g("a"), g(), "a", "a2");
  }

  @Test(dataProvider = "dp")
  public void includeFromSuite2(boolean withSuiteFiles) {
    runWithSuite(withSuiteFiles, g("a", "b"), g(), "a", "b", "a2", "b2");
  }

  @Test(dataProvider = "dp")
  public void excludeFromSuite1(boolean withSuiteFiles) {
    runWithSuite(withSuiteFiles, g(), g("a"), "b", "c", "b2", "c2");
  }

  @Test(dataProvider = "dp")
  public void excludeFromSuite2(boolean withSuiteFiles) {
    runWithSuite(withSuiteFiles, g(), g("a", "b"), "c", "c2");
  }

  @Test(description = "Include in both suite and test")
  public void includeTestAndSuite1() {
    runWithSuite(g("a"), g(), g("b"), g(), "a", "b", "a2", "b2");
  }

  @Test(description = "Include in suite, exclude in test")
  public void excludeTestAndSuite2() {
    runWithSuite(g("a", "b"), g(), g(), g("a"), "b", "b2");
  }

  private void runWithSuite(boolean withSuiteFiles, String... methods) {
    runWithSuite(withSuiteFiles, g(), g(), g(), g(), methods);
  }

  private void runWithSuite(boolean withSuiteFiles, List<String> suiteGroups, List<String> excludedSuiteGroups,
      String... methods) {
    runWithSuite(withSuiteFiles, suiteGroups, excludedSuiteGroups, g(), g(), methods);
  }

  private void runWithSuite(List<String> suiteGroups, List<String> excludedSuiteGroups,
                            List<String> testGroups, List<String> excludedTestGroups,
                            String... methods) {
    runWithSuite(false, suiteGroups, excludedSuiteGroups, testGroups, excludedTestGroups, methods);
  }

  private void runWithSuite(boolean withSuiteFiles, List<String> suiteGroups, List<String> excludedSuiteGroups,
                            List<String> testGroups, List<String> excludedTestGroups,
                            String... methods) {
    TestNG tng = create();

    XmlSuite suite = createXmlSuite("Groups");
    suite.setIncludedGroups(suiteGroups);
    suite.setExcludedGroups(excludedSuiteGroups);
    if (withSuiteFiles) {
      suite.setSuiteFiles(Arrays.asList(
              new File(PARENT, "suiteA.xml").getAbsolutePath(),
              new File(PARENT, "suiteB.xml").getAbsolutePath())
      );
      createXmlTest(suite, "Groups-test");
    } else {
      XmlTest test = createXmlTest(suite, "Groups-test", GroupSuiteSampleTest.class, GroupSuiteSampleTest2.class);
      test.setIncludedGroups(testGroups);
      test.setExcludedGroups(excludedTestGroups);
    }

    tng.setXmlSuites(Arrays.asList(suite));

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    verifyPassedTests(tla, methods);
  }

  private static List<String> g(String... groups) {
    return Arrays.asList(groups);
  }
}
