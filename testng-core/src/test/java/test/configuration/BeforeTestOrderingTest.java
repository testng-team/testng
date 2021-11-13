package test.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

public class BeforeTestOrderingTest {

  private static final List<String> m_testNames = new ArrayList<>();

  @BeforeSuite
  public void init() {
    m_testNames.clear();
  }

  static void addTest(String testName) {
    m_testNames.add(testName);
  }

  private static final List<String> expected =
      Arrays.asList("bt1", "f1", "at1", "bt2", "f2", "at2");

  @Test
  public void verifyBeforeTestOrdering() throws IOException {
    XmlSuite s = new XmlSuite();
    Reporter.log("BEFORE");

    XmlTest t1 = new XmlTest(s);
    XmlClass c1 = new XmlClass(BeforeTestOrdering1Test.class);
    t1.getXmlClasses().add(c1);

    XmlTest t2 = new XmlTest(s);
    XmlClass c2 = new XmlClass(BeforeTestOrdering2Test.class);
    t2.getXmlClasses().add(c2);

    TestNG tng = TestHelper.createTestNG();
    TestListenerAdapter tl = new TestListenerAdapter();
    tng.addListener(tl);
    tng.setXmlSuites(Collections.singletonList(s));
    tng.run();

    Assert.assertEquals(m_testNames, expected);
  }
}
