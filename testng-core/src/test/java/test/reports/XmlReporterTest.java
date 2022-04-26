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
import test.SimpleBaseTest;
import test.reports.issue2171.TestClassExample;
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
    File file = runTest(TestClassExample.class, "issue_2171.xml");
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
            SimpleSample.class,
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

  private static File runTest(Class<?> clazz) {
    return runTest(clazz, RuntimeBehavior.FILE_NAME, null);
  }

  private static File runTest(Class<?> clazz, Consumer<TestNG> customizer) {
    return runTest(clazz, RuntimeBehavior.FILE_NAME, customizer);
  }

  private static File runTest(Class<?> clazz, String fileName) {
    return runTest(clazz, fileName, null);
  }

  private static File runTest(Class<?> clazz, String fileName, Consumer<TestNG> customizer) {
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
