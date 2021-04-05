package test.xml;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.lang.reflect.Method;
import java.util.Collections;

public class XmlVerifyTest extends SimpleBaseTest {
  static {
    System.setProperty("testng.testmode", "true");
  }

  private static final String COMMAND_LINE_TEST = "<!-- command_line_test -->";
  private static final String DEFAULT_SUITE = "<!-- Default Suite -->";

  @Test(description = "github-1455")
  public void testToXmlWithComments() {
    XmlSuite suite = createSuite();
    String xml = suite.toXml();
    assertThat(xml).contains(COMMAND_LINE_TEST);
    assertThat(xml).contains(DEFAULT_SUITE);
  }

  @Test(description = "github-1455", dependsOnMethods = "testToXmlWithComments")
  public void testToXmlWithoutComments() {
    System.setProperty("testng.xml.weaver", "org.testng.xml.CommentDisabledXmlWeaver");
    XmlSuite suite = createSuite();
    String xml = suite.toXml();
    assertThat(xml).doesNotContain(COMMAND_LINE_TEST);
    assertThat(xml).doesNotContain(DEFAULT_SUITE);
  }

  @AfterMethod
  public void reset(Method method) {
    if (method.getName().equals("testToXmlWithoutComments")) {
      System.setProperty("testng.xml.weaver", "org.testng.xml.DefaultXmlWeaver");
    }
  }

  private static XmlSuite createSuite() {
    XmlSuite suite = new XmlSuite();
    XmlTest test = new XmlTest(suite);
    test.setName("command_line_test");
    XmlClass xClass = new XmlClass(XmlVerifyTest.class);
    test.getXmlClasses().add(xClass);
    test.addExcludedGroup("fast");
    test.setVerbose(5);
    return suite;
  }

  @Test(description="Ensure that TestNG stops without running any tests if some class" +
      " included in suite is missing")
  public void handleInvalidSuites() {
     TestListenerAdapter tla = new TestListenerAdapter();
     try {
        TestNG tng = create();
        String testngXmlPath = getPathToResource("suite1.xml");
        tng.setTestSuites(Collections.singletonList(testngXmlPath));
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
