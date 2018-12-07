package test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.testng.collections.Maps;
import org.testng.internal.Configuration;
import org.testng.internal.IConfiguration;
import org.testng.internal.Systematiser;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlScript;
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
  private Map<Long, Map<String, List<ITestResult>>> m_passedTests= new HashMap<>();
  private Map<Long, Map<String, List<ITestResult>>> m_failedTests= new HashMap<>();
  private Map<Long, Map<String, List<ITestResult>>> m_skippedTests= new HashMap<>();
  private Map<Long, Map<String, List<ITestResult>>> m_passedConfigs= new HashMap<>();
  private Map<Long, Map<String, List<ITestResult>>> m_failedConfigs= new HashMap<>();
  private Map<Long, Map<String, List<ITestResult>>> m_skippedConfigs= new HashMap<>();
  private Map<Long, Map<String, List<ITestResult>>> m_failedButWithinSuccessPercentageTests= new HashMap<>();

  protected Map<String, List<ITestResult>> getTests(Map<Long, Map<String, List<ITestResult>>> map) {
    return map.computeIfAbsent(getId(), k -> new HashMap<>());
  }

  protected XmlTest getTest() {
    return m_tests.get(getId());
  }

  protected void setTests(Map<Long, Map<String, List<ITestResult>>> map, Map<String, List<ITestResult>> m) {
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

  public void setSkippedTests(Map<String, List<ITestResult>> m) {
    setTests(m_skippedTests, m);
  }

  public void setPassedTests(Map<String, List<ITestResult>> m) {
    setTests(m_passedTests, m);
  }

  public void setFailedTests(Map<String, List<ITestResult>> m) {
    setTests(m_failedTests, m);
  }

  public void setFailedButWithinSuccessPercentageTests(Map<String, List<ITestResult>> m) {
    setTests(m_failedButWithinSuccessPercentageTests, m);
  }

  public void setSkippedConfigs(Map<String, List<ITestResult>> m) {
    setTests(m_skippedConfigs, m);
  }

  public void setPassedConfigs(Map<String, List<ITestResult>> m) {
    setTests(m_passedConfigs, m);
  }

  public void setFailedConfigs(Map<String, List<ITestResult>> m) {
    setTests(m_failedConfigs, m);
  }


  protected void run() {
    assert null != getTest() : "Test wasn't set, maybe @Configuration methodSetUp() was never called";
    setPassedTests(Maps.newHashMap());
    setFailedTests(Maps.newHashMap());
    setSkippedTests(Maps.newHashMap());
    setPassedConfigs(Maps.newHashMap());
    setFailedConfigs(Maps.newHashMap());
    setSkippedConfigs(Maps.newHashMap());
    setFailedButWithinSuccessPercentageTests(Maps.newHashMap());

    m_suite.setVerbose(m_verbose != null ? m_verbose : 0);
    SuiteRunner suite = new SuiteRunner(m_configuration,
        m_suite, m_outputDirectory, m_testRunnerFactory, Systematiser.getComparator());

    suite.run();
  }

  protected void addMethodSelector(String className, int priority) {
    XmlMethodSelector methodSelector= new XmlMethodSelector();
    methodSelector.setName(className);
    methodSelector.setPriority(priority);
    getTest().getMethodSelectors().add(methodSelector);
  }

  protected void addClasses(Class<?>... classes) {
    for (Class<?> clazz : classes) {
      addClass(clazz);
    }
  }

  protected XmlClass addClass(Class<?> cls) {
    return addClass(cls.getName());
  }

  protected XmlClass addClass(String className) {
    XmlClass result= new XmlClass(className);
    getTest().getXmlClasses().add(result);

    return result;
  }

  protected void setScript(String language, String expression) {
    XmlScript script = new XmlScript();
    script.setExpression(expression);
    script.setLanguage(language);
    getTest().setScript(script);
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

    return addClass(className);
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

  @BeforeMethod(groups= { "init", "initTest" })
  public void methodSetUp() {
    m_suite= new XmlSuite();
    m_suite.setName("Internal_suite");
    XmlTest xmlTest= new XmlTest(m_suite);
    xmlTest.setName("Internal_test_failures_are_expected");
    m_tests.put(getId(), xmlTest);
  }

  private void addTest(Map<String, List<ITestResult>> tests, ITestResult t) {
    List<ITestResult> l = tests
        .computeIfAbsent(t.getMethod().getMethodName(), k -> new ArrayList<>());
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

  protected Long getId() {
    return 42L;
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
      Set<String> keys = tests.keySet();
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

  protected static void verifyInstanceNames(Map<String, List<ITestResult>> actual,
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

  protected void verifySkippedTests(String... expectedSkipped) {
    verifyTests("Skipped", expectedSkipped, getSkippedTests());
  }

  private static class InternalTestRunnerFactory implements ITestRunnerFactory {
    private final BaseTest m_baseTest;

    public InternalTestRunnerFactory(final BaseTest baseTest) {
      m_baseTest= baseTest;
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
      TestRunner testRunner= new TestRunner(m_baseTest.getConfiguration(), suite, test, false,
          listeners, classListeners, Systematiser.getComparator());

      testRunner.addListener(new TestHTMLReporter());
      testRunner.addListener(new JUnitXMLReporter());
      testRunner.addListener(new TestListener(m_baseTest));
      if (listeners != null) {
        for (IInvokedMethodListener l : listeners) {
          testRunner.addListener(l);
        }
      }

      return testRunner;
    }
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
