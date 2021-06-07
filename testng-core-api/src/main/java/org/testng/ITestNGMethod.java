package org.testng;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.testng.annotations.CustomAttribute;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.IParameterInfo;
import org.testng.xml.XmlTest;

/**
 * Describes a TestNG annotated method and the instance on which it will be invoked.
 *
 * <p>This interface is not meant to be implemented by users.
 */
public interface ITestNGMethod extends Cloneable {

  /**
   * @return The real class on which this method was declared (can be different from
   *     getMethod().getDeclaringClass() if the test method was defined in a superclass).
   */
  Class getRealClass();

  ITestClass getTestClass();

  /**
   * Sets the test class having this method. This is not necessarily the declaring class.
   *
   * @param cls The test class having this method.
   */
  void setTestClass(ITestClass cls);

  /**
   * Returns the method name. This is needed for serialization because methods are not Serializable.
   *
   * @return the method name.
   */
  String getMethodName();

  Object getInstance();

  /**
   * Needed for serialization.
   *
   * @return The hashcode of instances
   */
  long[] getInstanceHashCodes();

  /**
   * @return The groups this method belongs to, possibly added to the groups declared on the class.
   */
  String[] getGroups();

  /**
   * @return The groups this method depends on, possibly added to the groups declared on the class.
   */
  String[] getGroupsDependedUpon();

  /** @return If a group was not found. */
  String getMissingGroup();

  void setMissingGroup(String group);

  String[] getBeforeGroups();

  String[] getAfterGroups();

  /**
   * @return The methods this method depends on, possibly added to the methods declared on the
   *     class.
   */
  String[] getMethodsDependedUpon();

  void addMethodDependedUpon(String methodName);

  /** @return true if this method was annotated with @Test */
  boolean isTest();

  /** @return true if this method was annotated with @Configuration and beforeTestMethod = true */
  boolean isBeforeMethodConfiguration();

  /** @return true if this method was annotated with @Configuration and beforeTestMethod = false */
  boolean isAfterMethodConfiguration();

  /** @return true if this method was annotated with @Configuration and beforeClassMethod = true */
  boolean isBeforeClassConfiguration();

  /** @return true if this method was annotated with @Configuration and beforeClassMethod = false */
  boolean isAfterClassConfiguration();

  /** @return true if this method was annotated with @Configuration and beforeSuite = true */
  boolean isBeforeSuiteConfiguration();

  /** @return true if this method was annotated with @Configuration and afterSuite = true */
  boolean isAfterSuiteConfiguration();

  /** @return <code>true</code> if this method is a @BeforeTest (@Configuration beforeTest=true) */
  boolean isBeforeTestConfiguration();

  /** @return <code>true</code> if this method is an @AfterTest (@Configuration afterTest=true) */
  boolean isAfterTestConfiguration();

  boolean isBeforeGroupsConfiguration();

  boolean isAfterGroupsConfiguration();

  default boolean hasBeforeGroupsConfiguration() {
    return false;
  }

  default boolean hasAfterGroupsConfiguration() {
    return false;
  }

  /** @return The timeout in milliseconds. */
  long getTimeOut();

  void setTimeOut(long timeOut);

  /** @return the number of times this method needs to be invoked. */
  int getInvocationCount();

  void setInvocationCount(int count);

  /** @return the success percentage for this method (between 0 and 100). */
  int getSuccessPercentage();

  /** @return The id of the thread this method was run in. */
  String getId();

  void setId(String id);

  long getDate();

  void setDate(long date);

  /**
   * @param testClass The test class
   * @return true if this ITestNGMethod can be invoked from within IClass.
   */
  boolean canRunFromClass(IClass testClass);

  /** @return true if this method is alwaysRun=true */
  boolean isAlwaysRun();

  /** @return the number of threads to be used when invoking the method on parallel */
  int getThreadPoolSize();

  void setThreadPoolSize(int threadPoolSize);

  boolean getEnabled();

  String getDescription();

  void setDescription(String description);

  void incrementCurrentInvocationCount();

  int getCurrentInvocationCount();

  void setParameterInvocationCount(int n);

  int getParameterInvocationCount();

  void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker);

  boolean hasMoreInvocation();

  ITestNGMethod clone();

  IRetryAnalyzer getRetryAnalyzer(ITestResult result);

  void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> clazz);

  Class<? extends IRetryAnalyzer> getRetryAnalyzerClass();

  boolean skipFailedInvocations();

  void setSkipFailedInvocations(boolean skip);

  /** @return The time under which all invocationCount methods need to complete by. */
  long getInvocationTimeOut();

  boolean ignoreMissingDependencies();

  void setIgnoreMissingDependencies(boolean ignore);

  /**
   * Which invocation numbers of this method should be used (only applicable if it uses a data
   * provider). If this value is an empty list, use all the values returned from the data provider.
   * These values are read from the XML file in the <code>&lt;include invocationNumbers="..."&gt;
   * </code> tag.
   *
   * @return The list of invocation numbers
   */
  List<Integer> getInvocationNumbers();

  void setInvocationNumbers(List<Integer> numbers);

  /**
   * The list of invocation numbers that failed, which is only applicable for methods that have a
   * data provider.
   *
   * @param number The invocation number that failed
   */
  void addFailedInvocationNumber(int number);

  List<Integer> getFailedInvocationNumbers();

  /**
   * The scheduling priority. Lower priorities get scheduled first.
   *
   * @return The priority value
   */
  int getPriority();

  void setPriority(int priority);

  int getInterceptedPriority();

  void setInterceptedPriority(int priority);

  /** @return the XmlTest this method belongs to. */
  XmlTest getXmlTest();

  ConstructorOrMethod getConstructorOrMethod();

  /**
   * @param test - The {@link XmlTest} object.
   * @return the parameters found in the include tag, if any
   */
  Map<String, String> findMethodParameters(XmlTest test);

  /**
   * getRealClass().getName() + "." + getMethodName()
   *
   * @return qualified name for this method
   */
  String getQualifiedName();

  default boolean isDataDriven() {
    return false;
  }

  /**
   * @return - A {@link IParameterInfo} object that represents details about the parameters
   *     associated with the factory method.
   */
  default IParameterInfo getFactoryMethodParamsInfo() {
    return null;
  }

  /**
   * @return - An array of {@link CustomAttribute} that represents the custom attributes associated
   *     with a test.
   */
  default CustomAttribute[] getAttributes() {
    return new CustomAttribute[] {};
  }

  /**
   * @return - An {@link IDataProviderMethod} for a data provider powered test method and <code>null
   *     </code> otherwise.
   */
  default IDataProviderMethod getDataProviderMethod() {
    return null;
  }

  default Class<?>[] getParameterTypes() {
    return new Class<?>[] {};
  }
}
