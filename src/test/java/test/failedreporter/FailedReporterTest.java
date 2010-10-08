package test.failedreporter;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import test.BaseTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FailedReporterTest extends BaseTest {
  private File mTempDirectory;

  @BeforeMethod
  public void setUp() {
    File slashTmpDir = new File(System.getProperty("java.io.tmpdir"));
    mTempDirectory = new File(slashTmpDir, "testng-tmp-" + System.currentTimeMillis() % 1000);
    mTempDirectory.mkdirs();
    mTempDirectory.deleteOnExit();
  }

  @AfterMethod
  public void tearDown() {
    deleteDir(mTempDirectory);
  }

  @Test
  public void failedAndSkippedMethodsShouldBeIncluded() throws IOException {
    testFailedReporter(FailedReporterSampleTest.class, new String[] { "f1", "f2" },
        "<include name=\"%s\"" + "\"/>");   }

  @Test
  public void failedMethodWithDataProviderShouldHaveInvocationNumbers() throws IOException {
    testFailedReporter(FailedReporter2SampleTest.class, new String[] { "f1" },
        "<include invocationNumbers=\"1\" name=\"%s\"" + "\"/>");
  }

  private void testFailedReporter(Class<?> cls, String[] expectedMethods, String expectedLine) {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { cls });
    tng.setOutputDirectory(mTempDirectory.getAbsolutePath());
    tng.run();

    File failed = new File(mTempDirectory, "testng-failed.xml");
    for (String s : expectedMethods) {
      List<String> resultLines = Lists.newArrayList();
      grep(failed, expectedLine.format(s), resultLines);
      Assert.assertEquals(1, resultLines.size());
    }

  }
}
