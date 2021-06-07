package test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestHelper {
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
    return Files.createTempDirectory("testng-tmp-");
  }
}
