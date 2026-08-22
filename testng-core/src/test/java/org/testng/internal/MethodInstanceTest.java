package org.testng.internal;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
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
      methods.sort(MethodInstance.SORT_BY_INDEX);
    } catch (IllegalArgumentException ex) {
      fail("Comparison method violates its general contract!");
    }
  }

  private MethodInstance buildTestNgFactoryMethodInstance(String xmlTestName) {
    // A factory-produced method has no <class> tag of its own, which is the null XmlClass here.
    // It does have a method name, so the stub carries one rather than pretending otherwise.
    TestClassStub testClass = new TestClassStub(new XmlTestStub(xmlTestName), null);
    return new MethodInstance(new TestNGMethodStub("factoryProducedMethod", testClass));
  }

  private MethodInstance buildMethodInstance(
      String xmlTestName, String xmlClassName, int xmlClassIndex, String methodName) {
    TestClassStub testClass =
        new TestClassStub(
            new XmlTestStub(xmlTestName), new XmlClassStub(xmlClassName, xmlClassIndex));
    return new MethodInstance(new TestNGMethodStub(methodName, testClass));
  }

  public static class XmlClassStub extends XmlClass {

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

    private String name;

    public XmlTestStub(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  public static class TestClassStub implements ITestClass, IObject {

    private final @Nullable XmlTest xmlTest;
    private final @Nullable XmlClass xmlClass;

    public TestClassStub(@Nullable XmlTest xmlTest, @Nullable XmlClass xmlClass) {
      this.xmlTest = xmlTest;
      this.xmlClass = xmlClass;
    }

    /** {@code TestResult} names the instance from this, so it must not be null. */
    @Override
    public String getName() {
      return xmlClass == null ? "" : xmlClass.getName();
    }

    @Override
    public @Nullable XmlTest getXmlTest() {
      return xmlTest;
    }

    @Override
    public @Nullable XmlClass getXmlClass() {
      return xmlClass;
    }

    @Override
    public @Nullable String getTestName() {
      return null;
    }

    @Override
    public Class<?> getRealClass() {
      return requireNonNull(xmlClass, "a factory-produced stub has no class tag").getSupportClass();
    }

    @Override
    public void addInstance(Object instance) {}

    @Override
    public void addObject(IObject.IdentifiableObject instance) {
      // Intentionally left blank
    }

    @Override
    public Object[] getInstances(boolean reuse) {
      return new Object[0];
    }

    @Override
    public IObject.IdentifiableObject[] getObjects(
        boolean create, @Nullable String errorMsgPrefix) {
      return new IObject.IdentifiableObject[0];
    }

    @Override
    public long @Nullable [] getInstanceHashCodes() {
      return null;
    }

    @Override
    public ITestNGMethod[] getTestMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeTestMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterTestMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeClassMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterClassMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeSuiteMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterSuiteMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeTestConfigurationMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterTestConfigurationMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getBeforeGroupsMethods() {
      return new ITestNGMethod[0];
    }

    @Override
    public ITestNGMethod[] getAfterGroupsMethods() {
      return new ITestNGMethod[0];
    }
  }

  public static class TestNGMethodStub implements ITestNGMethod {

    private final @Nullable TestClassStub testClassStub;
    private final String methodName;

    public TestNGMethodStub(String methodName, @Nullable TestClassStub testClassStub) {
      this.methodName = methodName;
      this.testClassStub = testClassStub;
    }

    @Override
    public ITestNGMethod clone() {
      return (TestNGMethodStub) this;
    }

    @Override
    public Class<?> getRealClass() {
      throw new UnsupportedOperationException("Pending implementation");
    }

    @Override
    public @Nullable ITestClass getTestClass() {
      return testClassStub;
    }

    @Override
    public void setTestClass(ITestClass cls) {}

    @Override
    public String getMethodName() {
      return methodName;
    }

    @Override
    public @Nullable Object getInstance() {
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
    public @Nullable String getMissingGroup() {
      return null;
    }

    @Override
    public void setMissingGroup(@Nullable String group) {}

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
    public void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker) {}

    @Override
    public boolean hasMoreInvocation() {
      return false;
    }

    @Override
    public int getSuccessPercentage() {
      return 0;
    }

    @Override
    public @Nullable String getId() {
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
    public @Nullable String getDescription() {
      return null;
    }

    @Override
    public void setDescription(@Nullable String description) {}

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
    public @Nullable IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
      return null;
    }

    @Override
    public Class<? extends IRetryAnalyzer> getRetryAnalyzerClass() {
      throw new UnsupportedOperationException("Pending implementation");
    }

    @Override
    public void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> clazz) {}

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
    public int getInterceptedPriority() {
      return 0;
    }

    @Override
    public void setInterceptedPriority(int priority) {}

    @Override
    public @Nullable XmlTest getXmlTest() {
      return null;
    }

    @Override
    public ConstructorOrMethod getConstructorOrMethod() {
      throw new UnsupportedOperationException("Pending implementation");
    }

    @Override
    public Map<String, String> findMethodParameters(XmlTest test) {
      return Collections.emptyMap();
    }

    @Override
    public String getQualifiedName() {
      return getRealClass().getName() + "." + getMethodName();
    }
  }
}
