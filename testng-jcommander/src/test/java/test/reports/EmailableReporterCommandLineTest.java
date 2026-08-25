package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.EmailableReporter2;
import test.SimpleBaseTest;

/**
 * The command line half of {@code test.reports.EmailableReporterTest}, which stays in {@code
 * testng-core} for the cases that drive the Java API. These are the only tests covering the {@code
 * -reporter} string to {@code ReporterConfig} conversion.
 */
public class EmailableReporterCommandLineTest extends SimpleBaseTest {

  @Test(dataProvider = "getReporterNames", priority = 1)
  public void testReportsNameCustomizationViaMainMethodInvocation(String clazzName) {
    runTestViaMainMethod(clazzName, null /* no jvm arguments */);
  }

  @Test(dataProvider = "getReporterNames", priority = 2)
  public void testReportsNameCustomizationViaMainMethodInvocationAndJVMArguments(
      String clazzName, String jvm) {
    runTestViaMainMethod(clazzName, jvm);
  }

  @DataProvider(name = "getReporterNames")
  public Object[][] getReporterNames(Method method) {
    if (method.getName().toLowerCase(Locale.ROOT).contains("jvmarguments")) {
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
    String previous = jvm == null ? null : System.getProperty(jvm);
    try {
      if (jvm != null) {
        System.setProperty(jvm, filename);
      }
      TestNG.privateMain(args, null);
    } catch (SecurityException t) {
      // Gobble Security exception
    } finally {
      if (jvm != null) {
        // Put the property back the way it was, rather than leaving an empty value behind.
        if (previous == null) {
          System.clearProperty(jvm);
        } else {
          System.setProperty(jvm, previous);
        }
      }
    }
    File actual = new File(output.getAbsolutePath(), filename);
    assertThat(actual).exists();
  }
}
