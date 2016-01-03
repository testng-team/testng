package org.testng.internal;

import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClonedMethod implements ITestNGMethod {
  private static final long serialVersionUID = 1L;

  private ITestNGMethod m_method;
  transient private Method m_javaMethod;
  private String m_id;
  private int m_currentInvocationCount;
  private long m_date;

  private List<Integer> m_invocationNumbers = Lists.newArrayList();
  private List<Integer> m_failedInvocationNumbers = Lists.newArrayList();

  public ClonedMethod(ITestNGMethod method, Method javaMethod) {
    m_method = method;
    m_javaMethod = javaMethod;
  }

  @Override
  public void addMethodDependedUpon(String methodName) {
    // nop
  }

  @Override
  public boolean canRunFromClass(IClass testClass) {
    return m_method.canRunFromClass(testClass);
  }

  @Override
  public String[] getAfterGroups() {
    return m_method.getAfterGroups();
  }

  @Override
  public String[] getBeforeGroups() {
    return m_method.getBeforeGroups();
  }

  @Override
  public int getCurrentInvocationCount() {
    return m_currentInvocationCount;
  }

  @Override
  public long getDate() {
    return m_method.getDate();
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public void setDescription(String description) {
    m_method.setDescription(description);
  }

  @Override
  public boolean getEnabled() {
    return true;
  }

  @Override
  public String[] getGroups() {
    return m_method.getGroups();
  }

  @Override
  public String[] getGroupsDependedUpon() {
    return new String[0];
  }

  @Override
  public String getId() {
    return m_id;
  }

  @Override
  public long[] getInstanceHashCodes() {
    return m_method.getInstanceHashCodes();
  }

  @Override
  public Object[] getInstances() {
    return m_method.getInstances();
  }

  @Override
  public Object getInstance() {
    return m_method.getInstance();
  }

  @Override
  public int getInvocationCount() {
    return 1;
  }

  @Override
  public int getTotalInvocationCount() {
    return 1;
  }

  @Override
  public long getInvocationTimeOut() {
    return m_method.getInvocationTimeOut();
  }

  @Override
  public Method getMethod() {
    return m_javaMethod;
  }

  @Override
  public String getMethodName() {
    return m_javaMethod.getName();
  }

  @Override
  public String[] getMethodsDependedUpon() {
    return new String[0];
  }

  @Override
  public String getMissingGroup() {
    return null;
  }

  @Override
  public int getParameterInvocationCount() {
    return 1;
  }

  @Override
  public Class getRealClass() {
    return m_javaMethod.getClass();
  }

  @Override
  public IRetryAnalyzer getRetryAnalyzer() {
    return m_method.getRetryAnalyzer();
  }

  @Override
  public int getSuccessPercentage() {
    return 100;
  }

  @Override
  public ITestClass getTestClass() {
    return m_method.getTestClass();
  }

  @Override
  public int getThreadPoolSize() {
    return m_method.getThreadPoolSize();
  }

  @Override
  public long getTimeOut() {
    return m_method.getTimeOut();
  }

  @Override
  public void setTimeOut(long timeOut) {
    m_method.setTimeOut(timeOut);
  }

  @Override
  public boolean ignoreMissingDependencies() {
    return false;
  }

  @Override
  public void incrementCurrentInvocationCount() {
    m_currentInvocationCount++;
  }

  @Override
  public boolean isAfterClassConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterGroupsConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterMethodConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterSuiteConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterTestConfiguration() {
    return false;
  }

  @Override
  public boolean isAlwaysRun() {
    return false;
  }

  @Override
  public boolean isBeforeClassConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeGroupsConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeMethodConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeSuiteConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeTestConfiguration() {
    return false;
  }

  @Override
  public boolean isTest() {
    return true;
  }

  @Override
  public void setDate(long date) {
    m_date = date;
  }

  @Override
  public void setId(String id) {
    m_id = id;
  }

  @Override
  public void setIgnoreMissingDependencies(boolean ignore) {
  }

  @Override
  public void setInvocationCount(int count) {
  }

  @Override
  public void setMissingGroup(String group) {
  }

  @Override
  public void setParameterInvocationCount(int n) {
  }

  @Override
  public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer) {
  }

  @Override
  public void setSkipFailedInvocations(boolean skip) {
  }

  @Override
  public void setTestClass(ITestClass cls) {
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {
  }

  @Override
  public boolean skipFailedInvocations() {
    return false;
  }

  @Override
  public int compareTo(Object o) {
    int result = -2;
    Class<?> thisClass = getRealClass();
    Class<?> otherClass = ((ITestNGMethod) o).getRealClass();
    if (thisClass.isAssignableFrom(otherClass)) {
      result = -1;
    } else if (otherClass.isAssignableFrom(thisClass)) {
      result = 1;
    } else if (equals(o)) {
      result = 0;
    }

    return result;
  }

  @Override
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
      if (i++ > 0) {
        result.append(", ");
      }
      result.append(p.getName());
    }
    result.append(")");

    return result.toString();
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
    return m_method.getPriority();
  }

  @Override
  public void setPriority(int priority) {
    // ignored
  }

  @Override
  public XmlTest getXmlTest() {
    return m_method.getXmlTest();
  }

  @Override
  public ConstructorOrMethod getConstructorOrMethod() {
    return null;
  }

  @Override
  public Map<String, String> findMethodParameters(XmlTest test) {
    return Collections.emptyMap();
  }
}
