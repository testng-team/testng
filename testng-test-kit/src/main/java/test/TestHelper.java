package test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestHelper {

  /**
   * Writes a suite to a temporary file and returns its path, the way a command line test needs it.
   *
   * @param suite the suite to serialize.
   * @return the absolute path of the written file.
   */
  public static String writeSuiteToTempFile(XmlSuite suite) throws IOException {
    return writeSuiteToTempFile(suite.toXml());
  }

  /**
   * @param suiteXml the already serialized suite.
   * @return the absolute path of the written file.
   */
  public static String writeSuiteToTempFile(String suiteXml) throws IOException {
    Path file = Files.createTempFile("testng", ".xml");
    file.toFile().deleteOnExit();
    Files.write(file, suiteXml.getBytes(StandardCharsets.UTF_8));
    return file.toFile().getAbsolutePath();
  }

  /**
   * Asserts that exactly the given test methods passed, in order.
   *
   * @param found the results collected by a listener.
   * @param expected the expected method names.
   */
  public static void assertPassedTestNames(List<ITestResult> found, String... expected) {
    assertTestNames("passed tests", found, expected);
  }

  /**
   * Asserts that exactly the given test methods failed, in order.
   *
   * @param found the results collected by a listener.
   * @param expected the expected method names.
   */
  public static void assertFailedTestNames(List<ITestResult> found, String... expected) {
    assertTestNames("failed tests", found, expected);
  }

  private static void assertTestNames(
      String description, List<ITestResult> found, String... expected) {
    assertThat(found.stream().map(ITestResult::getName).toArray(String[]::new))
        .describedAs(description)
        .isEqualTo(expected);
  }

  /**
   * Asserts the configuration and skip counters a run produced.
   *
   * @param tla the listener that observed the run.
   */
  public static void assertCounts(
      TestListenerAdapter tla,
      int configurationFailures,
      int configurationSkips,
      int skippedTests) {
    assertThat(tla.getConfigurationFailures())
        .describedAs("configuration failures")
        .hasSize(configurationFailures);
    assertThat(tla.getConfigurationSkips())
        .describedAs("configuration skips")
        .hasSize(configurationSkips);
    assertThat(tla.getSkippedTests()).describedAs("skipped tests").hasSize(skippedTests);
  }

  /*
   * TestNG issues a warning if the XML misses DOCTYPE, so here's a common header for
   * xml suites generated in the tests.
   */
  public static final String SUITE_XML_HEADER =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">\n";

  public static XmlSuite createSuite(String cls, String suiteName) {
    XmlSuite result = new XmlSuite();
    result.setName(suiteName);

    XmlTest test = new XmlTest(result);
    test.setName("TmpTest");
    test.setXmlClasses(Collections.singletonList(new XmlClass(cls)));

    return result;
  }

  public static TestNG createTestNG() throws IOException {
    return createTestNG(createRandomDirectory());
  }

  public static TestNG createTestNG(XmlSuite suite) throws IOException {
    return createTestNG(suite, createRandomDirectory());
  }

  public static TestNG createTestNG(XmlSuite suite, Path outputDir) {
    TestNG result = createTestNG(outputDir);
    result.setXmlSuites(Collections.singletonList(suite));
    return result;
  }

  private static TestNG createTestNG(Path outputDir) {
    TestNG result = new TestNG();
    result.setOutputDirectory(outputDir.toAbsolutePath().toString());

    return result;
  }

  public static Path createRandomDirectory() throws IOException {
    Path directory = Files.createTempDirectory("testng-tmp-");
    directory.toFile().deleteOnExit();
    return directory;
  }
}
