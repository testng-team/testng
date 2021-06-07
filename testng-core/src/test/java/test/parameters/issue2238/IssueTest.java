package test.parameters.issue2238;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void ensureParametersCanBeOverriddenAtSuiteLevel() {
    XmlSuite xmlSuite = createXmlSuite("suite", "test", ExampleTestCase.class);
    xmlSuite.getParameters().put("value", "100");
    runTest(xmlSuite);
  }

  @Test
  public void ensureParametersCanBeOverriddenAtTestLevel() {
    XmlSuite xmlSuite = createXmlSuite("suite");
    XmlTest xmltest = createXmlTest(xmlSuite, "test", ExampleTestCase.class);
    xmltest.getLocalParameters().put("value", "100");
    runTest(xmlSuite);
  }

  @Test
  public void ensureParametersCanBeOverriddenAtClassLevel() {
    XmlSuite xmlSuite = createXmlSuite("suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "test");
    XmlClass xmlclass = createXmlClass(xmlTest, ExampleTestCase.class);
    xmlclass.getLocalParameters().put("value", "100");
    runTest(xmlSuite);
  }

  @Test
  public void ensureParametersCanBeOverriddenAtMethodLevel() {
    XmlSuite xmlSuite = createXmlSuite("suite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "test");
    XmlClass xmlclass = createXmlClass(xmlTest, ExampleTestCase.class);
    XmlInclude xmlInclude = new XmlInclude();
    xmlInclude.setName("testMethod");
    xmlInclude.getLocalParameters().put("value", "100");
    xmlclass.getIncludedMethods().add(xmlInclude);
    runTest(xmlSuite);
  }

  private void runTest(XmlSuite xmlSuite) {
    System.setProperty("value", "200");
    TestNG testng = create(xmlSuite);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getFailedTests()).isEmpty();
  }
}
