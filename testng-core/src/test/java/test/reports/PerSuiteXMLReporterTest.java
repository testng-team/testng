package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.testng.CommandLineArgs;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import test.SimpleBaseTest;
import test.reports.issue2906.SampleOneTestCase;
import test.reports.issue2906.SampleThreeTestCase;
import test.reports.issue2906.SampleTwoTestCase;

public class PerSuiteXMLReporterTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2906")
  public void ensurePerSuiteGenerationHappens() throws Exception {
    runTest(testng -> testng.setGenerateResultsPerSuite(true));
  }

  @Test(description = "GITHUB-2906")
  public void ensurePerSuiteGenerationHappensWithEnabledViaMap() throws Exception {
    runTest(
        testng -> {
          Map<String, Object> map = new HashMap<>();
          map.put(CommandLineArgs.GENERATE_RESULTS_PER_SUITE, "true");
          testng.configure(map);
        });
  }

  private void runTest(Consumer<TestNG> tweaker) throws Exception {
    XmlSuite parentSuite = createXmlSuite("parent_suite");
    XmlSuite childSuite1 = createChildSuite("child_one", SampleOneTestCase.class);
    XmlSuite childSuite2 = createChildSuite("child_two", SampleTwoTestCase.class);
    XmlSuite childSuite3 = createChildSuite("child_three", SampleThreeTestCase.class);
    parentSuite.getChildSuites().addAll(Arrays.asList(childSuite1, childSuite2, childSuite3));

    File parentDir = createDirInTempDir("parent_suite");
    TestNG testng = create(parentDir.toPath(), parentSuite);
    testng.setUseDefaultListeners(true);
    tweaker.accept(testng);
    testng.run();
    runVerificationsForSuite(parentDir, childSuite1, "firstPassingTestCase() ran");
    runVerificationsForSuite(parentDir, childSuite2, "secondPassingTestCase() ran");
    runVerificationsForSuite(parentDir, childSuite3, "thirdPassingTestCase() ran");
  }

  private static void runVerificationsForSuite(File parentDir, XmlSuite xmlSuite, String output)
      throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
    File dir = new File(parentDir, xmlSuite.getName());
    File resultsFile = new File(dir, "testng-results.xml");
    assertThat(resultsFile).exists();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(resultsFile);
    XPath xPath = XPathFactory.newInstance().newXPath();
    Node node = (Node) xPath.compile("/testng-results").evaluate(doc, XPathConstants.NODE);
    int ignored = Integer.parseInt(node.getAttributes().getNamedItem("ignored").getNodeValue());
    int total = Integer.parseInt(node.getAttributes().getNamedItem("total").getNodeValue());
    int passed = Integer.parseInt(node.getAttributes().getNamedItem("passed").getNodeValue());
    int failed = Integer.parseInt(node.getAttributes().getNamedItem("failed").getNodeValue());
    assertThat(ignored).isZero();
    assertThat(total).isEqualTo(2);
    assertThat(passed).isEqualTo(1);
    assertThat(failed).isEqualTo(1);
    String text =
        (String)
            xPath
                .compile("/testng-results/reporter-output/line")
                .evaluate(doc, XPathConstants.STRING);
    text = text.stripLeading().stripTrailing().replaceAll("\\r\\n|\\r|\\n", "");
    assertThat(text).isEqualTo(output);
    node =
        (Node) xPath.compile("/testng-results/suite/test/class").evaluate(doc, XPathConstants.NODE);
    String expected = xmlSuite.getTests().get(0).getClasses().get(0).getName();
    assertThat(node.getAttributes().getNamedItem("name").getNodeValue()).isEqualTo(expected);
    NodeList nodes =
        (NodeList)
            xPath
                .compile("/testng-results/suite/test/class/test-method")
                .evaluate(doc, XPathConstants.NODESET);
    assertThat(nodes.getLength()).isEqualTo(2);
  }

  private static XmlSuite createChildSuite(String suffix, Class<?> testClass) {
    XmlSuite xmlSuite = createXmlSuite("suite_" + suffix);
    createXmlTest(xmlSuite, "test_" + suffix, testClass);
    return xmlSuite;
  }
}
