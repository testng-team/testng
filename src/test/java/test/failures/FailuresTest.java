package test.failures;


import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.TestHelper;

import java.util.HashMap;
import java.util.Map;

public class FailuresTest extends BaseFailuresTest {

  @Test
  public void shouldIncludeFailedMethodsFromBaseClass() {
    XmlSuite suite = TestHelper.createSuite("test.failures.Child", getSuiteName());
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();

     String[] expected = new String[] {
       "<class name=\"test.failures.Child\">",
       "<include name=\"fail\"/>",
       "<include name=\"failFromBase\"/>",
     };

     verify(getOutputDir(), expected);
  }

  @Test(enabled = false)
  public void shouldIncludeDependentMethods() {
    XmlSuite suite = TestHelper.createSuite("test.failures.DependentTest", getSuiteName());
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();

    String[] expected = new String[] {
        "<include name=\"f1\"/>",
        "<include name=\"f2\"/>"
      };

    verify(getOutputDir(), expected);
  }

  @Test(enabled = false)
  public void shouldIncludeParameters() {
    XmlSuite suite = TestHelper.createSuite("test.failures.Child", getSuiteName());
    Map<String, String> params = new HashMap<>();
    params.put("first-name", "Cedric");
    params.put("last-name", "Beust");
    suite.setParameters(params);

    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();

    String[] expected = new String[] {
        "<parameter name=\"first-name\" value=\"Cedric\"/>",
      };

    verify(getOutputDir(), expected);
  }

  private String getOutputDir() {
    return System.getProperty("java.io.tmpdir");
  }

  private static void ppp(String s) {
    System.out.println("[FailuresTest] " + s);
  }
}
