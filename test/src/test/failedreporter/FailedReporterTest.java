package test.failedreporter;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.v6.Lists;

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
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { FailedReporterSampleTest.class });
    tng.setOutputDirectory(mTempDirectory.getAbsolutePath());
    tng.run();

    String[] expected = new String[] { "f1", "f2" };
    File failed = new File(mTempDirectory, "testng-failed.xml");
    for (String s : expected) {
      List<String> resultLines = Lists.newArrayList();
      BaseTest.grep(failed, "<include name=\"" + s + "\"/>", resultLines);
      Assert.assertEquals(1, resultLines.size());
    }

  }
}
