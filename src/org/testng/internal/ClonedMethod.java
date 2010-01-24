package org.testng.internal;

import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;

import java.lang.reflect.Method;
import java.util.List;

public class ClonedMethod implements ITestNGMethod {
  private static final long serialVersionUID = 1L;

  private ITestNGMethod m_method;
  private Method m_javaMethod;
  private String m_id;
  private int m_currentInvocationCount;
  private long m_date;

  private List<Integer> m_invocationNumbers = Lists.newArrayList();
  private List<Integer> m_failedInvocationNumbers = Lists.newArrayList();

  public ClonedMethod(ITestNGMethod method, Method javaMethod) {
    m_method = method;
    m_javaMethod = javaMethod;
  }

  public void addMethodDependedUpon(String methodName) {
    // nop
  }

  public boolean canRunFromClass(IClass testClass) {
    return m_method.canRunFromClass(testClass);
  }

  public String[] getAfterGroups() {
    return m_method.getAfterGroups();
  }

  public String[] getBeforeGroups() {
    return m_method.getBeforeGroups();
  }

  public int getCurrentInvocationCount() {
    return m_currentInvocationCount;
  }

  public long getDate() {
    return m_method.getDate();
  }

  public String getDescription() {
    return "";
  }

  public String[] getGroups() {
    return m_method.getGroups();
  }

  public String[] getGroupsDependedUpon() {
    return new String[0];
  }

  public String getId() {
    return m_id;
  }

  public long[] getInstanceHashCodes() {
    return m_method.getInstanceHashCodes();
  }

  public Object[] getInstances() {
    return m_method.getInstances();
  }

  public int getInvocationCount() {
    return 1;
  }

  public long getInvocationTimeOut() {
    return m_method.getInvocationTimeOut();
  }

  public Method getMethod() {
    return m_javaMethod;
  }

  public String getMethodName() {
    return m_javaMethod.getName();
  }

  public String[] getMethodsDependedUpon() {
    return new String[0];
  }

  public String getMissingGroup() {
    return null;
  }

  public int getParameterInvocationCount() {
    return 1;
  }

  public Class getRealClass() {
    return m_javaMethod.getClass();
  }

  public IRetryAnalyzer getRetryAnalyzer() {
    return m_method.getRetryAnalyzer();
  }

  public int getSuccessPercentage() {
    return 100;
  }

  public ITestClass getTestClass() {
    return m_method.getTestClass();
  }

  public int getThreadPoolSize() {
    return m_method.getThreadPoolSize();
  }

  public long getTimeOut() {
    return m_method.getTimeOut();
  }

  public boolean ignoreMissingDependencies() {
    return false;
  }

  public void incrementCurrentInvocationCount() {
    m_currentInvocationCount++;
  }

  public boolean isAfterClassConfiguration() {
    return false;
  }

  public boolean isAfterGroupsConfiguration() {
    return false;
  }

  public boolean isAfterMethodConfiguration() {
    return false;
  }

  public boolean isAfterSuiteConfiguration() {
    return false;
  }

  public boolean isAfterTestConfiguration() {
    return false;
  }

  public boolean isAlwaysRun() {
    return false;
  }

  public boolean isBeforeClassConfiguration() {
    return false;
  }

  public boolean isBeforeGroupsConfiguration() {
    return false;
  }

  public boolean isBeforeMethodConfiguration() {
    return false;
  }

  public boolean isBeforeSuiteConfiguration() {
    return false;
  }

  public boolean isBeforeTestConfiguration() {
    return false;
  }

  public boolean isTest() {
    return true;
  }

  public void setDate(long date) {
    m_date = date;
  }

  public void setId(String id) {
    m_id = id;
  }

  public void setIgnoreMissingDependencies(boolean ignore) {
  }

  public void setInvocationCount(int count) {
  }

  public void setMissingGroup(String group) {
  }

  public void setParameterInvocationCount(int n) {
  }

  public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer) {
  }

  public void setSkipFailedInvocations(boolean skip) {
  }

  public void setTestClass(ITestClass cls) {
  }

  public void setThreadPoolSize(int threadPoolSize) {
  }

  public boolean skipFailedInvocations() {
    return false;
  }

  public int compareTo(Object o) {
    int result = -2;
    Class<?> thisClass = getRealClass();
    Class<?> otherClass = ((ITestNGMethod) o).getRealClass();
    if (thisClass.isAssignableFrom(otherClass)) 
      result = -1;
    else if (otherClass.isAssignableFrom(thisClass)) 
      result = 1;
    else if (equals(o)) 
      result = 0;
    
    return result;
  }

  public ClonedMethod clone() {
    return new ClonedMethod(m_method, m_javaMethod);
  }

  @Override
  public String toString() {
    Method m = getMethod();
    String cls = m.getDeclaringClass().getName();
    StringBuffer result = new StringBuffer(cls + "." + m.getName() + "(");
    int i = 0;
    for (Class<?> p : m.getParameterTypes()) {
      if (i++ > 0) result.append(", ");
      result.append(p.getName());
    }
    result.append(")");

    return result.toString();
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
