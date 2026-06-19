package test.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.testhelper.OutputDirectoryPatch;
import org.testng.xml.XmlSuite;

public class IncludedExcludedTest {

  private TestNG m_tng;

  @BeforeMethod
  public void init() {
    m_tng = new TestNG();
    m_tng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    m_tng.setUseDefaultListeners(false);
  }

  @Test(description = "First test method")
  public void verifyIncludedExcludedCount1() {
    m_tng.setTestClasses(new Class[] {IncludedExcludedSampleTest.class});
    m_tng.setGroups("a");
    m_tng.addListener(
        (ITestNGListener) new MyReporter(new String[] {"test3"}, new String[] {"test1", "test2"}));
    m_tng.run();
  }

  @Test(description = "Second test method")
  public void verifyIncludedExcludedCount2() {
    m_tng.setTestClasses(new Class[] {IncludedExcludedSampleTest.class});
    m_tng.addListener(
        (ITestNGListener)
            new MyReporter(
                new String[] {
                  "beforeSuite",
                  "beforeTest",
                  "beforeTestClass",
                  "beforeTestMethod",
                  "test1",
                  "beforeTestMethod",
                  "test3"
                },
                new String[] {"test2"}));
    m_tng.run();
  }
}

class MyReporter implements IReporter {

  private String[] m_included;
  private String[] m_excluded;

  public MyReporter(String[] included, String[] excluded) {
    m_included = included;
    m_excluded = excluded;
  }

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    assertThat(suites).hasSize(1);
    ISuite suite = suites.get(0);

    List<IInvokedMethod> invoked = suite.getAllInvokedMethods();
    assertThat(invoked).hasSameSizeAs(m_included);
    for (String s : m_included) {
      assertThat(containsInvokedMethod(invoked, s)).isTrue();
    }

    Collection<ITestNGMethod> excluded = suite.getExcludedMethods();
    assertThat(excluded).hasSameSizeAs(m_excluded);
    for (String s : m_excluded) {
      assertThat(containsMethod(excluded, s)).isTrue();
    }
  }

  private boolean containsMethod(Collection<ITestNGMethod> invoked, String string) {
    for (ITestNGMethod m : invoked) {
      if (m.getMethodName().equals(string)) {
        return true;
      }
    }

    return false;
  }

  private static boolean containsInvokedMethod(Collection<IInvokedMethod> invoked, String string) {
    for (IInvokedMethod m : invoked) {
      if (m.getTestMethod().getMethodName().equals(string)) {
        return true;
      }
    }

    return false;
  }
}
