package test.configuration;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.TestHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeforeTestOrderingTest {
  private static List<String> m_testNames;

  @BeforeSuite
  public void init() {
    m_testNames = new ArrayList<>();
  }

  static void addTest(String testName) {
    m_testNames.add(testName);
  }

  @Test
  public void verifyBeforeTestOrdering() {

    XmlSuite s = new XmlSuite();

    Reporter.log("BEFORE");

    XmlTest t1 = new XmlTest(s);
    XmlClass c1 = new XmlClass("test.configuration.BeforeTestOrdering1Test");
    t1.getXmlClasses().add(c1);

    XmlTest t2 = new XmlTest(s);
    XmlClass c2 = new XmlClass("test.configuration.BeforeTestOrdering2Test");
    t2.getXmlClasses().add(c2);

    TestNG tng = TestHelper.createTestNG();
    TestListenerAdapter tl = new TestListenerAdapter();
    tng.addListener(tl);
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { s }));
    tng.run();

    List<String> expected = Arrays.asList(new String[] {
      "bt1", "f1", "at1", "bt2", "f2", "at2",
    });

    Assert.assertEquals(expected, m_testNames);
  }



  private static void ppp(String s) {
    System.out.println("[BeforeTestOrderingTest] " + s);
  }

}
