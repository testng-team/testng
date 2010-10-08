package test.pkg;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import test.BaseTest;
import test.pkg2.Test2;
/**
 * Tests that <package> in testng.xml works.
 *
 * Created on Aug 2, 2005
 * @author cbeust
 */
public class PackageTest extends BaseTest {
  public static boolean NON_TEST_CONSTRUCTOR= false;

  @Test
  public void stringSingle() {
    addPackage("test.pkg2", new String[0], new String[0]);
    run();
    String[] passed = {
      "method11", "method12",
      "method31",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void packageWithNonTestClasses() {
    addPackage("test.pkg2", new String[0], new String[0]);
    run();
    assertTrue(!NON_TEST_CONSTRUCTOR, Test2.class.getName() + " should not be considered");
  }

  @Test
  public void packageWithRegExp1() {
    addPackage("test.pkg2", new String[] { ".*1.*"}, new String[0]);
    run();
    String[] passed = {
      "method11", "method12",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void packageWithRegExp2() {
    addPackage("test.pkg2", new String[0], new String[] { ".*1.*"});
    run();
    String[] passed = {
      "method31",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void packageWithRegExp3() {
    addPackage("test.pkg2", new String[] { ".*3.*"}, new String[] { ".*1.*"});
    run();
    String[] passed = {
      "method31",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void packageWithRegExp4() {
    addPackage("test.pkg2",  new String[] { ".*1.*"}, new String[] { ".*3.*"});
    run();
    String[] passed = {
      "method11", "method12"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void packageWithRegExp5() {
    addPackage("test.pkg2",  new String[0], new String[] { "Test.*"});
    run();
    String[] passed = {
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

}
