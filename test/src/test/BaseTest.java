package test;


import org.testng.IAnnotationTransformer;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.ITestRunnerFactory;
import org.testng.SuiteRunner;
import org.testng.TestListenerAdapter;
import org.testng.TestRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK14AnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  private IAnnotationTransformer m_defaultAnnotationTransformer= new DefaultAnnotationTransformer();
  private IAnnotationFinder m_jdkAnnotationFinder;
  private IAnnotationFinder m_javadocAnnotationFinder;

  public BaseTest() {
    m_testRunnerFactory= new InternalTestRunnerFactory(this);
    m_jdkAnnotationFinder= new JDK15AnnotationFinder(m_defaultAnnotationTransformer);
  }

  protected void setDebug() {
    getTest().setVerbose(9);
  }

  protected void setParallel(String parallel) {
    getTest().setParallel(parallel);
  }

  protected void setVerbose(int n) {
    getTest().setVerbose(n);
  }

  protected void setJUnit(boolean f) {
    getTest().setJUnit(f);
  }

  protected void setThreadCount(int count) {
    getTest().getSuite().setThreadCount(count);
  }

  private Map<Long, XmlTest> m_tests= new HashMap<Long, XmlTest>();
  private Map<Long, Map> m_passedTests= new HashMap<Long, Map>();
  private Map<Long, Map> m_failedTests= new HashMap<Long, Map>();
  private Map<Long, Map> m_skippedTests= new HashMap<Long, Map>();
  private Map<Long, Map> m_failedButWithinSuccessPercentageTests= new HashMap<Long, Map>();

  protected Map<String, List<ITestResult>> getTests(Map<Long, Map> map) {
    Map<String, List<ITestResult>> result= map.get(getId());
    if(null == result) {
      result= new HashMap<String, List<ITestResult>>();
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

  protected void run() {
    assert null != getTest() : "Test wasn't set, maybe @Configuration methodSetUp() was never called";
    setPassedTests(new HashMap());
    setFailedTests(new HashMap());
    setSkippedTests(new HashMap());
    setFailedButWithinSuccessPercentageTests(new HashMap());

    m_suite.setVerbose(0);
    SuiteRunner suite= new SuiteRunner(m_suite,
                                       m_outputDirectory,
                                       m_testRunnerFactory,
                                       new IAnnotationFinder[] {m_javadocAnnotationFinder, m_jdkAnnotationFinder});

    suite.run();
  }

  protected void addMethodSelector(String className, int priority) {
    XmlMethodSelector methodSelector= new XmlMethodSelector();
    methodSelector.setName(className);
    methodSelector.setPriority(priority);
    getTest().getMethodSelectors().add(methodSelector);
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
    xmlClass.getIncludedMethods().add(m);
    getTest().getXmlClasses().add(xmlClass);
  }

  public void addExcludedMethod(String className, String m) {
    XmlClass xmlClass= findClass(className);
    xmlClass.getExcludedMethods().add(m);
    getTest().getXmlClasses().add(xmlClass);
  }

  public void addIncludedGroup(String g) {
    getTest().getIncludedGroups().add(g);
  }

  public void addExcludedGroup(String g) {
    getTest().getExcludedGroups().add(g);
  }

  public void addMetaGroup(String mg, List<String> l) {
    getTest().getMetaGroups().put(mg, l);
  }

  public void addMetaGroup(String mg, String n) {
    List<String> l= new ArrayList<String>();
    l.add(n);
    addMetaGroup(mg, l);
  }

  public void setParameter(String key, String value) {
    getTest().addParameter(key, value);
  }

  private void setTest(XmlTest test) {
    XmlTest t= m_tests.get(getId());
    if(null == t) {
      m_tests.put(getId(), t);
    }
  }

//  @Configuration(beforeTestMethod = true, groups = { "init", "initTest"})
  @BeforeMethod(groups= { "init", "initTest" })
  public void methodSetUp() {
    m_javadocAnnotationFinder= new JDK14AnnotationFinder(m_defaultAnnotationTransformer);
    m_suite= new XmlSuite();
    m_suite.setName("Internal_suite");
    XmlTest xmlTest= new XmlTest(m_suite);
    xmlTest.setName("Internal_test_failures_are_expected");
    m_tests.put(getId(), xmlTest);
  }

  private Collection computeDifferences(Map m1, Map m2) {
    List result= new ArrayList();

    for(Iterator it= m1.keySet().iterator(); it.hasNext();) {
      Object key= it.next();
    }

    return result;
  }

  private void addTest(Map<String, List<ITestResult>> tests, ITestResult t) {
    List<ITestResult> l= tests.get(t.getName());
    if(null == l) {
      l= new ArrayList<ITestResult>();
      tests.put(t.getName(), l);
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

  private void ppp(String s) {
    System.out.println("[BaseTest " + getId() + "] " + s);
  }

  protected Long getId() {
    return Thread.currentThread().getId();
  }

  public XmlSuite getSuite() {
    return m_suite;
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
    for(String n : tests.keySet()) {
      ppp("TEST:" + n);
      List<ITestResult> l= tests.get(n);
      for(ITestResult tr : l) {
        ppp("   " + tr);
      }
    }
  }

  private static class InternalTestRunnerFactory implements ITestRunnerFactory {
    private final BaseTest m_baseTest;

    public InternalTestRunnerFactory(final BaseTest baseTest) {
      m_baseTest= baseTest;
    }

    /**
     * @see org.testng.ITestRunnerFactory#newTestRunner(org.testng.ISuite, org.testng.xml.XmlTest)
     */
    public TestRunner newTestRunner(ISuite suite, XmlTest test) {
      TestRunner testRunner= new TestRunner(suite, test, false);

      testRunner.addTestListener(new TestHTMLReporter());
      testRunner.addTestListener(new JUnitXMLReporter());
      testRunner.addTestListener(new TestListener(m_baseTest));

      return testRunner;
    }
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

} // TestListener
