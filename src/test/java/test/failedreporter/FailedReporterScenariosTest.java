package test.failedreporter;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import test.BaseTest;

import java.io.File;
import java.util.UUID;

public class FailedReporterScenariosTest extends BaseTest {
    private File tempDir = new File(System.getProperty("java.io.tmpdir"));

    @Test
    public void testFileCreationSkipWhenNoFailuresExist() {
        File fileLocation = runTests(false);
        try {
            Assert.assertFalse(getLocation(fileLocation).exists());
        } finally {
            if (fileLocation.exists()) {
                deleteRecursive(fileLocation);
            }
        }
    }

    @Test
    public void testFileCreationWhenFailuresExist() {
        File fileLocation = runTests(true);
        try {
            Assert.assertTrue(getLocation(fileLocation).exists());
        } finally {
            if (fileLocation.exists()) {
                deleteRecursive(fileLocation);
            }
        }
    }

    private File getLocation(File fileLocation) {
        String name = fileLocation.getAbsolutePath() + File.separator + FailedReporter.TESTNG_FAILED_XML;
        return new File(name);
    }

    private File runTests(boolean simulateFailures) {
        String suiteName = UUID.randomUUID().toString();
        File fileLocation = new File(tempDir, suiteName);
        if (! fileLocation.exists()) {
            fileLocation.mkdirs();
        }
        TestNG testNG = new TestNG();
        Class cls = FailedReporterLocalTestClass.WithoutFailure.class;
        if (simulateFailures) {
            cls = FailedReporterLocalTestClass.WithFailure.class;
        }
        testNG.setTestClasses(new Class[] {cls});
        testNG.setOutputDirectory(fileLocation.getAbsolutePath());
        try {
            testNG.run();
        } catch (AssertionError e) {
            //catch all assertion failures. Our intent is not assertions of the test class.
        }
        return fileLocation;
    }

    private static void deleteRecursive(File path) {
        if (! path.exists()) {
            return;
        }
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (null != files) {
                for (File f : files) {
                    deleteRecursive(f);
                }
            }
        }
        path.delete();

    }

}
