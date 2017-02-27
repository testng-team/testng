package test.xml;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class XmlVerifyTest extends SimpleBaseTest {

  @Test
  public void simple() {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);
    XmlClass xClass = new XmlClass(XmlVerifyTest.class);
    test.getXmlClasses().add(xClass);
    test.addExcludedGroup("fast");
    test.setVerbose(5);

    suite.toXml();
  }

  @Test(description="Ensure that TestNG stops without running any tests if some class" +
      " included in suite is missing")
  public void handleInvalidSuites() {
     TestListenerAdapter tla = new TestListenerAdapter();
     try {
        TestNG tng = create();
        String testngXmlPath = getPathToResource("suite1.xml");
        tng.setTestSuites(Arrays.asList(testngXmlPath));
        tng.addListener((ITestNGListener) tla);
        tng.run();
     } catch (TestNGException ex) {
        Assert.assertEquals(tla.getPassedTests().size(), 0);
     }
  }

  @Test
  public void preserverOrderAttribute() {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);

    suite.setPreserveOrder(true);
    test.setPreserveOrder(false);
    Assert.assertFalse(test.getPreserveOrder());

    suite.setPreserveOrder(false);
    test.setPreserveOrder(true);
    Assert.assertTrue(test.getPreserveOrder());

    suite.setPreserveOrder((Boolean)null);
    test.setPreserveOrder(false);
    Assert.assertFalse(test.getPreserveOrder());

    suite.setPreserveOrder(false);
    test.setPreserveOrder((Boolean)null);
    Assert.assertFalse(test.getPreserveOrder());

    suite.setPreserveOrder((Boolean)null);
    test.setPreserveOrder(true);
    Assert.assertTrue(test.getPreserveOrder());

    suite.setPreserveOrder(true);
    test.setPreserveOrder((Boolean)null);
    Assert.assertTrue(test.getPreserveOrder());

    suite.setPreserveOrder((Boolean)null);
    test.setPreserveOrder((Boolean)null);
    Assert.assertNull(test.getPreserveOrder());
  }
}
