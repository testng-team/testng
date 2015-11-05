package test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.ITestRunnerFactory;
import org.testng.SuiteRunner;
import org.testng.TestListenerAdapter;
import org.testng.TestRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.collections.Lists;
import org.testng.internal.Configuration;
import org.testng.internal.IConfiguration;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Base class for tests
 *
 * @author Cedric Beust, May 5, 2004
 *
 */
public class BaseTest extends BaseDistributedTest {
  private static final String m_outputDirectory= "test-output-tests";

  private XmlSuite m_suite= null;
  private ITestRunnerFactory m_testRunnerFactory;
  private IConfiguration m_configuration;

  private Integer m_verbose = null;

  public BaseTest() {
    m_testRunnerFactory= new InternalTestRunnerFactory(this);
    m_configuration = new Configuration();
  }

  private IConfiguration getConfiguration() {
    return m_configuration;
  }

  protected void setDebug() {
    getTest().setVerbose(9);
  }

  protected void setParallel(XmlSuite.ParallelMode parallel) {
    getTest().setParallel(parallel);
  }

  protected void setVerbose(int n) {
    m_verbose = n;
  }

  protected void setTestTimeOut(long n) {
      getTest().setTimeOut(n);
  }

  protected void setSuiteTimeOut(long n) {
      m_suite.setTimeOut(Long.toString(n));
  }

  protected void setJUnit(boolean f) {
    getTest().setJUnit(f);
  }

  protected void setThreadCount(int count) {
    getTest().getSuite().setThreadCount(count);
  }

  private Map<Long, XmlTest> m_tests= new HashMap<>();
  private Map<Long, Map> m_passedTests= new HashMap<>();
  private Map<Long, Map> m_failedTests= new HashMap<>();
  private Map<Long, Map> m_skippedTests= new HashMap<>();
  private Map<Long, XmlTest> m_testConfigs= new HashMap<>();
  private Map<Long, Map> m_passedConfigs= new HashMap<>();
  private Map<Long, Map> m_failedConfigs= new HashMap<>();
  private Map<Long, Map> m_skippedConfigs= new HashMap<>();
  private Map<Long, Map> m_failedButWithinSuccessPercentageTests= new HashMap<>();

  protected Map<String, List<ITestResult>> getTests(Map<Long, Map> map) {
    Map<String, List<ITestResult>> result= map.get(getId());
    if(null == result) {
      result= new HashMap<>();
      map.put(getId(), result);
    }

    return result;
  }

  protected XmlTest getTest() {
    return m_tests.get(getId());
  }

  protected void setTests(Map<Long, Map> map, Map m) {
    map.put(getId(), m);
  }

  public Map<String, List<ITestResult>> getFailedTests() {
    return getTests(m_failedTests);
  }

  public Map<String, List<ITestResult>> getFailedButWithinSuccessPercentageTests() {
    return getTests(m_failedButWithinSuccessPercentageTests);
  }

  public Map<String, List<ITestResult>> getPassedTests() {
    return getTests(m_passedTests);
  }

  public Map<String, List<ITestResult>> getSkippedTests() {
    return getTests(m_skippedTests);
  }

  public Map<String, List<ITestResult>> getFailedConfigs() {
    return getTests(m_failedConfigs);
  }

  public Map<String, List<ITestResult>> getPassedConfigs() {
    return getTests(m_passedConfigs);
  }

  public Map<String, List<ITestResult>> getSkippedConfigs() {
    return getTests(m_skippedConfigs);
  }

  public void setSkippedTests(Map m) {
    setTests(m_skippedTests, m);
  }

  public void setPassedTests(Map m) {
    setTests(m_passedTests, m);
  }

  public void setFailedTests(Map m) {
    setTests(m_failedTests, m);
  }

  public void setFailedButWithinSuccessPercentageTests(Map m) {
    setTests(m_failedButWithinSuccessPercentageTests, m);
  }

  public void setSkippedConfigs(Map m) {
    setTests(m_skippedConfigs, m);
  }

  public void setPassedConfigs(Map m) {
    setTests(m_passedConfigs, m);
  }

  public void setFailedConfigs(Map m) {
    setTests(m_failedConfigs, m);
  }


  protected void run() {
    assert null != getTest() : "Test wasn't set, maybe @Configuration methodSetUp() was never called";
    setPassedTests(new HashMap());
    setFailedTests(new HashMap());
    setSkippedTests(new HashMap());
    setPassedConfigs(new HashMap());
    setFailedConfigs(new HashMap());
    setSkippedConfigs(new HashMap());
    setFailedButWithinSuccessPercentageTests(new HashMap());

    m_suite.setVerbose(m_verbose != null ? m_verbose : 0);
    SuiteRunner suite = new SuiteRunner(m_configuration,
        m_suite, m_outputDirectory, m_testRunnerFactory);

    suite.run();
  }

