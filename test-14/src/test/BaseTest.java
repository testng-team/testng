package test;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.ITestRunnerFactory;
import org.testng.SuiteRunner;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.TestRunner;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Base class for tests
 *
 * @author Cedric Beust, May 5, 2004
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class BaseTest {
  private static final String m_outputDirectory = "test-output-tests";

  private XmlSuite            m_suite           = null;
  private ITestRunnerFactory  m_testRunnerFactory;

  public BaseTest() {
    m_testRunnerFactory = new InternalTestRunnerFactory(this);
  }

  protected void setDebug() {
    getTest().setVerbose(9);
  }

  protected void setParallel(boolean parallel) {
    getTest().setParallel(parallel);
  }
  
  public void setThreadCount(int threadCount) {
    getTest().getSuite().setThreadCount(threadCount);
  }  

  protected void setVerbose(int n) {
    getTest().setVerbose(n);
  }

  private Map m_tests = new HashMap();
  private Map m_passedTests  = new HashMap();
  private Map m_failedTests = new HashMap();
  private Map m_skippedTests = new HashMap();
  private Map m_failedButWithinSuccessPercentageTests = new HashMap();

  protected Map getTests(Map map) {
    Map result = (Map) map.get(getId());
    if(null == result) {
      result = new HashMap();
      map.put(getId(), result);
    }
    return result;
  }

  protected XmlTest getTest() {
    return (XmlTest) m_tests.get(getId());
  }

  protected void setTests(Map map, Map m) {
    map.put(getId(), m);
  }

  public Map getFailedTests() {
    return getTests(m_failedTests);
  }

  // Map<String, List<ITestResult>>
  public Map getFailedButWithinSuccessPercentageTests() {
    return getTests(m_failedButWithinSuccessPercentageTests);
  }

  public Map getPassedTests() {
    return getTests(m_passedTests);
  }

  public Map getSkippedTests() {
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

    m_suite.setVerbose(new Integer(0));
    SuiteRunner suite = new SuiteRunner(m_suite, m_outputDirectory, m_testRunnerFactory);

    suite.run();
  }

  public void addIncludedMethod(String className, String m) {
    XmlClass xmlClass = findClass(className);
    xmlClass.getIncludedMethods().add(m);
    getTest().getXmlClasses().add(xmlClass);
  }

  public void addExcludedMethod(String className, String m) {
    XmlClass xmlClass = findClass(className);
    xmlClass.getExcludedMethods().add(m);
    getTest().getXmlClasses().add(xmlClass);
  }

  private XmlClass findClass(String className) {
    for (Iterator it = getTest().getXmlClasses().iterator(); it.hasNext(); ) {
      XmlClass cl = (XmlClass) it.next();
      if (cl.getName().equals(className)) {
        return cl;
      }
    }
    
    XmlClass result = addClass(className);
    return result;
  }

  public void addIncludedGroup(String g) {
    getTest().getIncludedGroups().add(g);
  }

  public void addExcludedGroup(String g) {
    getTest().getExcludedGroups().add(g);
  }

  public void addMetaGroup(String mg, List l) {
    getTest().getMetaGroups().put(mg, l);
  }

  public void addMetaGroup(String mg, String n) {
    List l = new ArrayList();
    l.add(n);
    addMetaGroup(mg, l);
  }

  public void setParameter(String key, String value) {
    getTest().addParameter(key, value);
  }

  protected XmlClass addClass(String className) {
    XmlClass result = new XmlClass(className);
    getTest().getXmlClasses().add(result);
    return result;
  }
  
  private void setTest(XmlTest test) {
    XmlTest t = (XmlTest) m_tests.get(getId());
    if(null == t) {
      m_tests.put(getId(), t);
    }
  }

  /**
   * @testng.before-method groups = "init, initTest, current"
   * @         testng.configuration beforeTestMethod="true" groups="init,initTest,current"
   */
  public void methodSetUp() {
    m_suite = new XmlSuite();
    m_suite.setAnnotations("javadoc");
    m_tests.put(getId(), new XmlTest(m_suite));
    getTest().setName("Internal test, failures are expected");
  }

  protected void verifyResults(Map tests, int expected, String message) {
    //  protected void verifyResults(Map<String, List<ITestResult>> tests, int expected, String message) {
    if(tests.size() > 0) {
      Set keys = tests.keySet();
      Object firstKey = keys.iterator().next();
      List passedResult = (List) tests.get(firstKey);
      int n = passedResult.size();
      assert n == expected : "Expected " + expected + " " + message + " but found " + n;
    } else {
      assert expected == 0 : "Expected " + expected + " " + message + " but found " + tests.size();
    }
  }

  protected void verifyTests(String title, String[] exp, Map found) {
    Map expected = new HashMap();
    for(int i = 0; i < exp.length; i++) {
      expected.put(exp[i], exp[i]);
    }

    assert expected.size() == found.size() : "Expected " + expected.size() + " " + title
        + " tests but found " + found.size();

    for(Iterator it = expected.values().iterator(); it.hasNext();) {
      String name = (String) it.next();
      if(null == found.get(name)) {
        dumpMap("Expected", expected);
        dumpMap("Found", found);
      }
      assert null != found.get(name) : "Expected to find method " + name + " in " + title
          + " but didn't find it.";
    }
  }

  private void dumpMap(String title, Map m) {
    System.out.println("==== " + title);
    for(Iterator it = m.keySet().iterator(); it.hasNext();) {
      Object key = it.next();
      Object value = m.get(key);
      ppp(key + "  => " + value);
    }

  }

  private Collection computeDifferences(Map m1, Map m2) {
    List result = new ArrayList();

    for(Iterator it = m1.keySet().iterator(); it.hasNext();) {
      Object key = it.next();
    }

    return result;
  }

  /** Map<String, List<ITestResult>> */
  private void addTest(Map tests, ITestResult t) {
    List l = (List) tests.get(t.getName());
    if(null == l) {
      l = new ArrayList();
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
    return new Long(Thread.currentThread().hashCode());
  }

  public XmlSuite getSuite() {
    return m_suite;
  }

  private static class InternalTestRunnerFactory implements ITestRunnerFactory {
    private final BaseTest m_baseTest;

    public InternalTestRunnerFactory(final BaseTest baseTest) {
      m_baseTest = baseTest;
    }

    /**
     * @see org.testng.ITestRunnerFactory#newTestRunner(org.testng.ISuite, org.testng.xml.XmlTest)
     */
    public TestRunner newTestRunner(ISuite suite, XmlTest test) {
      TestRunner testRunner = new TestRunner(suite, test);

      testRunner.addTestListener(new TestHTMLReporter());
      testRunner.addTestListener(new JUnitXMLReporter());
      testRunner.addTestListener(new TestListener(m_baseTest));

      return testRunner;
    }
  }

  private static class TestListener extends TestListenerAdapter {
    private BaseTest m_test = null;

    public TestListener(BaseTest t1) {
      m_test = t1;
    }

    public void onTestSuccess(ITestResult tr) {
      m_test.addPassedTest(tr);
    }

    public void onTestFailure(ITestResult tr) {
      m_test.addFailedTest(tr);
    }

    public void onTestSkipped(ITestResult tr) {
      m_test.addSkippedTest(tr);
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
      m_test.addFailedButWithinSuccessPercentageTest(result);
    }
  } // TestListener

} // BaseTest

