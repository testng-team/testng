package test.failedreporter.skippedposition;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

/**
 * {@code invocation-numbers} indexes the data provider's own rows, so it only survives a re-run if
 * a filtered run still numbers the rows it kept the way the unfiltered run did. That is what {@code
 * org.testng.internal.FilteredParameters} answering {@code null} for an excluded position buys:
 * {@code MethodRunner} counts those placeholders, so the row that does run keeps its original
 * index.
 *
 * <p>Nothing else pins that. Every other {@code indices} test asserts on the row's payload, which
 * is selected by source index and therefore unchanged if the placeholders were dropped. The
 * regression only shows on the <em>second</em> generated {@code testng-failed.xml} -- the first one
 * comes from an unfiltered run, where no placeholder is ever emitted.
 */
public class SkippedPositionRerunTest extends SimpleBaseTest {

  private File outputDir;

  @BeforeMethod
  public void setUp() {
    outputDir = createDirInTempDir("testng-skipped-position-" + System.currentTimeMillis() % 1000);
  }

  @AfterMethod
  public void tearDown() {
    deleteDir(outputDir);
  }

  @Test
  public void aRegeneratedFailedSuiteKeepsTheDataProviderNumbering() throws Exception {
    TestNG tng = create(outputDir.toPath(), SkippedPositionSample.class);
    tng.setUseDefaultListeners(true);
    tng.run();

    // Row "b" is the second of three.
    assertThat(includesOf(outputDir))
        .containsExactly("<include name=\"f1\" invocation-numbers=\"1\"/>");

    File rerunDir = createDirInTempDir("testng-skipped-position-rerun");
    try {
      TestNG rerun = create();
      rerun.setUseDefaultListeners(true);
      rerun.setOutputDirectory(rerunDir.getAbsolutePath());
      rerun.setTestSuites(
          Collections.singletonList(new File(outputDir, "testng-failed.xml").getAbsolutePath()));
      rerun.run();

      // The filtered run still walks all three positions and runs only the second. Drop the
      // placeholders and the surviving row would report index 0, so this file would say 0 and a
      // third run would execute row "a" instead.
      assertThat(includesOf(rerunDir))
          .containsExactly("<include name=\"f1\" invocation-numbers=\"1\"/>");
    } finally {
      deleteDir(rerunDir);
    }
  }

  /** @return - The {@code <include>} lines of the suite generated in {@code dir}, in file order. */
  private static List<String> includesOf(File dir) throws Exception {
    File failed = new File(dir, "testng-failed.xml");
    assertThat(failed).exists();
    return Files.readAllLines(failed.toPath(), StandardCharsets.UTF_8).stream()
        .map(String::trim)
        .filter(it -> it.startsWith("<include "))
        .collect(Collectors.toList());
  }
}
