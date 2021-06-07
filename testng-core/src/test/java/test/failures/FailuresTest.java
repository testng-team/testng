package test.failures;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.reporters.FailedReporter;
import org.testng.xml.XmlSuite;
import test.TestHelper;
import test.failures.issue1930.SimpleCliStatus;
import test.failures.issue1930.TestClassSample;

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

  @Test(description = "GITHUB-1930")
  public void testToEnsureThatWeRunOnlyFailedIterationsFromBaseClass() {
    File outputDir = createDirInTempDir("testng-tmp-" + System.currentTimeMillis() % 1000);
    TestNG testng = create(TestClassSample.class);
    testng.setOutputDirectory(outputDir.getAbsolutePath());
    testng.setUseDefaultListeners(true);
    testng.run();
    String file = outputDir.getAbsolutePath() + File.separator + FailedReporter.TESTNG_FAILED_XML;

    // First iteration of running failed tests.
    runIteration(outputDir, file);
    // Second iteration of running failed tests.
    runIteration(outputDir, file);
  }

  private static void runIteration(File outputDir, String file) {
    SimpleCliStatus listener = new SimpleCliStatus();
    TestNG testng = create();
    testng.setTestSuites(Collections.singletonList(file));
    testng.setOutputDirectory(outputDir.getAbsolutePath());
    testng.setUseDefaultListeners(true);
    testng.addListener(listener);
    testng.run();

    Map<String, List<Integer>> expected = Maps.newHashMap();
    expected.put("testPrimeNumberChecker", Arrays.asList(3, 4));
    expected.put("testNumberEquality", Arrays.asList(2, 3));
    assertThat(listener.getFailedTests()).containsAllEntriesOf(expected);
  }
}
