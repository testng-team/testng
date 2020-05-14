package test.reports;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.xml.sax.SAXException;
import test.SimpleBaseTest;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

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
        new Parser(temp.resolve(FailedReporter.TESTNG_FAILED_XML).toAbsolutePath().toString()).parse();
    XmlSuite failedSuite = failedSuites.iterator().next();
    Assert.assertEquals("42", failedSuite.getParameter("n"));

    XmlTest failedTest = failedSuite.getTests().get(0);
    Assert.assertEquals("43", failedTest.getParameter("o"));

    XmlClass failedClass = failedTest.getClasses().get(0);
    Assert.assertEquals("44", failedClass.getAllParameters().get("p"));
  }
}
