package test.reports;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.reporters.FailedReporter;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.xml.sax.SAXException;

import test.SimpleBaseTest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class FailedReporterTest extends SimpleBaseTest {

  private static final String XML = "<suite name=\"Suite\">\n"
      + "<parameter name=\"n\" value=\"42\"/>\n"
      + "<test name=\"Test\">\n"
      + "<classes>\n"
      + "<parameter name=\"o\" value=\"43\"/>\n"
      + "<class name=\"test.reports.FailedSampleTest\">\n"
      + "<parameter name=\"p\" value=\"44\"/>\n"
      + "</class>"
      + "</classes>"
      + "</test></suite>";

  @Test
  public void failedFile() throws ParserConfigurationException, SAXException, IOException {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    Collection<XmlSuite> suites = 
        new Parser(new ByteArrayInputStream(XML.getBytes())).parse();
    tng.setXmlSuites(Lists.newArrayList(suites));
    TestListenerAdapter tla = new TestListenerAdapter();
    File f = new File("/tmp");
    tng.setOutputDirectory(f.getAbsolutePath());
    tng.addListener(tla);
    tng.run();

    Collection<XmlSuite> failedSuites =
        new Parser(new File(f, FailedReporter.TESTNG_FAILED_XML).getAbsolutePath()).parse();
    XmlSuite failedSuite = failedSuites.iterator().next();
    Assert.assertEquals("42", failedSuite.getParameter("n"));

    XmlTest test = failedSuite.getTests().get(0);
    Assert.assertEquals("43", test.getParameter("o"));

    XmlClass c = test.getClasses().get(0);
    Assert.assertEquals("44", c.getAllParameters().get("p"));
  }
}
