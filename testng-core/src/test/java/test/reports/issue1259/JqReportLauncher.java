package test.reports.issue1259;

import java.nio.file.Paths;
import java.util.Collections;
import org.testng.TestNG;
import org.testng.reporters.jq.Main;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.TestHelper;

/**
 * Runs {@link LargeReportSample} under the default HTML reporter and nothing else, which is what
 * makes an OutOfMemoryError attributable to that reporter.
 *
 * <p>Registering only {@link Main} is deliberate, and jq runs first among the default reporters, so
 * leaving the others enabled would let a green jq report be followed by an OutOfMemoryError this
 * test says nothing about. {@code JUnitReportReporter} materialises the same way but one file per
 * test class, so its peak is bounded by the largest class rather than by the run; {@code
 * AbstractXmlReporter} and {@code XMLSuiteResultWriter} do hold a whole suite, in the fragmented
 * modes. Switching those two to the streaming {@code Utils.writeUtf8File} overload is not the
 * one-line change it looks like -- the String overload runs {@code Utils.escapeUnicode} and the
 * XMLStringBuffer one does not, so it would change what they write.
 *
 * <p>This class is a {@code main} rather than a test method because it is started as a child JVM
 * with a heap of its own; see {@code test.reports.issue1259.JqReportMemoryTest}.
 */
public class JqReportLauncher {

  public static final String SUITE_NAME_LENGTH_PROPERTY = "testng.test.issue1259.suiteNameLength";

  public static void main(String[] args) {
    XmlSuite suite = new XmlSuite();
    suite.setName(suiteName());
    XmlTest test = new XmlTest(suite);
    test.setName("large-report");
    test.setXmlClasses(Collections.singletonList(new XmlClass(LargeReportSample.class.getName())));

    TestNG testng = TestHelper.createTestNG(suite, Paths.get(args[0]));
    testng.setUseDefaultListeners(false);
    testng.addListener(new Main());
    testng.run();
    System.out.println("REPORT GENERATED");
  }

  /**
   * The navigator lists every method under the suite it belongs to and names that suite on each
   * one, so the name is report content that grows with the result count while the run retains a
   * single copy of it. That is what separates what the report costs from what the run costs.
   */
  private static String suiteName() {
    StringBuilder name = new StringBuilder("issue1259");
    while (name.length() < Integer.getInteger(SUITE_NAME_LENGTH_PROPERTY, 9)) {
      name.append("-large-report-suite");
    }
    return name.toString();
  }
}