  protected void addMethodSelector(String className, int priority) {
    XmlMethodSelector methodSelector= new XmlMethodSelector();
    methodSelector.setName(className);
    methodSelector.setPriority(priority);
    getTest().getMethodSelectors().add(methodSelector);
  }

  protected XmlClass addClass(Class<?> cls) {
    return addClass(cls.getName());
  }

  protected XmlClass addClass(String className) {
    XmlClass result= new XmlClass(className);
    getTest().getXmlClasses().add(result);

    return result;
  }

  protected void setBeanShellExpression(String expression) {
    getTest().setBeanShellExpression(expression);
  }

  protected void addPackage(String pkgName, String[] included, String[] excluded) {
    XmlPackage pkg= new XmlPackage();
    pkg.setName(pkgName);
    pkg.getInclude().addAll(Arrays.asList(included));
    pkg.getExclude().addAll(Arrays.asList(excluded));
    getTest().getSuite().getXmlPackages().add(pkg);
  }

  private XmlClass findClass(String className) {
    for(XmlClass cl : getTest().getXmlClasses()) {
      if(cl.getName().equals(className)) {
        return cl;
      }
    }

    XmlClass result= addClass(className);

    return result;
  }

  public void addIncludedMethod(String className, String m) {
    XmlClass xmlClass= findClass(className);
    xmlClass.getIncludedMethods().add(new XmlInclude(m));
    getTest().getXmlClasses().add(xmlClass);
  }

  public void addExcludedMethod(String className, String m) {
    XmlClass xmlClass= findClass(className);
    xmlClass.getExcludedMethods().add(m);
    getTest().getXmlClasses().add(xmlClass);
  }

  public void addIncludedGroup(String g) {
    getTest().addIncludedGroup(g);
  }

  public void addExcludedGroup(String g) {
    getTest().addExcludedGroup(g);
  }

  public void addMetaGroup(String mg, List<String> l) {
    getTest().getMetaGroups().put(mg, l);
  }

  public void addMetaGroup(String mg, String n) {
    List<String> l= new ArrayList<>();
    l.add(n);
    addMetaGroup(mg, l);
  }

  public void setParameter(String key, String value) {
    getTest().addParameter(key, value);
  }

//  @Configuration(beforeTestMethod = true, groups = { "init", "initTest"})
  @BeforeMethod(groups= { "init", "initTest" })
  public void methodSetUp() {
    m_suite= new XmlSuite();
    m_suite.setName("Internal_suite");
    XmlTest xmlTest= new XmlTest(m_suite);
    xmlTest.setName("Internal_test_failures_are_expected");
    m_tests.put(getId(), xmlTest);
  }

  private void addTest(Map<String, List<ITestResult>> tests, ITestResult t) {
    List<ITestResult> l= tests.get(t.getMethod().getMethodName());
    if(null == l) {
      l= new ArrayList<>();
      tests.put(t.getMethod().getMethodName(), l);
    }
    l.add(t);
  }

  public void addPassedTest(ITestResult t) {
    addTest(getPassedTests(), t);
  }

  public void addFailedTest(ITestResult t) {
    addTest(getFailedTests(), t);
  }

  public void addFailedButWithinSuccessPercentageTest(ITestResult t) {
    addTest(getFailedButWithinSuccessPercentageTests(), t);
  }

  public void addSkippedTest(ITestResult t) {
    addTest(getSkippedTests(), t);
  }

  public void addPassedConfig(ITestResult t) {
    addTest(getPassedConfigs(), t);
  }

  public void addFailedConfig(ITestResult t) {
    addTest(getFailedConfigs(), t);
  }

  public void addSkippedConfig(ITestResult t) {
    addTest(getSkippedConfigs(), t);
  }

  private void ppp(String s) {
    System.out.println("[BaseTest " + getId() + "] " + s);
  }

  protected Long getId() {
    return 42L;
//    long result = Thread.currentThread().getId();
////    System.out.println("RETURNING ID " + result);
//    return result;
  }

  public XmlSuite getSuite() {
    return m_suite;
  }

  public void setSuite(XmlSuite suite) {
    m_suite = suite;
  }

  /**
   * Used for instanceCount testing, when we need to look inside the
   * TestResult to count the various SUCCESS/FAIL/FAIL_BUT_OK
   */
  protected void verifyResults(Map<String, List<ITestResult>> tests,
                               int expected,
                               String message) {
    if(tests.size() > 0) {
      Set keys= tests.keySet();
      Object firstKey= keys.iterator().next();
      List<ITestResult> passedResult= tests.get(firstKey);
      int n= passedResult.size();
      assert n == expected : "Expected " + expected + " " + message + " but found " + n;
    }
    else {
      assert expected == 0 : "Expected " + expected + " " + message + " but found "
        + tests.size();
    }
  }

