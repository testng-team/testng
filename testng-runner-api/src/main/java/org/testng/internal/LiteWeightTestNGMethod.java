package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.testng.IClass;
import org.testng.IDataProviderMethod;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.xml.XmlTest;

public class LiteWeightTestNGMethod implements ITestNGMethod {

  private final Class<?> realClass;
  private ITestClass testClass;
  private final String methodName;
  private final Object instance;
  private final long[] instanceHashCodes;
  private final String[] groups;
  private final String[] groupsDependedUpon;
  private String missingGroup;
  private final String[] beforeGroups;
  private final String[] afterGroups;
  private final List<String> methodsDependedUpon = new LinkedList<>();
  private int priority;
  private int interceptedPriority;
  private final XmlTest xmlTest;
  private final String qualifiedName;
  private final boolean isBeforeTestConfiguration;
  private final boolean isAfterTestConfiguration;
  private final boolean isBeforeGroupsConfiguration;
  private final boolean isAfterGroupsConfiguration;
  private final boolean isTest;
  private final boolean isBeforeMethodConfiguration;
  private final boolean isAfterMethodConfiguration;
  private final boolean isBeforeClassConfiguration;
  private final boolean isAfterClassConfiguration;
  private final boolean isBeforeSuiteConfiguration;
  private final boolean isAfterSuiteConfiguration;
  private List<Integer> invocationNumbers;
  private final List<Integer> failedInvocationNumbers;
  private boolean ignoreMissingDependencies;
  private final long invocationTimeout;
  private boolean skipFailedInvocations;

  private long timeout;
  private int invocationCount;
  private final int successPercentage;
  private String id;
  private long date;
  private final boolean isAlwaysRun;
  private int threadPoolSize;
  private final boolean enabled;
  private String description;
  private final int currentInvocationCount;
  private int parameterInvocationCount;
  private final boolean hasMoreInvocation;
  private final Class<? extends IRetryAnalyzer> retryAnalyzerClass;
  private final String toString;
  private final IDataProviderMethod dataProviderMethod;
  private final int hashCode;
  private final Class<?>[] parameterTypes;

  public LiteWeightTestNGMethod(ITestNGMethod iTestNGMethod) {
    realClass = iTestNGMethod.getRealClass();
    testClass = iTestNGMethod.getTestClass();
    methodName = iTestNGMethod.getMethodName();
    instance = iTestNGMethod.getInstance();
    instanceHashCodes = iTestNGMethod.getInstanceHashCodes();
    groups = iTestNGMethod.getGroups();
    groupsDependedUpon = iTestNGMethod.getGroupsDependedUpon();
    missingGroup = iTestNGMethod.getMissingGroup();
    beforeGroups = iTestNGMethod.getBeforeGroups();
    afterGroups = iTestNGMethod.getAfterGroups();
    if (iTestNGMethod.getMethodsDependedUpon() != null) {
      methodsDependedUpon.addAll(Arrays.asList(iTestNGMethod.getMethodsDependedUpon()));
    }
    priority = iTestNGMethod.getPriority();
    interceptedPriority = iTestNGMethod.getInterceptedPriority();
    xmlTest = iTestNGMethod.getXmlTest();
    qualifiedName = iTestNGMethod.getQualifiedName();
    isBeforeTestConfiguration = iTestNGMethod.isBeforeTestConfiguration();
    isAfterTestConfiguration = iTestNGMethod.isAfterTestConfiguration();
    isBeforeGroupsConfiguration = iTestNGMethod.isBeforeGroupsConfiguration();
    isAfterGroupsConfiguration = iTestNGMethod.isAfterGroupsConfiguration();
    isTest = iTestNGMethod.isTest();
    isBeforeMethodConfiguration = iTestNGMethod.isBeforeMethodConfiguration();
    isAfterMethodConfiguration = iTestNGMethod.isAfterMethodConfiguration();
    isBeforeClassConfiguration = iTestNGMethod.isBeforeClassConfiguration();
    isAfterClassConfiguration = iTestNGMethod.isAfterClassConfiguration();
    isBeforeSuiteConfiguration = iTestNGMethod.isBeforeSuiteConfiguration();
    isAfterSuiteConfiguration = iTestNGMethod.isAfterSuiteConfiguration();
    invocationNumbers = iTestNGMethod.getInvocationNumbers();
    failedInvocationNumbers = iTestNGMethod.getFailedInvocationNumbers();
    ignoreMissingDependencies = iTestNGMethod.ignoreMissingDependencies();
    invocationTimeout = iTestNGMethod.getInvocationTimeOut();
    skipFailedInvocations = iTestNGMethod.skipFailedInvocations();
    timeout = iTestNGMethod.getTimeOut();
    invocationCount = iTestNGMethod.getInvocationCount();
    successPercentage = iTestNGMethod.getSuccessPercentage();
    id = iTestNGMethod.getId();
    date = iTestNGMethod.getDate();
    isAlwaysRun = iTestNGMethod.isAlwaysRun();
    threadPoolSize = iTestNGMethod.getThreadPoolSize();
    enabled = iTestNGMethod.getEnabled();
    description = iTestNGMethod.getDescription();
    currentInvocationCount = iTestNGMethod.getCurrentInvocationCount();
    parameterInvocationCount = iTestNGMethod.getParameterInvocationCount();
    hasMoreInvocation = iTestNGMethod.hasMoreInvocation();
    retryAnalyzerClass = iTestNGMethod.getRetryAnalyzerClass();
    toString = iTestNGMethod.toString();
    IDataProviderMethod dp = iTestNGMethod.getDataProviderMethod();
    dataProviderMethod =
        new IDataProviderMethod() {
          @Override
          public Object getInstance() {
            return dp.getInstance();
          }

          @Override
          public Method getMethod() {
            throw new UnsupportedOperationException("method() retrieval not supported");
          }

          @Override
          public String getName() {
            if (dp == null) {
              return "";
            }
            return dp.getName();
          }

          @Override
          public boolean isParallel() {
            if (dp == null) {
              return false;
            }
            return dp.isParallel();
          }

          @Override
          public List<Integer> getIndices() {
            if (dp == null) {
              return Lists.newArrayList();
            }
            return dp.getIndices();
          }
        };
    hashCode = iTestNGMethod.hashCode();
    parameterTypes = iTestNGMethod.getConstructorOrMethod().getParameterTypes();
  }

