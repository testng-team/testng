package test.groupinvocation;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;

/**
 * Test that <suite> tags can have groups.
 */
@Test
public class GroupSuiteTest extends SimpleBaseTest {

  public void includeFromSuite0() {
    runWithSuite(g(), g(), g("a", "b", "c"));
  }

  public void includeFromSuite1() {
    runWithSuite(g("a"), g(), g("a"));
  }
  
  public void includeFromSuite2() {
    runWithSuite(g("a", "b"), g(), g("a", "b"));
  }

  public void excludeFromSuite1() {
    runWithSuite(g(), g("a"), g("b", "c"));
  }

  public void excludeFromSuite2() {
    runWithSuite(g(), g("a", "b"), g("c"));
  }

  @Test(description = "Include in both suite and test")
  public void includeTestAndSuite1() {
    runWithSuite(g("a"), g(), g("b"), g(), g("a", "b"));
  }

  @Test(description = "Include in suite, exclude in test")
  public void excludeTestAndSuite2() {
    runWithSuite(g(), g("a"), g(), g("a"), g("b", "c"));
  }

  private void runWithSuite(String[] suiteGroups, String[] excludedSuiteGroups,
      String[] methods) {
    runWithSuite(suiteGroups, excludedSuiteGroups, g(), g(), methods);
  }

  private void runWithSuite(String[] suiteGroups, String[] excludedSuiteGroups,
      String[] testGroups, String[] excludedTestGroups,
      String[] methods) {
    XmlSuite s = createXmlSuite("Groups");
    s.setIncludedGroups(Arrays.asList(suiteGroups));
    s.setExcludedGroups(Arrays.asList(excludedSuiteGroups));
    XmlTest t = createXmlTest(s, "Groups-test", GroupSuiteSampleTest.class.getName());
    t.setIncludedGroups(Arrays.asList(testGroups));
    t.setExcludedGroups(Arrays.asList(excludedTestGroups));
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    tng.addListener(tla);
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { s }));
    tng.run();

    verifyPassedTests(tla, methods);
  }

  private String[] g(String... groups) {
    return groups;
  }
}
