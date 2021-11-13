package test.failedreporter.issue2517;

import java.io.File;
import java.util.List;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import test.SimpleBaseTest;

public class DataProviderWithFactoryFailedReporterTest extends SimpleBaseTest {
  private File mTempDirectory;

  @BeforeMethod
  public void setUp() {
    mTempDirectory = createDirInTempDir("testng-tmp-" + System.currentTimeMillis() % 1000);
  }

  @AfterMethod
  public void tearDown() {
    deleteDir(mTempDirectory);
  }

  @Test
  public void failedMethodWithDataProviderAndFactoryShouldHaveInvocationNumbers() {
    testFailedReporter(
        new String[] {"f1"},
        "<include name=\"%s\" invocation-numbers=\"1\"/>",
        DataProviderWithFactoryFailedReporterSample.class);
  }

  private void testFailedReporter(String[] expectedMethods, String expectedLine, Class<?>... cls) {
    triggerTest(cls);
    runAssertions(mTempDirectory, expectedMethods, expectedLine, 1);
  }

  private void triggerTest(Class<?>... cls) {
    TestNG tng = create(mTempDirectory.toPath(), cls);
    tng.setUseDefaultListeners(true);
    tng.run();
  }

  private static void runAssertions(
      File outputDir, String[] expectedMethods, String expectedLine, int expected) {
    File failed = new File(outputDir, "testng-failed.xml");
    for (String s : expectedMethods) {
      List<String> resultLines = Lists.newArrayList();
      grep(failed, String.format(expectedLine, s), resultLines);
      Assert.assertEquals(resultLines.size(), expected);
    }
  }
}