  @Override
  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }

  @Override
  public Class getRealClass() {
    return realClass;
  }

  @Override
  public ITestClass getTestClass() {
    return testClass;
  }

  @Override
  public void setTestClass(ITestClass cls) {
    this.testClass = cls;
  }

  @Override
  public String getMethodName() {
    return methodName;
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public long[] getInstanceHashCodes() {
    return instanceHashCodes;
  }

  @Override
  public String[] getGroups() {
    return groups;
  }

  @Override
  public String[] getGroupsDependedUpon() {
    return groupsDependedUpon;
  }

  @Override
  public String getMissingGroup() {
    return missingGroup;
  }

  @Override
  public void setMissingGroup(String group) {
    this.missingGroup = group;
  }

  @Override
  public String[] getBeforeGroups() {
    return beforeGroups;
  }

  @Override
  public String[] getAfterGroups() {
    return afterGroups;
  }

  @Override
  public String[] getMethodsDependedUpon() {
    return methodsDependedUpon.toArray(new String[0]);
  }

  @Override
  public void addMethodDependedUpon(String methodName) {
    methodsDependedUpon.add(methodName);
  }

  @Override
  public boolean isTest() {
    return isTest;
  }

  @Override
  public boolean isBeforeMethodConfiguration() {
    return isBeforeMethodConfiguration;
  }

  @Override
  public boolean isAfterMethodConfiguration() {
    return isAfterMethodConfiguration;
  }

  @Override
  public boolean isBeforeClassConfiguration() {
    return isBeforeClassConfiguration;
  }

  @Override
  public boolean isAfterClassConfiguration() {
    return isAfterClassConfiguration;
  }

  @Override
  public boolean isBeforeSuiteConfiguration() {
    return isBeforeSuiteConfiguration;
  }

  @Override
  public boolean isAfterSuiteConfiguration() {
    return isAfterSuiteConfiguration;
  }

  @Override
  public boolean isBeforeTestConfiguration() {
    return isBeforeTestConfiguration;
  }

  @Override
  public boolean isAfterTestConfiguration() {
    return isAfterTestConfiguration;
  }

  @Override
  public boolean isBeforeGroupsConfiguration() {
    return isBeforeGroupsConfiguration;
  }

  @Override
  public boolean isAfterGroupsConfiguration() {
    return isAfterGroupsConfiguration;
  }

  @Override
  public long getTimeOut() {
    return timeout;
  }

  @Override
  public void setTimeOut(long timeOut) {
    this.timeout = timeOut;
  }

  @Override
  public int getInvocationCount() {
    return invocationCount;
  }

  @Override
  public void setInvocationCount(int count) {
    this.invocationCount = count;
  }

  @Override
  public int getSuccessPercentage() {
    return successPercentage;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public long getDate() {
    return date;
  }

  @Override
  public void setDate(long date) {
    this.date = date;
  }

  @Override
  public boolean canRunFromClass(IClass testClass) {
    return getTestClass().getRealClass().isAssignableFrom(testClass.getRealClass());
  }

  @Override
  public boolean isAlwaysRun() {
    return isAlwaysRun;
  }

  @Override
  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    this.threadPoolSize = threadPoolSize;
  }

  @Override
  public boolean getEnabled() {
    return enabled;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void incrementCurrentInvocationCount() {}

  @Override
  public int getCurrentInvocationCount() {
    return currentInvocationCount;
  }

  @Override
  public void setParameterInvocationCount(int n) {
    this.parameterInvocationCount = n;
  }

  @Override
  public int getParameterInvocationCount() {
    return parameterInvocationCount;
  }

  @Override
  public void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker) {}

  @Override
  public boolean hasMoreInvocation() {
    return hasMoreInvocation;
  }

  @Override
  public ITestNGMethod clone() {
    throw new UnsupportedOperationException("clone() not supported");
  }

  @Override
  public IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
    throw new UnsupportedOperationException("getRetryAnalyzer() not supported");
  }

  @Override
  public void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> clazz) {}

  @Override
  public Class<? extends IRetryAnalyzer> getRetryAnalyzerClass() {
    return retryAnalyzerClass;
  }

  @Override
  public boolean skipFailedInvocations() {
    return skipFailedInvocations;
  }

  @Override
  public void setSkipFailedInvocations(boolean skip) {
    this.skipFailedInvocations = skip;
  }

  @Override
  public long getInvocationTimeOut() {
    return invocationTimeout;
  }

  @Override
  public boolean ignoreMissingDependencies() {
    return ignoreMissingDependencies;
  }

  @Override
  public void setIgnoreMissingDependencies(boolean ignore) {
    this.ignoreMissingDependencies = ignore;
  }

  @Override
  public List<Integer> getInvocationNumbers() {
    return invocationNumbers;
  }

  @Override
  public void setInvocationNumbers(List<Integer> numbers) {
    this.invocationNumbers = numbers;
  }

  @Override
  public void addFailedInvocationNumber(int number) {
    failedInvocationNumbers.add(number);
  }

  @Override
  public List<Integer> getFailedInvocationNumbers() {
    return failedInvocationNumbers;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public void setPriority(int priority) {
    this.priority = priority;
  }

  @Override
  public int getInterceptedPriority() {
    return interceptedPriority;
  }

  @Override
  public void setInterceptedPriority(int priority) {
    this.interceptedPriority = priority;
  }

  @Override
  public XmlTest getXmlTest() {
    return xmlTest;
  }

  @Override
  public ConstructorOrMethod getConstructorOrMethod() {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public Map<String, String> findMethodParameters(XmlTest test) {
    return XmlTestUtils.findMethodParameters(xmlTest, getTestClass().getName(), getMethodName());
  }

  @Override
  public String getQualifiedName() {
    return qualifiedName;
  }

  @Override
  public IDataProviderMethod getDataProviderMethod() {
    return dataProviderMethod;
  }

  @Override
  public String toString() {
    return toString;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object o) {
    // Intentionally not checking the parameter type
    if (this == o) {
      return true;
    }
    if (!(o instanceof ITestNGMethod)) {
      return false;
    }

    ITestNGMethod that = (ITestNGMethod) o;
    boolean value =
        realClass.equals(that.getRealClass())
            && qualifiedName.equals(that.getQualifiedName())
            && equalParamTypes(parameterTypes, that.getParameterTypes());
    return value;
  }

  boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
    if (params1.length == params2.length) {
      for (int i = 0; i < params1.length; i++) {
        if (params1[i] != params2[i]) return false;
      }
      return true;
    }
    return false;
  }
}
