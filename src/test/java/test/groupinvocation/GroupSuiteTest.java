package test.groupinvocation;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

/**
 * Test that <suite> tags can have g.
 */
@Test
public class GroupSuiteTest extends SimpleBaseTest {

  public void includeFromSuite0() {
    runWithSuite(g(), g(), "a", "b", "c");
  }

  public void includeFromSuite1() {
    runWithSuite(g("a"), g(), "a");
  }
  
  public void includeFromSuite2() {
    runWithSuite(g("a", "b"), g(), "a", "b");
  }

  public void excludeFromSuite1() {
    runWithSuite(g(), g("a"), "b", "c");
  }

  public void excludeFromSuite2() {
    runWithSuite(g(), g("a", "b"), "c");
  }

  @Test(description = "Include in both suite and test")
  public void includeTestAndSuite1() {
    runWithSuite(g("a"), g(), g("b"), g(), "a", "b");
  }

  @Test(description = "Include in suite, exclude in test")
  public void excludeTestAndSuite2() {
    runWithSuite(g("a", "b"), g(), g(), g("a"), "b");
  }

  private void runWithSuite(List<String> suiteGroups, List<String> excludedSuiteGroups,
      String... methods) {
    runWithSuite(suiteGroups, excludedSuiteGroups, g(), g(), methods);
  }

  private void runWithSuite(List<String> suiteGroups, List<String> excludedSuiteGroups,
                            List<String> testGroups, List<String> excludedTestGroups,
                            String... methods) {
    TestNG tng = create();

    XmlSuite suite = createXmlSuite("Groups");
    suite.setIncludedGroups(suiteGroups);
    suite.setExcludedGroups(excludedSuiteGroups);

    XmlTest test = createXmlTest(suite, "Groups-test", GroupSuiteSampleTest.class);
    test.setIncludedGroups(testGroups);
    test.setExcludedGroups(excludedTestGroups);

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
