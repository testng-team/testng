package org.testng.junit;

import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.ConstructorOrMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Help methods for JUnit
 *
 * @author cbeust
 * @date Jan 14, 2006
 */
public class JUnitUtils {
  private static final String[] EMTPY_STRINGARRAY= new String[0];
  private static final ITestNGMethod[] EMPTY_METHODARRAY= new ITestNGMethod[0];

  /**
   * An <code>ITestNMethod</code> implementation for test methods in JUnit.
   *
   * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
   */
  public static class JUnitTestMethod implements ITestNGMethod {
    private static final long serialVersionUID = -415785273985919220L;
    private final ITestClass m_testClass;
    private final Class m_methodClass;
    private final Object[] m_instances;
    private final long[] m_instanceHashes;
    private transient Method m_method;
    private String m_methodName= "N/A";
    private String m_signature;
    private int m_currentInvocationCount = 0;
    private int m_parameterInvocationCount = 0;
    private List<Integer> m_invocationNumbers;

    private long m_date;
    private String m_id;

    transient private IRetryAnalyzer retryAnalyzer = null;
    private List<Integer> m_failedInvocationNumbers;

    public JUnitTestMethod(Test test, JUnitTestClass testClass) {
      m_testClass= testClass;
      m_instances= new Object[] {test};
      m_instanceHashes= new long[] {test.hashCode()};
      m_methodClass= test.getClass();

      init(test);
      testClass.getTestMethodList().add(this);
    }

    private void init(Test test) {
      if(TestCase.class.isAssignableFrom(test.getClass())) {
        TestCase tc= (TestCase) test;

        m_methodName= tc.getName();
        m_signature= m_methodClass.getName() + "." + m_methodName + "()";
        try {
          m_method= test.getClass().getMethod(tc.getName(), new Class[0]);
        }
        catch(Exception ex) {
          throw new TestNGException("Cannot find JUnit method "
              + tc.getClass() + "." + tc.getName(), ex);
        }
      }
    }

    /**
     * @see org.testng.ITestNGMethod#getDate()
     */
    @Override
    public long getDate() {
      return m_date;
    }

    /**
     * @see org.testng.ITestNGMethod#getDescription()
     */
    @Override
    public String getDescription() {
      return "";
    }

    /**
     * @see org.testng.ITestNGMethod#getId()
     */
    @Override
    public String getId() {
      return m_id;
    }

    @Override
    public boolean getEnabled() {
      return true;
    }

    /**
     * @see org.testng.ITestNGMethod#getInstanceHashCodes()
     */
    @Override
    public long[] getInstanceHashCodes() {
      return m_instanceHashes;
    }

    /**
     * @see org.testng.ITestNGMethod#getInstances()
     */
    @Override
    public Object[] getInstances() {
      return m_instances;
    }

    @Override
    public Object getInstance() {
      return m_instances[0];
    }

    /**
     * @see org.testng.ITestNGMethod#getMethod()
     */
    @Override
    public Method getMethod() {
      return m_method;
    }

    /**
     * @see org.testng.ITestNGMethod#getMethodName()
     */
    @Override
    public String getMethodName() {
      return m_methodName;
    }

    /**
     * @see org.testng.ITestNGMethod#getRealClass()
     */
    @Override
    public Class getRealClass() {
      return m_methodClass;
    }

    /**
     * @see org.testng.ITestNGMethod#setDate(long)
     */
    @Override
    public void setDate(long date) {
      m_date= date;
    }

    /**
     * @see org.testng.ITestNGMethod#setId(long)
     */
    @Override
    public void setId(String id) {
      m_id= id;
    }

    @Override
    public int compareTo(Object o) {
      int result = -2;
      Class thisClass = getRealClass();
      Class otherClass = ((ITestNGMethod) o).getRealClass();
      if (thisClass.isAssignableFrom(otherClass)) {
        result = -1;
      } else if (otherClass.isAssignableFrom(thisClass)) {
        result = 1;
      } else if (equals(o)) {
        result = 0;
      }

      return result;
    }

    // default values
    /**
     * @see org.testng.ITestNGMethod#isTest()
     */
    @Override
    public boolean isTest() {
      return true;
    }

    /**
     * @see org.testng.ITestNGMethod#canRunFromClass(org.testng.IClass)
     */
    @Override
    public boolean canRunFromClass(IClass testClass) {
      throw new IllegalStateException("canRunFromClass is not supported for JUnit");
    }


