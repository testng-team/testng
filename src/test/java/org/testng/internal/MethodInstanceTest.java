package org.testng.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

/**
 * Unit tests for {@link MethodInstance}.
 *
 * @author Andreas Kluth
 */
public class MethodInstanceTest {

  public static void main(String[] args) {
    new MethodInstanceTest().sortByIndexSatisfiesContract();
  }

  @Test
  public void sortByIndexSatisfiesContract() {

    // Build a list of entries imposing the same behavior as the live suite, reduced to the
    // minimum to create the same condition.
    List<MethodInstance> methods = new ArrayList<>();
    methods.add(buildMethodInstance("unittests", "StateTest", 1, "aCategorization"));
    methods.add(buildMethodInstance("unittests", "StateTest", 1, "bCategorization"));
    methods.add(buildMethodInstance("unittests", "StateTest", 1, "cCategorization"));
    methods.add(buildMethodInstance("unittests", "StateTest", 1, "dCategorization"));
    methods.add(buildMethodInstance("unittests", "StateTest", 1, "eCategorization"));
    methods.add(buildMethodInstance("unittests", "StateTest", 1, "fCategorization"));
    methods.add(buildMethodInstance("unittests", "StatusTest", 2, "aStatus"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildTestNgFactoryMethodInstance("unittests"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "aChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "bChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "cChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "dChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "eChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "fChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "gChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "eChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "hChangeTest"));
    methods.add(buildMethodInstance("unittests", "ChangeTest", 3, "iChangeTest"));
    methods.add(buildMethodInstance("unittests", "IdentifierClassTest", 4, "aIdentifier"));
    methods.add(buildMethodInstance("unittests", "IdentifierClassTest", 4, "bIdentifier"));
    methods.add(buildMethodInstance("unittests", "StatisticsTest", 0, "aStatistics"));
    methods.add(buildMethodInstance("unittests", "StatisticsTest", 0, "bStatistics"));
    methods.add(buildMethodInstance("unittests", "StatisticsTest", 0, "cStatistics"));

    try {
      Collections.sort(methods, MethodInstance.SORT_BY_INDEX);
    }
    catch (IllegalArgumentException ex) {
      Assert.fail("Comparison method violates its general contract!");
    }
  }

  private MethodInstance buildTestNgFactoryMethodInstance(String xmlTestName) {
    TestClassStub testClass = new TestClassStub(new XmlTestStub(xmlTestName), null);
    return new MethodInstance(new TestNGMethodStub(null, testClass));
  }

  private MethodInstance buildMethodInstance(String xmlTestName, String xmlClassName, int xmlClassIndex, String methodName) {
    TestClassStub testClass = new TestClassStub(new XmlTestStub(xmlTestName), new XmlClassStub(xmlClassName, xmlClassIndex));
    return new MethodInstance(new TestNGMethodStub(methodName, testClass));
  }

  public static class XmlClassStub extends XmlClass {
    private static final long serialVersionUID = 1L;
    private int index;
    private String name;

    public XmlClassStub(String name, int index) {
      this.name = name;
      this.index = index;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public List<XmlInclude> getIncludedMethods() {
      return Collections.emptyList();
    }
  }

  public static class XmlTestStub extends XmlTest {
    private static final long serialVersionUID = 1L;
    private String name;

    public XmlTestStub(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  public static class TestClassStub implements ITestClass {
    private static final long serialVersionUID = 1L;

    private XmlTest xmlTest;
    private XmlClass xmlClass;

    public TestClassStub(XmlTest xmlTest, XmlClass xmlClass) {
      this.xmlTest = xmlTest;
      this.xmlClass = xmlClass;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public XmlTest getXmlTest() {
      return xmlTest;
    }

    @Override
    public XmlClass getXmlClass() {
      return xmlClass;
    }

    @Override
    public String getTestName() {
      return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getRealClass() {
      return null;
    }

    @Override
    public void addInstance(Object instance) {
    }

    @Override
    public Object[] getInstances(boolean reuse) {
      return null;
    }

    @Override
    public long[] getInstanceHashCodes() {
      return null;
    }

    @Override
    public int getInstanceCount() {
      return 0;
    }

    @Override
    public ITestNGMethod[] getTestMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getBeforeTestMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getAfterTestMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getBeforeClassMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getAfterClassMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getBeforeSuiteMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getAfterSuiteMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getBeforeTestConfigurationMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getAfterTestConfigurationMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getBeforeGroupsMethods() {
      return null;
    }

    @Override
    public ITestNGMethod[] getAfterGroupsMethods() {
      return null;
    }

  }

  public static class TestNGMethodStub implements ITestNGMethod {
    private static final long serialVersionUID = 1L;
    private TestClassStub testClassStub;
    private String methodName;

    public TestNGMethodStub(String methodName, TestClassStub testClassStub) {
      this.methodName = methodName;
      this.testClassStub = testClassStub;

    }

    @Override
    public ITestNGMethod clone() {
      return (TestNGMethodStub) this;
    };

    @Override
    public int compareTo(Object o) {
      return 0;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getRealClass() {
      return null;
    }

    @Override
    public ITestClass getTestClass() {
      return testClassStub;
    }

    @Override
    public void setTestClass(ITestClass cls) {
    }

    @Override
    public Method getMethod() {
      return null;
    }

    @Override
    public String getMethodName() {
      return methodName;
    }

    @Override
    public Object[] getInstances() {
      return null;
    }

    @Override
    public Object getInstance() {
      return null;
    }

    @Override
    public long[] getInstanceHashCodes() {
      return null;
    }

    @Override
    public String[] getGroups() {
      return null;
    }

    @Override
    public String[] getGroupsDependedUpon() {
      return null;
    }

    @Override
    public String getMissingGroup() {
      return null;
    }

    @Override
    public void setMissingGroup(String group) {
    }

    @Override
    public String[] getBeforeGroups() {
      return null;
    }

    @Override
    public String[] getAfterGroups() {
      return null;
    }

    @Override
    public String[] getMethodsDependedUpon() {
      return null;
    }

    @Override
    public void addMethodDependedUpon(String methodName) {
    }

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
    public void setTimeOut(long timeOut) {
    }

    @Override
    public int getInvocationCount() {
      return 0;
    }

    @Override
    public int getTotalInvocationCount() {
      return 0;
    }

    @Override
    public void setInvocationCount(int count) {
    }

    @Override
    public int getSuccessPercentage() {
      return 0;
    }

    @Override
    public String getId() {
      return null;
    }

    @Override
    public void setId(String id) {
    }

    @Override
    public long getDate() {
      return 0;
    }

    @Override
    public void setDate(long date) {
    }

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
    public void setThreadPoolSize(int threadPoolSize) {
    }

    @Override
    public boolean getEnabled() {
      return false;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public void setDescription(String description) {
    }

    @Override
    public void incrementCurrentInvocationCount() {
    }

    @Override
    public int getCurrentInvocationCount() {
      return 0;
    }

    @Override
    public void setParameterInvocationCount(int n) {
    }

    @Override
    public int getParameterInvocationCount() {
      return 0;
    }

    @Override
    public IRetryAnalyzer getRetryAnalyzer() {
      return null;
    }

    @Override
    public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer) {
    }

    @Override
    public boolean skipFailedInvocations() {
      return false;
    }

    @Override
    public void setSkipFailedInvocations(boolean skip) {
    }

    @Override
    public long getInvocationTimeOut() {
      return 0;
    }

    @Override
    public boolean ignoreMissingDependencies() {
      return false;
    }

    @Override
    public void setIgnoreMissingDependencies(boolean ignore) {
    }

    @Override
    public List<Integer> getInvocationNumbers() {
      return null;
    }

    @Override
    public void setInvocationNumbers(List<Integer> numbers) {
    }

    @Override
    public void addFailedInvocationNumber(int number) {
    }

    @Override
    public List<Integer> getFailedInvocationNumbers() {
      return null;
    }

    @Override
    public int getPriority() {
      return 0;
    }

    @Override
    public void setPriority(int priority) {
    }

    @Override
    public XmlTest getXmlTest() {
      return null;
    }

    @Override
    public ConstructorOrMethod getConstructorOrMethod() {
      return null;
    }

    @Override
    public Map<String, String> findMethodParameters(XmlTest test) {
      return null;
    }
  }

}
