package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.MethodInstance;
import org.testng.internal.WrappedTestNGMethod;
import org.testng.reporters.RuntimeBehavior;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import test.SimpleBaseTest;
import test.reports.issue2171.TestClassExample;
import test.reports.issue2886.HydeTestSample;
import test.reports.issue2886.JekyllTestSample;
import test.simple.SimpleSample;

public class XmlReporterTest extends SimpleBaseTest {
  @Test(description = "GITHUB-1566")
  public void testMethod() throws IOException {
    File file = runTest(Issue1566Sample.class);
    boolean flag = false;
    Pattern pattern = Pattern.compile("\\p{Cc}");
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (pattern.matcher(line).find()) {
          flag = true;
        }
      }
    }
    assertThat(flag).as("Should not have found a control character").isFalse();
  }

  @Test(description = "GITHUB1659")
  public void ensureStackTraceHasLineFeedsTest() throws Exception {
    File file = runTest(Issue1659Sample.class);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(file);
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//full-stacktrace/text()";
    String data = (String) xPath.compile(expression).evaluate(doc, XPathConstants.STRING);
    data = data.trim();
    assertThat(data.split("\n").length)
        .as("Line feeds and carriage returns should not have been removed")
        .isGreaterThan(1);
  }

  @Test(description = "GITHUB-2171")
  public void ensureCustomisationOfReportIsSupported() throws Exception {
    File file = runTest("issue_2171.xml", null, TestClassExample.class);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(file);
    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//test/class/test-method/file/@path";
    String data = (String) xPath.compile(expression).evaluate(doc, XPathConstants.STRING);
    assertThat(data.trim()).isEqualTo("issue2171.html");
  }

  @Test
  public void ensureReportGenerationWhenTestMethodIsWrappedWithWrappedTestNGMethod() {
    File file =
        runTest(
            testng ->
                testng.setMethodInterceptor(
                    (methods, context) ->
                        methods.stream()
                            .flatMap(
                                t ->
                                    Stream.of(
                                        t,
                                        new MethodInstance(new WrappedTestNGMethod(t.getMethod()))))
                            .collect(Collectors.toList())));
    assertThat(file.exists()).isTrue();
  }

  @Test(description = "GITHUB-2886")
  public void ensureConfigurationMethodsAreNotCountedAsSkippedInXmlReports() throws Exception {
    File file =
        runTest(RuntimeBehavior.FILE_NAME, null, JekyllTestSample.class, HydeTestSample.class);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(file);
    XPath xPath = XPathFactory.newInstance().newXPath();
    Node node = (Node) xPath.compile("/testng-results").evaluate(doc, XPathConstants.NODE);
    int ignored = Integer.parseInt(node.getAttributes().getNamedItem("ignored").getNodeValue());
    int total = Integer.parseInt(node.getAttributes().getNamedItem("total").getNodeValue());
    int passed = Integer.parseInt(node.getAttributes().getNamedItem("passed").getNodeValue());
    int failed = Integer.parseInt(node.getAttributes().getNamedItem("failed").getNodeValue());
    assertThat(ignored).isZero();
    assertThat(total).isEqualTo(2);
    assertThat(passed).isEqualTo(2);
    assertThat(failed).isZero();
  }

  private static File runTest(Class<?> clazz) {
    return runTest(RuntimeBehavior.FILE_NAME, null, clazz);
  }

  private static File runTest(Consumer<TestNG> customizer) {
    return runTest(RuntimeBehavior.FILE_NAME, customizer, SimpleSample.class);
  }

  private static File runTest(String fileName, Consumer<TestNG> customizer, Class<?>... clazz) {
    String suiteName = UUID.randomUUID().toString();
    File fileLocation = createDirInTempDir(suiteName);
    TestNG testng = create(fileLocation.toPath(), clazz);
    testng.setUseDefaultListeners(true);
    if (customizer != null) {
      customizer.accept(testng);
    }
    testng.run();
    return new File(fileLocation, fileName);
  }
}