    /**
     * @see org.testng.ITestNGMethod#setTestClass(org.testng.ITestClass)
     */
    @Override
    public void setTestClass(ITestClass cls) {
      throw new IllegalStateException("setTestClass is not supported for JUnit");
    }

    /**
     * @see org.testng.ITestNGMethod#getTestClass()
     */
    @Override
    public ITestClass getTestClass() {
      return m_testClass;
    }


    /**
     * @see org.testng.ITestNGMethod#addMethodDependedUpon(java.lang.String)
     */
    @Override
    public void addMethodDependedUpon(String methodName) {
      throw new IllegalStateException("addMethodDependedUpon is not supported for JUnit");
    }

    /**
     * @see org.testng.ITestNGMethod#setMissingGroup(java.lang.String)
     */
    @Override
    public void setMissingGroup(String group) {
      throw new IllegalStateException("setMissingGroup is not supported for JUnit");
    }


    /**
     * @see org.testng.ITestNGMethod#getAfterGroups()
     */
    @Override
    public String[] getAfterGroups() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getBeforeGroups()
     */
    @Override
    public String[] getBeforeGroups() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getGroups()
     */
    @Override
    public String[] getGroups() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getGroupsDependedUpon()
     */
    @Override
    public String[] getGroupsDependedUpon() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getInvocationCount()
     */
    @Override
    public int getInvocationCount() {
      return 1;
    }

    /**
     * @see org.testng.ITestNGMethod#getMethodsDependedUpon()
     */
    @Override
    public String[] getMethodsDependedUpon() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getMissingGroup()
     */
    @Override
    public String getMissingGroup() {
      return null;
    }

    /**
     * @see org.testng.ITestNGMethod#getSuccessPercentage()
     */
    @Override
    public int getSuccessPercentage() {
      return 100;
    }

    /**
     * @see org.testng.ITestNGMethod#getThreadPoolSize()
     */
    @Override
    public int getThreadPoolSize() {
      return 1;
    }

    /**
     * @see org.testng.ITestNGMethod#getTimeOut()
     */
    @Override
    public long getTimeOut() {
      return 0L;
    }

    @Override
    public void setTimeOut(long timeOut) {
      // ignore
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterClassConfiguration()
     */
    @Override
    public boolean isAfterClassConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterGroupsConfiguration()
     */
    @Override
    public boolean isAfterGroupsConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterMethodConfiguration()
     */
    @Override
    public boolean isAfterMethodConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterSuiteConfiguration()
     */
    @Override
    public boolean isAfterSuiteConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterTestConfiguration()
     */
    @Override
    public boolean isAfterTestConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAlwaysRun()
     */
    @Override
    public boolean isAlwaysRun() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeClassConfiguration()
     */
    @Override
    public boolean isBeforeClassConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeGroupsConfiguration()
     */
    @Override
    public boolean isBeforeGroupsConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeMethodConfiguration()
     */
    @Override
    public boolean isBeforeMethodConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeSuiteConfiguration()
     */
    @Override
    public boolean isBeforeSuiteConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeTestConfiguration()
     */
    @Override
    public boolean isBeforeTestConfiguration() {
      return false;
    }

    @Override
    public int getCurrentInvocationCount() {
      return m_currentInvocationCount;
    }

    @Override
    public void incrementCurrentInvocationCount() {
      m_currentInvocationCount++;
    }

    @Override
    public void setParameterInvocationCount(int n) {
      m_parameterInvocationCount = n;
    }

    @Override
    public int getParameterInvocationCount() {
      return m_parameterInvocationCount;
    }

    @Override
    public String toString() {
      return m_signature;
    }

    @Override
    public ITestNGMethod clone() {
      throw new IllegalStateException("clone is not supported for JUnit");
    }

    /**
     * @see org.testng.ITestNGMethod#setInvocationCount(int)
     */
    @Override
    public void setInvocationCount(int count) {
      throw new IllegalStateException("setInvocationCount is not supported for JUnit");
    }

    /**
     * @see org.testng.ITestNGMethod#setThreadPoolSize(int)
     */
    @Override
    public void setThreadPoolSize(int threadPoolSize) {
      throw new IllegalStateException("setThreadPoolSize is not supported for JUnit");
    }

    @Override
    public IRetryAnalyzer getRetryAnalyzer() {
      return retryAnalyzer;
    }

    @Override
    public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer) {
      this.retryAnalyzer = retryAnalyzer;
    }

