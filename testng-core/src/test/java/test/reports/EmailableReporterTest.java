package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.reflect.Method;
import org.testng.IReporter;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.EmailableReporter2;
import test.SimpleBaseTest;
import test.reports.issue3038.AnotherTestCaseSample;
import test.reports.issue3038.ExceptionAwareEmailableReporter;
import test.reports.issue3038.TestCaseSample;
import test.reports.issue3038.TestCaseWithConfigProblemSample;

public class EmailableReporterTest extends SimpleBaseTest {
  @Test(dataProvider = "getReporterInstances", priority = 1)
  public void testReportsNameCustomizationViaRunMethodInvocationAndJVMArguments(
      IReporter reporter, String jvm) {
    runTestViaRunMethod(reporter, jvm);
  }

  @Test(dataProvider = "getReporterInstances", priority = 2)
  public void testReportsNameCustomizationViaRunMethodInvocation(IReporter reporter) {
    runTestViaRunMethod(reporter, null /* no jvm arguments */);
  }

  @Test(dataProvider = "getReporterNames", priority = 3)
  public void testReportsNameCustomizationViaMainMethodInvocation(String clazzName) {
    runTestViaMainMethod(clazzName, null /* no jvm arguments */);
  }

  @Test(dataProvider = "getReporterNames", priority = 4)
  public void testReportsNameCustomizationViaMainMethodInvocationAndJVMArguments(
      String clazzName, String jvm) {
    runTestViaMainMethod(clazzName, jvm);
  }

  @Test
  public void ensureEmailableReportsDontThrowExceptions() {
    runTest(TestCaseSample.class);
  }

  @Test
  public void ensureEmailableReportsDontThrowExceptionsWhenMultipleClassesAreUsed() {
    runTest(TestCaseSample.class, AnotherTestCaseSample.class);
  }

  @Test
  public void ensureEmailableReportsDontThrowExceptionsWhenConfigsHaveErrors() {
    runTest(
        TestCaseSample.class, TestCaseWithConfigProblemSample.class, AnotherTestCaseSample.class);
  }

  private static void runTest(Class<?>... classes) {
    TestNG testng = create(classes);
    ExceptionAwareEmailableReporter reporter = new ExceptionAwareEmailableReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.hasError).isFalse();
  }

  @DataProvider(name = "getReporterInstances")
  public Object[][] getReporterInstances(Method method) {
    if (method.getName().toLowerCase().contains("jvmarguments")) {
      return new Object[][] {{new EmailableReporter2(), "emailable.report2.name"}};
    }
    return new Object[][] {{new EmailableReporter2()}};
  }

  @DataProvider(name = "getReporterNames")
  public Object[][] getReporterNames(Method method) {
    if (method.getName().toLowerCase().contains("jvmarguments")) {
      return new Object[][] {{EmailableReporter2.class.getName(), "emailable.report2.name"}};
    }
    return new Object[][] {{EmailableReporter2.class.getName()}};
  }

  private void runTestViaMainMethod(String clazzName, String jvm) {
    String name = Long.toString(System.currentTimeMillis());
    File output = createDirInTempDir(name);
    String filename = "report" + name + ".html";
    String[] args = {
      "-d",
      output.getAbsolutePath(),
      "-reporter",
      clazzName + ":fileName=" + filename,
      "src/test/resources/1332.xml"
    };
    try {
      if (jvm != null) {
        System.setProperty(jvm, filename);
      }
      TestNG.privateMain(args, null);
    } catch (SecurityException t) {
      // Gobble Security exception
    } finally {
      if (jvm != null) {
        // reset the jvm arguments
        System.setProperty(jvm, "");
      }
    }
    File actual = new File(output.getAbsolutePath(), filename);
    assertThat(actual).exists();
  }

  private void runTestViaRunMethod(IReporter reporter, String jvm) {
    String name = Long.toString(System.currentTimeMillis());
    File output = createDirInTempDir(name);
    String filename = "report" + name + ".html";
    if (jvm != null) {
      System.setProperty(jvm, filename);
    }
    try {
      TestNG testNG = create();
      testNG.setOutputDirectory(output.getAbsolutePath());
      if (reporter instanceof EmailableReporter2) {
        ((EmailableReporter2) reporter).setFileName(filename);
      }
      testNG.addListener((ITestNGListener) reporter);
      testNG.setTestClasses(new Class[] {ReporterSample.class});
      testNG.run();
    } finally {
      if (jvm != null) {
        // reset the jvm argument if it was set
        System.setProperty(jvm, "");
      }
    }

    File actual = new File(output.getAbsolutePath(), filename);
    assertThat(actual).exists();
  }
}
