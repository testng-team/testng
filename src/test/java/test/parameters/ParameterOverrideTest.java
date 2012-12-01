package test.parameters;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;

public class ParameterOverrideTest extends SimpleBaseTest {
  enum S {
    FAIL,
    PASS_TEST,
    PASS_CLASS,
    PASS_INCLUDE,
  };

  @Test
  public void testOverrideSuite() {
    privateTestOverrideSuite(S.PASS_TEST);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testOverrideSuiteNegative() {
    privateTestOverrideSuite(S.FAIL);
  }

  @Test
  public void classOverrideSuite() {
    privateTestOverrideSuite(S.PASS_CLASS);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void classOverrideSuiteNegative() {
    privateTestOverrideSuite(S.FAIL);
  }

  @Test
  public void includeOverrideClass() {
    privateTestOverrideSuite(S.PASS_INCLUDE);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void includeOverrideClassNegative() {
    privateTestOverrideSuite(S.FAIL);
  }

  public void privateTestOverrideSuite(S status) {
    XmlSuite s = createXmlSuite("s");
    s.getParameters().put("a", "Incorrect");
    s.getParameters().put("InheritedFromSuite", "InheritedFromSuite");
    XmlTest t = createXmlTest(s, "t");
    t.getLocalParameters().put("InheritedFromTest", "InheritedFromTest");
    if (status == S.PASS_TEST) {
      t.getLocalParameters().put("a", "Correct");
    }

    {
      XmlClass c1 = new XmlClass(Override1Sample.class.getName());
      c1.getLocalParameters().put("InheritedFromClass", "InheritedFromClass");
      if (status == S.PASS_CLASS) {
        c1.getLocalParameters().put("a", "Correct");
      }
      t.getXmlClasses().add(c1);

      for (String method : new String[] { "f", "g" }) {
        XmlInclude include1 = new XmlInclude(method);
        if (status == S.PASS_INCLUDE) {
          include1.getLocalParameters().put("a", "Correct");
        }
        include1.setXmlClass(c1);
        c1.getIncludedMethods().add(include1);
      }

    }

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(s));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
//    System.out.println(s.toXml());
//    tng.setVerbose(10);
    tng.run();

    assertTestResultsEqual(tla.getPassedTests(), Arrays.asList("f", "g"));
  }
}