    @Override
    public void setSkipFailedInvocations(boolean skip) {
      // nop
    }

    @Override
    public boolean skipFailedInvocations() {
      return false;
    }

    @Override
    public void setIgnoreMissingDependencies(boolean ignore) {
      // nop
    }

    @Override
    public boolean ignoreMissingDependencies() {
      return false;
    }

    public boolean isFirstTimeOnly() {
      return false;
    }

    public boolean isLastTimeOnly() {
      return false;
    }

    @Override
    public long getInvocationTimeOut() {
      return 0;
    }

    @Override
    public List<Integer> getInvocationNumbers() {
      return m_invocationNumbers;
    }

    @Override
    public void setInvocationNumbers(List<Integer> count) {
      m_invocationNumbers = count;
    }

    @Override
    public List<Integer> getFailedInvocationNumbers() {
      return m_failedInvocationNumbers;
    }

    @Override
    public void addFailedInvocationNumber(int number) {
      m_failedInvocationNumbers.add(number);
    }

    @Override
    public int getPriority() {
      return 0;
    }

    @Override
    public void setPriority(int priority) {
      // ignored
    }

    @Override
    public XmlTest getXmlTest() {
      return null;
    }

    @Override
    public ConstructorOrMethod getConstructorOrMethod() {
      return null;
    }
  }

  /**
   * An <code>ITestClass</code> implementation for test methods in JUnit.
   *
   * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
   */
  public static class JUnitTestClass implements ITestClass {
    /**
     *
     */
    private static final long serialVersionUID = 405598615794850925L;
    private List<ITestNGMethod> m_testMethods= Lists.newArrayList();
    private Class m_realClass;
    private Object[] m_instances;
    private long[] m_instanceHashes;

    public JUnitTestClass(Test test) {
      m_realClass= test.getClass();
      m_instances= new Object[] {test};
      m_instanceHashes= new long[] {test.hashCode()};
    }

    List<ITestNGMethod> getTestMethodList() {
      return m_testMethods;
    }

    /**
     * @see org.testng.ITestClass#getInstanceCount()
     */
    @Override
    public int getInstanceCount() {
      return 1;
    }

    /**
     * @see org.testng.ITestClass#getInstanceHashCodes()
     */
    @Override
    public long[] getInstanceHashCodes() {
      return m_instanceHashes;
    }

    @Override
    public Object[] getInstances(boolean reuse) {
      return m_instances;
    }

    /**
     * @see org.testng.ITestClass#getTestMethods()
     */
    @Override
    public ITestNGMethod[] getTestMethods() {
      return m_testMethods.toArray(new ITestNGMethod[m_testMethods.size()]);
    }

    /**
     * @see org.testng.ITestClass#getAfterClassMethods()
     */
    @Override
    public ITestNGMethod[] getAfterClassMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterGroupsMethods()
     */
    @Override
    public ITestNGMethod[] getAfterGroupsMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterSuiteMethods()
     */
    @Override
    public ITestNGMethod[] getAfterSuiteMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterTestConfigurationMethods()
     */
    @Override
    public ITestNGMethod[] getAfterTestConfigurationMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterTestMethods()
     */
    @Override
    public ITestNGMethod[] getAfterTestMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeClassMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeClassMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeGroupsMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeGroupsMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeSuiteMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeSuiteMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeTestConfigurationMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeTestConfigurationMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeTestMethods()
     */
    @Override
    public ITestNGMethod[] getBeforeTestMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.IClass#addInstance(java.lang.Object)
     */
    @Override
    public void addInstance(Object instance) {
      throw new IllegalStateException("addInstance is not supported for JUnit");
    }

    /**
     * @see org.testng.IClass#getName()
     */
    @Override
    public String getName() {
      return m_realClass.getName();
    }

    /**
     * @see org.testng.IClass#getRealClass()
     */
    @Override
    public Class getRealClass() {
      return m_realClass;
    }

    @Override
    public String getTestName() {
      return null;
    }

    @Override
    public XmlTest getXmlTest() {
      return null;
    }

    @Override
    public XmlClass getXmlClass() {
      return null;
    }
  }

}
