package test.jason;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;

public class MainTest extends SimpleBaseTest {

  @Test
  public void afterClassShouldRun() {
    XmlSuite s = createXmlSuite("S");
    XmlTest t = createXmlTest(s, "T", Main.class.getName());
    XmlClass c = t.getXmlClasses().get(0);
    c.getIncludedMethods().add(new XmlInclude("test1"));
    t.setPreserveOrder("true");
    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { s }));
    Main.m_passed = false;
    tng.run();
    Assert.assertTrue(Main.m_passed);
  }
}