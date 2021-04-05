package test.failedreporter;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import test.SimpleBaseTest;

import java.io.File;
import java.util.UUID;

import static test.failedreporter.FailedReporterLocalTestClass.WithFailure;
import static test.failedreporter.FailedReporterLocalTestClass.WithoutFailure;

public class FailedReporterScenariosTest extends SimpleBaseTest {

  @Test
  public void testFileCreationSkipWhenNoFailuresExist() {
    File fileLocation = runTests(RUN_TYPES.WITHOUT_FAILURES);
    try {
      Assert.assertFalse(getLocation(fileLocation).exists());
    } finally {
      if (fileLocation.exists()) {
        deleteDir(fileLocation);
      }
    }
  }

  @Test
  public void testFileCreationInMixedMode() {
    File fileLocation = runTests(RUN_TYPES.MIXED_MODE);
    runAssertions(fileLocation);
  }

  @Test
  public void testFileCreationWhenFailuresExist() {
    File fileLocation = runTests(RUN_TYPES.WITH_FAILURES);
    runAssertions(fileLocation);
  }

  private void runAssertions(File fileLocation) {
    try {
      FailedReporterTest.runAssertions(
          fileLocation, new String[] {"testMethodWithFailure"}, "<include name=\"%s\"/>");
      Assert.assertTrue(getLocation(fileLocation).exists());
    } finally {
      if (fileLocation.exists()) {
        deleteDir(fileLocation);
      }
    }
  }

  private File getLocation(File fileLocation) {
    String name =
        fileLocation.getAbsolutePath() + File.separator + FailedReporter.TESTNG_FAILED_XML;
    return new File(name);
  }

  private File runTests(RUN_TYPES runType) {
    String suiteName = UUID.randomUUID().toString();
    File fileLocation = createDirInTempDir(suiteName);
    Class[] classes = {};
    switch (runType) {
      case WITH_FAILURES:
        classes = new Class[] {WithFailure.class};
        break;
      case WITHOUT_FAILURES:
        classes = new Class[] {WithoutFailure.class};
        break;
      case MIXED_MODE:
        classes = new Class[] {WithFailure.class, WithoutFailure.class};
    }
    TestNG testNG = create(fileLocation.toPath(), classes);
    testNG.setUseDefaultListeners(true);
    try {
      testNG.run();
    } catch (AssertionError e) {
      // catch all assertion failures. Our intent is not assertions of the test class.
    }
    return fileLocation;
  }

  enum RUN_TYPES {
    WITH_FAILURES,
    WITHOUT_FAILURES,
    MIXED_MODE
  }
}
