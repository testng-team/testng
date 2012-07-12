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
    PASS_TEST,
    FAIL_TEST,
    PASS_CLASS,
    FAIL_CLASS
  };

//  @Test
  public void testOverrideSuite() {
    privateTestOverrideSuite(S.PASS_TEST);
  }

//  @Test(expectedExceptions = AssertionError.class)
  public void testOverrideSuiteNegative() {
    privateTestOverrideSuite(S.FAIL_TEST);
  }

  @Test
  public void classOverrideSuite() {
    privateTestOverrideSuite(S.PASS_CLASS);
  }

//  @Test(expectedExceptions = AssertionError.class)
  public void classOverrideSuiteNegative() {
    privateTestOverrideSuite(S.FAIL_CLASS);
  }

  public void privateTestOverrideSuite(S status) {
    XmlSuite s = createXmlSuite("s");
    s.getParameters().put("a", "Incorrect");
    s.getParameters().put("InheritedFromSuite", "InheritedFromSuite");
    XmlTest t = createXmlTest(s, "t");
    if (status == S.PASS_TEST) {
      t.getParameters().put("a", "Correct");
    }

    {
      XmlClass c1 = new XmlClass(Override1Sample.class.getName());
      if (status == S.PASS_CLASS) {
        c1.getParameters().put("a", "Correct");
      }
      t.getXmlClasses().add(c1);

      for (String method : new String[] { "f", "g" }) {
        XmlInclude include1 = new XmlInclude(method);
        include1.setXmlClass(c1);
        c1.getIncludedMethods().add(include1);
      }

    }

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(s));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    System.out.println(s.toXml());
    tng.setVerbose(10);
    tng.run();

    assertTestResultsEqual(tla.getPassedTests(), Arrays.asList("f", "g"));
  }
}