  protected void dumpResults(String name, Map<String, List<ITestResult>> tests) {
    ppp("============= " + name);
    for(Map.Entry<String, List<ITestResult>> entry : tests.entrySet()) {
      ppp("TEST:" + entry.getKey());
      List<ITestResult> l= entry.getValue();
      for(ITestResult tr : l) {
        ppp("   " + tr);
      }
    }
  }

  protected static void verifyInstanceNames(String title, Map<String, List<ITestResult>> actual,
      String[] expected)
  {
    List<String> actualNames = Lists.newArrayList();
    for (Map.Entry<String, List<ITestResult>> es : actual.entrySet()) {
      for (ITestResult tr : es.getValue()) {
        Object instance = tr.getInstance();
        actualNames.add(es.getKey() + "#" + (instance != null ? instance.toString() : ""));
      }
    }
    Assert.assertEqualsNoOrder(actualNames.toArray(), expected);
  }

  protected void verifyPassedTests(String... expectedPassed) {
    verifyTests("Passed", expectedPassed, getPassedTests());
  }

  protected void verifyFailedTests(String... expectedFailed) {
    verifyTests("Failed", expectedFailed, getFailedTests());
  }

  /**
     *
     * @param fileName The filename to parse
     * @param regexp The regular expression
     * @param resultLines An out parameter that will contain all the lines
     * that matched the regexp
     * @return A List<Integer> containing the lines of all the matches
     *
     * Note that the size() of the returned valuewill always be equal to
     * result.size() at the end of this function.
     */
    public static List<Integer> grep(File fileName, String regexp, List<String> resultLines) {
      List<Integer> resultLineNumbers = new ArrayList<>();
      BufferedReader fr = null;
      try {
        fr = new BufferedReader(new FileReader(fileName));
        String line = fr.readLine();
        int currentLine = 0;
        Pattern p = Pattern.compile(".*" + regexp + ".*");

        while(null != line) {
  //        ppp("COMPARING " + p + " TO @@@" + line + "@@@");
          if(p.matcher(line).matches()) {
            resultLines.add(line);
            resultLineNumbers.add(currentLine);
          }

          line = fr.readLine();
          currentLine++;
        }
      } catch(IOException e) {
        e.printStackTrace();
      }
      finally {
        if(null != fr) {
          try {
            fr.close();
          }
          catch(IOException ex) {
            ex.printStackTrace();
          }
        }
      }

      return resultLineNumbers;

    }

  private static class InternalTestRunnerFactory implements ITestRunnerFactory {
    private final BaseTest m_baseTest;

    public InternalTestRunnerFactory(final BaseTest baseTest) {
      m_baseTest= baseTest;
    }

    /**
     * @see org.testng.ITestRunnerFactory#newTestRunner(org.testng.ISuite, org.testng.xml.XmlTest)
     */
    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
      TestRunner testRunner= new TestRunner(m_baseTest.getConfiguration(), suite, test, false,
          listeners, classListeners);

      testRunner.addTestListener(new TestHTMLReporter());
      testRunner.addTestListener(new JUnitXMLReporter());
      testRunner.addListener(new TestListener(m_baseTest));
      if (listeners != null) {
        for (IInvokedMethodListener l : listeners) {
          testRunner.addListener(l);
        }
      }

      return testRunner;
    }
  }

  /**
   *  Deletes all files and subdirectories under dir.

   *  @return true if all deletions were successful.
   *  If a deletion fails, the method stops attempting to delete and returns false.
   */
  public static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (String element : children) {
        boolean success = deleteDir(new File(dir, element));
        if (!success) {
          return false;
        }
      }
    }

    // The directory is now empty so delete it
    return dir.delete();
  }

  protected void runTest(String cls, String[] passed, String[] failed, String[] skipped) {
    addClass(cls);
    run();
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

} // BaseTest

////////////////////////////

class TestListener extends TestListenerAdapter {
  private static BaseTest m_test= null;

  public TestListener(BaseTest t1) {
    m_test= t1;
  }

  @Override
  public void onTestSuccess(ITestResult tr) {
    m_test.addPassedTest(tr);
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    m_test.addFailedTest(tr);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    m_test.addFailedButWithinSuccessPercentageTest(result);
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    m_test.addSkippedTest(tr);
  }

  @Override
  public void onConfigurationSuccess(ITestResult tr) {
    m_test.addPassedConfig(tr);
  }

  @Override
  public void onConfigurationFailure(ITestResult tr) {
    m_test.addFailedConfig(tr);
  }

  @Override
  public void onConfigurationSkip(ITestResult tr) {
    m_test.addSkippedConfig(tr);
  }

} // TestListener
