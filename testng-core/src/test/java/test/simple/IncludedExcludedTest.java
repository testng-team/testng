package test.simple;

import java.util.Collection;
import java.util.List;
import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import testhelper.OutputDirectoryPatch;

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
    Assert.assertEquals(suites.size(), 1);
    ISuite suite = suites.get(0);

    List<IInvokedMethod> invoked = suite.getAllInvokedMethods();
    Assert.assertEquals(invoked.size(), m_included.length);
    for (String s : m_included) {
      Assert.assertTrue(containsInvokedMethod(invoked, s));
    }

    Collection<ITestNGMethod> excluded = suite.getExcludedMethods();
    Assert.assertEquals(excluded.size(), m_excluded.length);
    for (String s : m_excluded) {
      Assert.assertTrue(containsMethod(excluded, s));
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
