package org.testng.junit;

import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.collections.Lists;

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
    private final ITestClass m_testClass;
    private final Class m_methodClass;
    private final Object[] m_instances;
    private final long[] m_instanceHashes;
    private Method m_method;
    private String m_methodName= "N/A";
    private String m_signature;
    private int m_currentInvocationCount = 0;
    private int m_parameterInvocationCount = 0;
    private List<Integer> m_invocationNumbers;

    private long m_date;
    private String m_id;
    
    private IRetryAnalyzer retryAnalyzer = null;
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
          throw new TestNGException("cannot retrieve JUnit method", ex);
        }
      }
    }
    
    /**
     * @see org.testng.ITestNGMethod#getDate()
     */
    public long getDate() {
      return m_date;
    }

    /**
     * @see org.testng.ITestNGMethod#getDescription()
     */
    public String getDescription() {
      return "";
    }

    /**
     * @see org.testng.ITestNGMethod#getId()
     */
    public String getId() {
      return m_id;
    }

    /**
     * @see org.testng.ITestNGMethod#getInstanceHashCodes()
     */
    public long[] getInstanceHashCodes() {
      return m_instanceHashes;
    }

    /**
     * @see org.testng.ITestNGMethod#getInstances()
     */
    public Object[] getInstances() {
      return m_instances;
    }

    /**
     * @see org.testng.ITestNGMethod#getMethod()
     */
    public Method getMethod() {
      return m_method;
    }

    /**
     * @see org.testng.ITestNGMethod#getMethodName()
     */
    public String getMethodName() {
      return m_methodName;
    }

    /**
     * @see org.testng.ITestNGMethod#getRealClass()
     */
    public Class getRealClass() {
      return m_methodClass;
    }

    /**
     * @see org.testng.ITestNGMethod#setDate(long)
     */
    public void setDate(long date) {
      m_date= date;
    }

    /**
     * @see org.testng.ITestNGMethod#setId(long)
     */
    public void setId(String id) {
      m_id= id;
    }

    public int compareTo(Object o) {
      int result = -2;
      Class thisClass = getRealClass();
      Class otherClass = ((ITestNGMethod) o).getRealClass();
      if (thisClass.isAssignableFrom(otherClass)) 
        result = -1;
      else if (otherClass.isAssignableFrom(thisClass)) 
        result = 1;
      else if (equals(o)) 
        result = 0;
      
      return result;
    }

    // default values
    /**
     * @see org.testng.ITestNGMethod#isTest()
     */
    public boolean isTest() {
      return true;
    }

    /**
     * @see org.testng.ITestNGMethod#canRunFromClass(org.testng.IClass)
     */
    public boolean canRunFromClass(IClass testClass) {
      throw new IllegalStateException("canRunFromClass is not supported for JUnit");
    }


    /**
     * @see org.testng.ITestNGMethod#setTestClass(org.testng.ITestClass)
     */
    public void setTestClass(ITestClass cls) {
      throw new IllegalStateException("setTestClass is not supported for JUnit");
    }

    /**
     * @see org.testng.ITestNGMethod#getTestClass()
     */
    public ITestClass getTestClass() {
      return m_testClass;
    }


    /**
     * @see org.testng.ITestNGMethod#addMethodDependedUpon(java.lang.String)
     */
    public void addMethodDependedUpon(String methodName) {
      throw new IllegalStateException("addMethodDependedUpon is not supported for JUnit");
    }

    /**
     * @see org.testng.ITestNGMethod#setMissingGroup(java.lang.String)
     */
    public void setMissingGroup(String group) {
      throw new IllegalStateException("setMissingGroup is not supported for JUnit");
    }


    /**
     * @see org.testng.ITestNGMethod#getAfterGroups()
     */
    public String[] getAfterGroups() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getBeforeGroups()
     */
    public String[] getBeforeGroups() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getGroups()
     */
    public String[] getGroups() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getGroupsDependedUpon()
     */
    public String[] getGroupsDependedUpon() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getInvocationCount()
     */
    public int getInvocationCount() {
      return 1;
    }

    /**
     * @see org.testng.ITestNGMethod#getMethodsDependedUpon()
     */
    public String[] getMethodsDependedUpon() {
      return EMTPY_STRINGARRAY;
    }

    /**
     * @see org.testng.ITestNGMethod#getMissingGroup()
     */
    public String getMissingGroup() {
      return null;
    }

    /**
     * @see org.testng.ITestNGMethod#getSuccessPercentage()
     */
    public int getSuccessPercentage() {
      return 100;
    }

    /**
     * @see org.testng.ITestNGMethod#getThreadPoolSize()
     */
    public int getThreadPoolSize() {
      return 1;
    }

    /**
     * @see org.testng.ITestNGMethod#getTimeOut()
     */
    public long getTimeOut() {
      return 0L;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterClassConfiguration()
     */
    public boolean isAfterClassConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterGroupsConfiguration()
     */
    public boolean isAfterGroupsConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterMethodConfiguration()
     */
    public boolean isAfterMethodConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterSuiteConfiguration()
     */
    public boolean isAfterSuiteConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAfterTestConfiguration()
     */
    public boolean isAfterTestConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isAlwaysRun()
     */
    public boolean isAlwaysRun() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeClassConfiguration()
     */
    public boolean isBeforeClassConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeGroupsConfiguration()
     */
    public boolean isBeforeGroupsConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeMethodConfiguration()
     */
    public boolean isBeforeMethodConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeSuiteConfiguration()
     */
    public boolean isBeforeSuiteConfiguration() {
      return false;
    }

    /**
     * @see org.testng.ITestNGMethod#isBeforeTestConfiguration()
     */
    public boolean isBeforeTestConfiguration() {
      return false;
    }
    
    public int getCurrentInvocationCount() {
      return m_currentInvocationCount;
    }
    
    public void incrementCurrentInvocationCount() {
      m_currentInvocationCount++;
    }
    
    public void setParameterInvocationCount(int n) {
      m_parameterInvocationCount = n;
    }

    public int getParameterInvocationCount() {
      return m_parameterInvocationCount;
    }

    public String toString() {
      return m_signature;
    }
    
    public ITestNGMethod clone() {
      throw new IllegalStateException("clone is not supported for JUnit"); 
    }

    /**
     * @see org.testng.ITestNGMethod#setInvocationCount(int)
     */
    public void setInvocationCount(int count) {
      throw new IllegalStateException("setInvocationCount is not supported for JUnit");
    }

    /**
     * @see org.testng.ITestNGMethod#setThreadPoolSize(int)
     */
    public void setThreadPoolSize(int threadPoolSize) {
      throw new IllegalStateException("setThreadPoolSize is not supported for JUnit");
    }

    public IRetryAnalyzer getRetryAnalyzer() {
      return retryAnalyzer;
    }

    public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer) {
      this.retryAnalyzer = retryAnalyzer;
    }

    public void setSkipFailedInvocations(boolean skip) {
      // nop
    }

    public boolean skipFailedInvocations() {
      return false;
    }

    public void setIgnoreMissingDependencies(boolean ignore) {
      // nop
    }

    public boolean ignoreMissingDependencies() {
      return false;
    }
    
    public boolean isFirstTimeOnly() {
      return false;
    }

    public boolean isLastTimeOnly() {
      return false;
    }

    public long getInvocationTimeOut() {
      return 0;
    }

    public List<Integer> getInvocationNumbers() {
      return m_invocationNumbers;
    }

    public void setInvocationNumbers(List<Integer> count) {
      m_invocationNumbers = count;
    }

    public List<Integer> getFailedInvocationNumbers() {
      return m_failedInvocationNumbers;
    }

    public void addFailedInvocationNumber(int number) {
      m_failedInvocationNumbers.add(number);
    }
  }
  
  /**
   * An <code>ITestClass</code> implementation for test methods in JUnit.
   * 
   * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
   */
  public static class JUnitTestClass implements ITestClass {
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
    public int getInstanceCount() {
      return 1;
    }

    /**
     * @see org.testng.ITestClass#getInstanceHashCodes()
     */
    public long[] getInstanceHashCodes() {
      return m_instanceHashes;
    }

    public Object[] getInstances(boolean reuse) {
      return m_instances;
    }

    /**
     * @see org.testng.ITestClass#getTestMethods()
     */
    public ITestNGMethod[] getTestMethods() {
      return m_testMethods.toArray(new ITestNGMethod[m_testMethods.size()]);
    }

    /**
     * @see org.testng.ITestClass#getAfterClassMethods()
     */
    public ITestNGMethod[] getAfterClassMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterGroupsMethods()
     */
    public ITestNGMethod[] getAfterGroupsMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterSuiteMethods()
     */
    public ITestNGMethod[] getAfterSuiteMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterTestConfigurationMethods()
     */
    public ITestNGMethod[] getAfterTestConfigurationMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getAfterTestMethods()
     */
    public ITestNGMethod[] getAfterTestMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeClassMethods()
     */
    public ITestNGMethod[] getBeforeClassMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeGroupsMethods()
     */
    public ITestNGMethod[] getBeforeGroupsMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeSuiteMethods()
     */
    public ITestNGMethod[] getBeforeSuiteMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeTestConfigurationMethods()
     */
    public ITestNGMethod[] getBeforeTestConfigurationMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.ITestClass#getBeforeTestMethods()
     */
    public ITestNGMethod[] getBeforeTestMethods() {
      return EMPTY_METHODARRAY;
    }

    /**
     * @see org.testng.IClass#addInstance(java.lang.Object)
     */
    public void addInstance(Object instance) {
      throw new IllegalStateException("addInstance is not supported for JUnit");
    }

    /**
     * @see org.testng.IClass#getName()
     */
    public String getName() {
      return m_realClass.getName();
    }

    /**
     * @see org.testng.IClass#getRealClass()
     */
    public Class getRealClass() {
      return m_realClass;
    }

    public String getTestName() {
      return null;
    }
  }
}
