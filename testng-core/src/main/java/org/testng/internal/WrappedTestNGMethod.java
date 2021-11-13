package org.testng.internal;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

/**
 * Represents a proxy for an actual instance of {@link ITestNGMethod} but with the exception that it
 * generates a unique hashcode that is different from the original {@link ITestNGMethod} instance
 * that it wraps.
 */
public class WrappedTestNGMethod implements ITestNGMethod {
  private final ITestNGMethod testNGMethod;
  private final int multiplicationFactor = new Random().nextInt();

  public WrappedTestNGMethod(ITestNGMethod testNGMethod) {
    this.testNGMethod = testNGMethod;
  }

  @Override
  public Class<?> getRealClass() {
    return testNGMethod.getRealClass();
  }

  @Override
  public ITestClass getTestClass() {
    return testNGMethod.getTestClass();
  }

  @Override
  public void setTestClass(ITestClass cls) {
    testNGMethod.setTestClass(cls);
  }

  @Override
  public String getMethodName() {
    return testNGMethod.getMethodName();
  }

  @Override
  public Object getInstance() {
    return testNGMethod.getInstance();
  }

  @Override
  public long[] getInstanceHashCodes() {
    return testNGMethod.getInstanceHashCodes();
  }

  @Override
  public String[] getGroups() {
    return testNGMethod.getGroups();
  }

  @Override
  public String[] getGroupsDependedUpon() {
    return testNGMethod.getGroupsDependedUpon();
  }

  @Override
  public String getMissingGroup() {
    return testNGMethod.getMissingGroup();
  }

  @Override
  public void setMissingGroup(String group) {
    testNGMethod.setMissingGroup(group);
  }

  @Override
  public String[] getBeforeGroups() {
    return testNGMethod.getBeforeGroups();
  }

  @Override
  public String[] getAfterGroups() {
    return testNGMethod.getAfterGroups();
  }

  @Override
  public String[] getMethodsDependedUpon() {
    return testNGMethod.getMethodsDependedUpon();
  }

  @Override
  public void addMethodDependedUpon(String methodName) {
    testNGMethod.addMethodDependedUpon(methodName);
  }

  @Override
  public boolean isTest() {
    return testNGMethod.isTest();
  }

  @Override
  public boolean isBeforeMethodConfiguration() {
    return testNGMethod.isBeforeMethodConfiguration();
  }

  @Override
  public boolean isAfterMethodConfiguration() {
    return testNGMethod.isAfterMethodConfiguration();
  }

  @Override
  public boolean isBeforeClassConfiguration() {
    return testNGMethod.isBeforeClassConfiguration();
  }

  @Override
  public boolean isAfterClassConfiguration() {
    return testNGMethod.isAfterClassConfiguration();
  }

  @Override
  public boolean isBeforeSuiteConfiguration() {
    return testNGMethod.isBeforeSuiteConfiguration();
  }

  @Override
  public boolean isAfterSuiteConfiguration() {
    return testNGMethod.isAfterSuiteConfiguration();
  }

  @Override
  public boolean isBeforeTestConfiguration() {
    return testNGMethod.isBeforeTestConfiguration();
  }

  @Override
  public boolean isAfterTestConfiguration() {
    return testNGMethod.isAfterTestConfiguration();
  }

  @Override
  public boolean isBeforeGroupsConfiguration() {
    return testNGMethod.isBeforeGroupsConfiguration();
  }

  @Override
  public boolean isAfterGroupsConfiguration() {
    return testNGMethod.isAfterGroupsConfiguration();
  }

  @Override
  public long getTimeOut() {
    return testNGMethod.getTimeOut();
  }

  @Override
  public void setTimeOut(long timeOut) {
    testNGMethod.setTimeOut(timeOut);
  }

  @Override
  public int getInvocationCount() {
    return testNGMethod.getInvocationCount();
  }

  @Override
  public void setInvocationCount(int count) {
    testNGMethod.setInvocationCount(count);
  }

  @Override
  public int getSuccessPercentage() {
    return testNGMethod.getSuccessPercentage();
  }

  @Override
  public String getId() {
    return testNGMethod.getId();
  }

  @Override
  public void setId(String id) {
    testNGMethod.setId(id);
  }

