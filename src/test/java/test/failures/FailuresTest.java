package test.failures;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import org.testng.xml.XmlSuite;

import test.TestHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FailuresTest extends BaseFailuresTest {

  private static final String suiteName = "TmpSuite";

  private static final String[] expected =
      new String[] {
        "<class name=\"test.failures.Child\">",
        "<include name=\"fail\"/>",
        "<include name=\"failFromBase\"/>",
      };

  @Test
  public void shouldIncludeFailedMethodsFromBaseClass() throws IOException {
    Path tempDirectory = Files.createTempDirectory("temp-testng-");
    XmlSuite suite = createXmlSuite(suiteName, "TmpTest", Child.class);
    TestNG tng = create(tempDirectory, suite);
    tng.addListener(new FailedReporter());
    tng.run();

    verify(tempDirectory, suiteName, expected);
  }

  private static final String[] expectedIncludes =
      new String[] {"<include name=\"f1\"/>", "<include name=\"f2\"/>"};

  @Test(enabled = false)
  public void shouldIncludeDependentMethods() throws IOException {
    Path tempDirectory = Files.createTempDirectory("temp-testng-");
    XmlSuite suite = TestHelper.createSuite("test.failures.DependentTest", suiteName);
    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();

    verify(tempDirectory, suiteName, expectedIncludes);
  }

  private static final String[] expectedParameter =
      new String[] {"<parameter name=\"first-name\" value=\"Cedric\"/>"};

  @Test(enabled = false)
  public void shouldIncludeParameters() throws IOException {
    Path tempDirectory = Files.createTempDirectory("temp-testng-");
    XmlSuite suite = TestHelper.createSuite("test.failures.Child", suiteName);
    Map<String, String> params = new HashMap<>();
    params.put("first-name", "Cedric");
    params.put("last-name", "Beust");
    suite.setParameters(params);

    TestNG tng = TestHelper.createTestNG(suite);
    tng.run();

    verify(tempDirectory, suiteName, expectedParameter);
  }
}
