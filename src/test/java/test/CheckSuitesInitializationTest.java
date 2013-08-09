package test;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * Check for a bug in how relative paths in suite files were being handled.
 *
 * All paths were being resolved using the initial suite's location and not
 * that of the current suite being parsed/processed.
 *
 * This test checks that TestNG can handle cases where we have the following set of
 * files (all linked using relative paths):
 *
 * - parent-suite -> [child-suite-1, children/child-suite-3]
 * - children/child-suite-3 -> [../child-suite-2, child-suite-4, morechildren/child-suite-5]
 *
 * Check the <code>checksuitesinitialization</code> folder under test resources
 *
 * @author Nalin Makar
 */
public class CheckSuitesInitializationTest extends SimpleBaseTest {

  /**
   * Child suites and tests within different suites have same names
   */
  @Test
  public void check() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG tng = create();
    String testngXmlPath = getPathToResource("checksuitesinitialization/parent-suite.xml");
    tng.setTestSuites(Arrays.asList(testngXmlPath));
    tng.addListener(tla);
    tng.run();
    Assert.assertEquals(tla.getPassedTests().size(), 4);
  }

}
