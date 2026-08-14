package test.failedreporter.issue3111;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * {@code invocation-numbers} selects rows of a test method's own data provider -- that is what
 * {@code ITestNGMethod} documents and the only thing TestNG reads it back as. The instance a
 * {@code @Factory} produced used to be written into that same attribute, so a failed factory
 * powered run produced a {@code testng-failed.xml} that looked right and re-ran everything, and for
 * a method that also had a data provider it re-ran the wrong rows.
 *
 * <p>The two axes are now separate, and {@link
 * #theGeneratedSuiteRerunsOnlyTheInstancesThatFailed()} is the one that gives the split its point:
 * the regenerated suite is executed, not just inspected.
 */
public class FactoryInstanceFailedReporterTest extends SimpleBaseTest {

  private File outputDir;

  @BeforeMethod
  public void setUp() {
    outputDir = createDirInTempDir("testng-3111-" + System.currentTimeMillis() % 1000);
  }

  @AfterMethod
  public void tearDown() {
    deleteDir(outputDir);
  }

  @Test(description = "GITHUB-3111")
  public void failedFactoryInstancesGoToTheirOwnAttribute() throws Exception {
    // No data provider on f1 itself, so there is no invocation number to record: only the failing
    // factory instance, which used to be smuggled into invocation-numbers.
    run(test.failedreporter.issue2517.DataProviderWithFactoryFailedReporterSample.class);

    assertThat(includesOfFailedSuite())
        .containsExactly("<include name=\"f1\" factory-instances=\"1\"/>");
  }

  @Test(description = "GITHUB-3111")
  public void bothAxesAreRecordedSideBySide() throws Exception {
    run(FactoryAndMethodDataProviderSample.class);

    // f1 failed on (instance 1, row 2) and (instance 2, row 0). Each axis keeps its own attribute;
    // the factory index no longer overwrites the data provider row.
    assertThat(includesOfFailedSuite())
        .containsExactly(
            "<include name=\"f1\" invocation-numbers=\"0 2\" factory-instances=\"1 2\"/>");
  }

  @Test(description = "GITHUB-3111")
  public void theGeneratedSuiteRerunsOnlyTheInstancesThatFailed() throws Exception {
    ExecutedPairs.clear();
    run(RerunSample.class);

    // Instances 1 and 3 fail, so the whole factory ran: 4 instances x 1 method.
    assertThat(ExecutedPairs.instances()).containsExactly(0, 1, 2, 3);
    assertThat(includesOfFailedSuite())
        .containsExactly("<include name=\"f1\" factory-instances=\"1 3\"/>");

    ExecutedPairs.clear();
    File failed = new File(outputDir, "testng-failed.xml");
    File rerunDir = createDirInTempDir("testng-3111-rerun");
    TestNG rerun = create();
    rerun.setOutputDirectory(rerunDir.getAbsolutePath());
    rerun.setTestSuites(Collections.singletonList(failed.getAbsolutePath()));
    rerun.run();

    // The factory still produces every instance -- the attribute filters afterwards, exactly as
    // invocation-numbers filters after the data provider has run -- but only the two that failed
    // execute their test method.
    assertThat(ExecutedPairs.instances()).containsExactly(1, 3);
  }

  private void run(Class<?> sample) {
    TestNG tng = create(outputDir.toPath(), sample);
    tng.setUseDefaultListeners(true);
    tng.run();
  }

  /** @return - The {@code <include>} lines of the generated suite, trimmed, in file order. */
  private List<String> includesOfFailedSuite() throws Exception {
    File failed = new File(outputDir, "testng-failed.xml");
    assertThat(failed).exists();
    return Files.readAllLines(failed.toPath(), StandardCharsets.UTF_8).stream()
        .map(String::trim)
        .filter(it -> it.startsWith("<include "))
        .collect(java.util.stream.Collectors.toList());
  }
}
