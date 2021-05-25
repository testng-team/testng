package test;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class TestHelper {

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
    result.setVerbose(-1);

    return result;
  }

  public static Path createRandomDirectory() throws IOException {
    return Files.createTempDirectory("testng-tmp-");
  }
}
