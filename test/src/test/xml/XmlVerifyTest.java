package test.xml;

import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class XmlVerifyTest {

  @Test
  public void simple() {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);
    XmlClass xClass = new XmlClass(XmlVerifyTest.class);
    test.getXmlClasses().add(xClass);
    test.getExcludedGroups().add("fast");
    test.setVerbose(5);
    
    suite.toXml();
  }
}
