package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import org.testng.xml.internal.Parser;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import test.SimpleBaseTest;

public class FailedReporterTest extends SimpleBaseTest {

  @Test
  public void failedFile() throws ParserConfigurationException, SAXException, IOException {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    xmlSuite.getParameters().put("n", "42");

    XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
    xmlTest.addParameter("o", "43");

    XmlClass xmlClass = createXmlClass(xmlTest, SimpleFailedSample.class);
    xmlClass.getLocalParameters().put("p", "44");

    TestNG tng = create(xmlSuite);

    Path temp = Files.createTempDirectory("tmp");
    tng.setOutputDirectory(temp.toAbsolutePath().toString());
    tng.addListener((ITestNGListener) new FailedReporter());
    tng.run();

    Collection<XmlSuite> failedSuites =
        new Parser(temp.resolve(FailedReporter.TESTNG_FAILED_XML).toAbsolutePath().toString())
            .parse();
    XmlSuite failedSuite = failedSuites.iterator().next();
    Assert.assertEquals("42", failedSuite.getParameter("n"));

    XmlTest failedTest = failedSuite.getTests().get(0);
    Assert.assertEquals("43", failedTest.getParameter("o"));

    XmlClass failedClass = failedTest.getClasses().get(0);
    Assert.assertEquals("44", failedClass.getAllParameters().get("p"));
  }

  @Test(description = "ISSUE-2445")
  public void testParameterPreservationWithFactory() throws IOException {
    final SuiteXmlParser parser = new SuiteXmlParser();
    final String testSuite = "src/test/resources/xml/github2445/test-suite.xml";
    final String expectedResult = "src/test/resources/xml/github2445/expected-failed-report.xml";
    final XmlSuite xmlSuite = parser.parse(testSuite, new FileInputStream(testSuite), true);
    final TestNG tng = create(xmlSuite);

    final Path temp = Files.createTempDirectory("tmp");
    tng.setOutputDirectory(temp.toAbsolutePath().toString());
    tng.addListener(new FailedReporter());
    tng.run();

    final Diff myDiff =
        DiffBuilder.compare(Input.fromFile(expectedResult))
            .withTest(
                Input.fromFile(
                    temp.resolve(FailedReporter.TESTNG_FAILED_XML).toAbsolutePath().toString()))
            .checkForSimilar()
            .ignoreWhitespace()
            .build();

    assertThat(myDiff).matches((it) -> !it.hasDifferences(), "!it.hasDifferences()");
  }
}
