package org.testng.internal.paramhandler;

import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.MethodInstanceTest;
import org.testng.internal.reflect.ReflectionHelper;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class FakeTestNGMethod implements ITestNGMethod {
  private final ConstructorOrMethod consMethod;
  private Class<?> clazz;
  private XmlTest xmlTest;

  public FakeTestNGMethod(Class<?> clazz, String methodName) {
    this(clazz, methodName, null);
  }

  public FakeTestNGMethod(Class<?> clazz, String methodName, XmlTest xmlTest) {
    ConstructorOrMethod temp = null;
    Method[] methods = ReflectionHelper.getLocalMethods(clazz);
    for (Method method : methods) {
      if (method.getName().equalsIgnoreCase(methodName)) {
        temp = new ConstructorOrMethod(method);
        break;
      }
    }
    this.xmlTest = xmlTest;
    this.clazz = clazz;
    this.consMethod = temp;
  }

  @Override
  public Class<?> getRealClass() {
    return clazz;
  }

  @Override
  public ITestClass getTestClass() {
    return new MethodInstanceTest.TestClassStub(this.xmlTest, new XmlClass(this.clazz));
  }

  @Override
  public void setTestClass(ITestClass cls) {}

  @Override
  public String getMethodName() {
    return null;
  }

  @Override
  public Object getInstance() {
    return null;
  }

  @Override
  public long[] getInstanceHashCodes() {
    return new long[0];
  }

  @Override
  public String[] getGroups() {
    return new String[0];
  }

  @Override
  public String[] getGroupsDependedUpon() {
    return new String[0];
  }

  @Override
  public String getMissingGroup() {
    return null;
  }

  @Override
  public void setMissingGroup(String group) {}

  @Override
  public String[] getBeforeGroups() {
    return new String[0];
  }

  @Override
  public String[] getAfterGroups() {
    return new String[0];
  }

  @Override
  public String[] getMethodsDependedUpon() {
    return new String[0];
  }

  @Override
  public void addMethodDependedUpon(String methodName) {}

  @Override
  public boolean isTest() {
    return false;
  }

  @Override
  public boolean isBeforeMethodConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterMethodConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeClassConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterClassConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeSuiteConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterSuiteConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeTestConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterTestConfiguration() {
    return false;
  }

  @Override
  public boolean isBeforeGroupsConfiguration() {
    return false;
  }

  @Override
  public boolean isAfterGroupsConfiguration() {
    return false;
  }

  @Override
  public long getTimeOut() {
    return 0;
  }

  @Override
  public void setTimeOut(long timeOut) {}

  @Override
  public int getInvocationCount() {
    return 0;
  }

  @Override
  public void setInvocationCount(int count) {}

  @Override
  public int getSuccessPercentage() {
    return 0;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public void setId(String id) {}

  @Override
  public long getDate() {
    return 0;
  }

  @Override
  public void setDate(long date) {}

  @Override
  public boolean canRunFromClass(IClass testClass) {
    return false;
  }

  @Override
  public boolean isAlwaysRun() {
    return false;
  }

  @Override
  public int getThreadPoolSize() {
    return 0;
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {}

  @Override
  public boolean getEnabled() {
    return false;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void setDescription(String description) {}

  @Override
  public void incrementCurrentInvocationCount() {}

  @Override
  public int getCurrentInvocationCount() {
    return 0;
  }

  @Override
  public void setParameterInvocationCount(int n) {}

  @Override
  public int getParameterInvocationCount() {
    return 0;
  }

  @Override
  public void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker) {}

  @Override
  public boolean hasMoreInvocation() {
    return false;
  }

  @Override
  public ITestNGMethod clone() {
    throw new UnsupportedOperationException("Not supported");
  }

  @Override
  public IRetryAnalyzer getRetryAnalyzer() {
    return null;
  }

  @Override
  public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer) {}

  @Override
  public boolean skipFailedInvocations() {
    return false;
  }

  @Override
  public void setSkipFailedInvocations(boolean skip) {}

  @Override
  public long getInvocationTimeOut() {
    return 0;
  }

  @Override
  public boolean ignoreMissingDependencies() {
    return false;
  }

  @Override
  public void setIgnoreMissingDependencies(boolean ignore) {}

  @Override
  public List<Integer> getInvocationNumbers() {
    return Collections.emptyList();
  }

  @Override
  public void setInvocationNumbers(List<Integer> numbers) {}

  @Override
  public void addFailedInvocationNumber(int number) {}

  @Override
  public List<Integer> getFailedInvocationNumbers() {
    return Collections.emptyList();
  }

  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public void setPriority(int priority) {}

  @Override
  public XmlTest getXmlTest() {
    return null;
  }

  @Override
  public ConstructorOrMethod getConstructorOrMethod() {
    return consMethod;
  }

  @Override
  public Map<String, String> findMethodParameters(XmlTest test) {
    return test.getLocalParameters();
  }

  @Override
  public String getQualifiedName() {
    return null;
  }
}
