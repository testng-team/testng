package test.reports;

import org.testng.CliTestNgRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.EmailableReporter;
import org.testng.reporters.EmailableReporter2;
import test.MySecurityManager;
import test.SimpleBaseTest;
import java.io.File;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailableReporterTest extends SimpleBaseTest {
  private SecurityManager manager;

  @BeforeClass(alwaysRun = true)
  public void setup() {
    manager = System.getSecurityManager();
    System.setSecurityManager(new MySecurityManager(manager));
  }

  @AfterClass(alwaysRun = true)
  public void cleanup() {
    System.setSecurityManager(manager);
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

  @DataProvider(name = "getReporterNames")
  public Object[][] getReporterNames(Method method) {
    if (method.getName().toLowerCase().contains("jvmarguments")) {
      return new Object[][] {
          {EmailableReporter.class.getName(), "emailable.report.name"},
          {EmailableReporter2.class.getName(), "emailable.report2.name"}
      };
    }
    return new Object[][] {
        {EmailableReporter.class.getName()}, {EmailableReporter2.class.getName()}
    };
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
      CliTestNgRunner.Main.main(args);
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
}
