package test.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import test.SimpleBaseTest;

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

  @Test(description = "GITHUB-3177")
  public void testThreadPoolRelatedAttributesPresentInXml() throws Exception {
    XmlSuite suite = createSuite();
    suite.setShareThreadPoolForDataProviders(true);
    suite.shouldUseGlobalThreadPool(true);
    String xml = suite.toXml();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new InputSource(new StringReader(xml)));
    document.getDocumentElement().normalize();
    NodeList allSuites = document.getElementsByTagName("suite");
    assertThat(allSuites.getLength()).isEqualTo(1);
    Node xmlSuite = allSuites.item(0);
    assertThat(xmlSuite.getNodeType()).isEqualTo(Node.ELEMENT_NODE);
    Element element = (Element) xmlSuite;
    assertThat(element.getAttribute("use-global-thread-pool")).isEqualTo(Boolean.TRUE.toString());
    assertThat(element.getAttribute("share-thread-pool-for-data-providers"))
        .isEqualTo(Boolean.TRUE.toString());
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
    return suite;
  }

  @Test(
      description =
          "Ensure that TestNG stops without running any tests if some class"
              + " included in suite is missing")
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

    suite.setPreserveOrder((Boolean) null);
    test.setPreserveOrder(false);
    Assert.assertFalse(test.getPreserveOrder());

    suite.setPreserveOrder(false);
    test.setPreserveOrder((Boolean) null);
    Assert.assertFalse(test.getPreserveOrder());

    suite.setPreserveOrder((Boolean) null);
    test.setPreserveOrder(true);
    Assert.assertTrue(test.getPreserveOrder());

    suite.setPreserveOrder(true);
    test.setPreserveOrder((Boolean) null);
    Assert.assertTrue(test.getPreserveOrder());

    suite.setPreserveOrder((Boolean) null);
    test.setPreserveOrder((Boolean) null);
    Assert.assertNull(test.getPreserveOrder());
  }
}
