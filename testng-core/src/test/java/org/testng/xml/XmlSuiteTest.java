package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.internal.Parser;
import test.SimpleBaseTest;

public class XmlSuiteTest extends SimpleBaseTest {

  @Test
  public void testIncludedAndExcludedGroups() {
    XmlSuite suite = new XmlSuite();
    suite.addIncludedGroup("foo");
    suite.addExcludedGroup("bar");
    assertThat(suite.getIncludedGroups()).containsExactly("foo");
    assertThat(suite.getExcludedGroups()).containsExactly("bar");
  }

  @Test
  public void testIncludedAndExcludedGroupsWithRun() {
    XmlRun xmlRun = new XmlRun();
    xmlRun.onInclude("foo");
    xmlRun.onExclude("bar");
    XmlGroups groups = new XmlGroups();
    groups.setRun(xmlRun);
    XmlSuite suite = new XmlSuite();
    suite.setGroups(groups);
    assertThat(suite.getIncludedGroups()).containsExactly("foo");
    assertThat(suite.getExcludedGroups()).containsExactly("bar");
  }

  @Test(dataProvider = "dp", description = "GITHUB-778")
  public void testTimeOut(String timeout, int size, int lineNumber) {
    XmlSuite suite = new XmlSuite();
    suite.setTimeOut(timeout);
    StringReader stringReader = new StringReader(suite.toXml());
    List<String> resultLines = Lists.newArrayList();
    List<Integer> lineNumbers = grep(stringReader, "time-out=\"1000\"", resultLines);
    assertThat(lineNumbers).size().isEqualTo(size);
    assertThat(resultLines).size().isEqualTo(size);
    if (size > 0) {
      assertThat(lineNumbers.get(size - 1)).isEqualTo(lineNumber);
    }
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      {"1000", 1, 2},
      {"", 0, 0}
    };
  }

  @Test(description = "GITHUB-1668")
  public void ensureNoExceptionsAreRaisedWhenMethodSelectorsDefinedAtSuiteLevel()
      throws IOException {
    Parser parser = new Parser("src/test/resources/xml/issue1668.xml");
    List<XmlSuite> suites = parser.parseToList();
    XmlSuite xmlsuite = suites.get(0);
    TestNG testNG = create();
    testNG.setXmlSuites(suites);
    testNG.setUseDefaultListeners(false);
    testNG.run();
    // Trigger a call to "toXml()" to ensure that there is no exception raised.
    assertThat(xmlsuite.toXml()).isNotEmpty();
  }

  @Test(description = "GITHUB-435")
  public void ensureSuiteLevelPackageIsAppliedToAllTests() throws IOException {
    Parser parser = new Parser("src/test/resources/xml/issue435.xml");
    List<XmlSuite> suites = parser.parseToList();
    XmlSuite xmlsuite = suites.get(0);
    assertThat(xmlsuite.getTests().get(0).getClasses().size()).isEqualTo(0);
    TestNG testNG = create();
    testNG.setXmlSuites(suites);
    testNG.setUseDefaultListeners(false);
    testNG.run();
    assertThat(xmlsuite.getTests().get(0).getClasses().size()).isEqualTo(1);
  }

  @Test(description = "GITHUB-1674")
  public void ensureSuiteLevelBeanshellIsAppliedToAllTests() throws IOException {
    PrintStream current = System.out;
    StringOutputStream stream = new StringOutputStream();
    try {
      System.setOut(new PrintStream(stream));
      Parser parser = new Parser("src/test/resources/xml/issue1674.xml");
      List<XmlSuite> suites = parser.parseToList();
      XmlSuite xmlsuite = suites.get(0);
      assertThat(xmlsuite.getTests().get(0).getMethodSelectors().size()).isEqualTo(0);
      TestNG testNG = create();
      testNG.setXmlSuites(suites);
      testNG.setUseDefaultListeners(false);
      testNG.run();
      assertThat(xmlsuite.getTests().get(0).getMethodSelectors().size()).isEqualTo(1);
      assertThat(stream.toString()).contains(Arrays.asList("rajni", "kamal", "mgr"));
    } finally {
      System.setOut(current);
    }
  }

  static class StringOutputStream extends OutputStream {
    private StringBuilder string = new StringBuilder();

    @Override
    public void write(int b) {
      this.string.append((char) b);
    }

    // Netbeans IDE automatically overrides this toString()
    public String toString() {
      return this.string.toString();
    }
  }
}