  @Override
  public long getDate() {
    return testNGMethod.getDate();
  }

  @Override
  public void setDate(long date) {
    testNGMethod.setDate(date);
  }

  @Override
  public boolean canRunFromClass(IClass testClass) {
    return testNGMethod.canRunFromClass(testClass);
  }

  @Override
  public boolean isAlwaysRun() {
    return testNGMethod.isAlwaysRun();
  }

  @Override
  public int getThreadPoolSize() {
    return testNGMethod.getThreadPoolSize();
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    testNGMethod.setThreadPoolSize(threadPoolSize);
  }

  @Override
  public boolean getEnabled() {
    return testNGMethod.getEnabled();
  }

  @Override
  public String getDescription() {
    return testNGMethod.getDescription();
  }

  @Override
  public void setDescription(String description) {
    testNGMethod.setDescription(description);
  }

  @Override
  public void incrementCurrentInvocationCount() {
    testNGMethod.incrementCurrentInvocationCount();
  }

  @Override
  public int getCurrentInvocationCount() {
    return testNGMethod.getCurrentInvocationCount();
  }

  @Override
  public void setParameterInvocationCount(int n) {
    testNGMethod.setParameterInvocationCount(n);
  }

  @Override
  public int getParameterInvocationCount() {
    return testNGMethod.getParameterInvocationCount();
  }

  @Override
  public void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker) {
    testNGMethod.setMoreInvocationChecker(moreInvocationChecker);
  }

  @Override
  public boolean hasMoreInvocation() {
    return testNGMethod.hasMoreInvocation();
  }

  @Override
  public ITestNGMethod clone() {
    return testNGMethod.clone();
  }

  @Override
  public IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
    return testNGMethod.getRetryAnalyzer(result);
  }

  @Override
  public void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> clazz) {
    testNGMethod.setRetryAnalyzerClass(clazz);
  }

  @Override
  public Class<? extends IRetryAnalyzer> getRetryAnalyzerClass() {
    return testNGMethod.getRetryAnalyzerClass();
  }

  @Override
  public boolean skipFailedInvocations() {
    return testNGMethod.skipFailedInvocations();
  }

  @Override
  public void setSkipFailedInvocations(boolean skip) {
    testNGMethod.setSkipFailedInvocations(skip);
  }

  @Override
  public long getInvocationTimeOut() {
    return testNGMethod.getInvocationTimeOut();
  }

  @Override
  public boolean ignoreMissingDependencies() {
    return testNGMethod.ignoreMissingDependencies();
  }

  @Override
  public void setIgnoreMissingDependencies(boolean ignore) {
    testNGMethod.setIgnoreMissingDependencies(ignore);
  }

  @Override
  public List<Integer> getInvocationNumbers() {
    return testNGMethod.getInvocationNumbers();
  }

  @Override
  public void setInvocationNumbers(List<Integer> numbers) {
    testNGMethod.setInvocationNumbers(numbers);
  }

  @Override
  public void addFailedInvocationNumber(int number) {
    testNGMethod.addFailedInvocationNumber(number);
  }

  @Override
  public List<Integer> getFailedInvocationNumbers() {
    return testNGMethod.getFailedInvocationNumbers();
  }

  @Override
  public int getPriority() {
    return testNGMethod.getPriority();
  }

  @Override
  public void setPriority(int priority) {
    testNGMethod.setPriority(priority);
  }

  @Override
  public int getInterceptedPriority() {
    return testNGMethod.getInterceptedPriority();
  }

  @Override
  public void setInterceptedPriority(int priority) {
    testNGMethod.setInterceptedPriority(priority);
  }

  @Override
  public XmlTest getXmlTest() {
    return testNGMethod.getXmlTest();
  }

  @Override
  public ConstructorOrMethod getConstructorOrMethod() {
    return testNGMethod.getConstructorOrMethod();
  }

  @Override
  public Map<String, String> findMethodParameters(XmlTest test) {
    return testNGMethod.findMethodParameters(test);
  }

  @Override
  public String getQualifiedName() {
    return testNGMethod.getQualifiedName();
  }

  @Override
  public boolean equals(Object o) {
    return testNGMethod.equals(o);
  }

  @Override
  public int hashCode() {
    return multiplicationFactor * testNGMethod.hashCode();
  }
}
