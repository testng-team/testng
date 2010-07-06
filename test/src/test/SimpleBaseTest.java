package test;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class SimpleBaseTest {

  protected TestNG create() {
    TestNG result = new TestNG();
    result.setVerbose(0);
    return result;
  }
  
  protected TestNG create(Class<?> testClass) {
    TestNG result = create();
    result.setTestClasses(new Class[] { testClass});
    return result;
  }

  protected TestNG create(Class<?>[] testClasses) {
    TestNG result = create();
    result.setTestClasses(testClasses);
    return result;
  }

  protected XmlSuite createXmlSuite(String name) {
    XmlSuite result = new XmlSuite();
    result.setName(name);
    return result;
  }

  protected XmlTest createXmlTest(XmlSuite suite, String name, String... classes) {
    XmlTest result = new XmlTest(suite);
    int index = 0;
    result.setName(name);
    for (String c : classes) {
      XmlClass xc = new XmlClass(c, true /* declared class */, index++);
      result.getXmlClasses().add(xc);
    }

    return result;
  }

  protected void addMethods(XmlClass cls, String... methods) {
    int index = 0;
    for (String m : methods) {
      XmlInclude include = new XmlInclude(m, index++);
      cls.getIncludedMethods().add(include);
    }
  }
}
