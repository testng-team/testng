package test.reports.issue2069;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  private PrintStream currentError = System.err;
  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

  @BeforeMethod
  public void setup() throws UnsupportedEncodingException {
    PrintStream ps = new PrintStream(baos, true, "UTF-8");
    System.setErr(ps);
  }

  @AfterMethod(alwaysRun = true)
  public void teardown() {
    System.setErr(currentError);
  }

  @Test
  public void ensureNoExceptionsAriseFromReporters() {
    XmlSuite xmlSuite = createXmlSuite("Not Failing TestSuite");
    createXmlTest(xmlSuite, "TestngTest", Dummy4.class);
    createXmlTest(xmlSuite, "TestSuite", Dummy1.class).setJunit(true);
    createXmlTest(xmlSuite, "TestCase", Dummy2.class).setJunit(true);
    TestNG tng = create(xmlSuite);
    tng.setUseDefaultListeners(true);
    tng.run();
    String data = new String(baos.toByteArray(), StandardCharsets.UTF_8);
    assertThat(data).doesNotContain("NullPointerException");
  }
}
