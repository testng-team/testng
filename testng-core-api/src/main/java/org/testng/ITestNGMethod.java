package org.testng;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
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

  /**
   * @return The test class this method is bound to, or {@code null} while it has not been bound to
   *     one yet.
   */
  @Nullable
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

  /**
   * @return The instance this method will be invoked on, or {@code null} when the method carries no
   *     instance.
   */
  @Nullable
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

  /** @return The group that was not found, or {@code null} when every group was found. */
  @Nullable
  String getMissingGroup();

  void setMissingGroup(@Nullable String group);

  String[] getBeforeGroups();

  String[] getAfterGroups();

  /**
   * @return The methods this method depends on, possibly added to the methods declared on the
   *     class.
   */
  String[] getMethodsDependedUpon();

  /**
   * @return - The set of methods that are dependent on the current method. This information can
   *     help in deciding what other TestNG methods will be skipped if the current method fails. If
   *     the current method is a configuration method, then an empty set is returned. The set is
   *     available by the time an {@link IMethodInterceptor} registered by the user is invoked, and
   *     reflects the graph the run is scheduled on from the moment the first test method starts.
   */
  default Set<ITestNGMethod> downstreamDependencies() {
    throw new UnsupportedOperationException("Pending implementation");
  }

  /**
   * @return - The set of methods upon which the current method has a dependency. This information
   *     can help in deciding what all TestNG methods need to pass before the current method can be
   *     executed. If the current method is a configuration method, then an empty set is returned.
   *     The set is available by the time an {@link IMethodInterceptor} registered by the user is
   *     invoked, and reflects the graph the run is scheduled on from the moment the first test
   *     method starts.
   */
  default Set<ITestNGMethod> upstreamDependencies() {
    throw new UnsupportedOperationException("Pending implementation");
  }

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

  /** @return The id of the thread this method was run in, or {@code null} before it has run. */
  @Nullable
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

  /** @return The description of this method, or {@code null} when it declares none. */
  @Nullable
  String getDescription();

  void setDescription(@Nullable String description);

  void incrementCurrentInvocationCount();

  int getCurrentInvocationCount();

  void setParameterInvocationCount(int n);

  int getParameterInvocationCount();

  void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker);

  boolean hasMoreInvocation();

  ITestNGMethod clone();

  /**
   * @param result The result to pick a retry analyzer for.
   * @return The retry analyzer for that result, or {@code null} when the method declares none.
   */
  @Nullable
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

  /** @return the XmlTest this method belongs to, or {@code null} when it belongs to none. */
  @Nullable
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
   *     associated with the factory method, or {@code null} when no factory produced the test
   *     class.
   * @deprecated - As of TestNG <code>v7.13.0</code>. It exposes a type from an internal package;
   *     use {@link #getFactoryInstance()} instead.
   */
  @Deprecated
  @Nullable
  default IParameterInfo getFactoryMethodParamsInfo() {
    return null;
  }

  /**
   * Returns the <code>&#64;Factory</code> produced instance this method is bound to.
   *
   * @return - The instance, or an empty {@link Optional} when no factory produced the test class.
   *     Reading it never instantiates a lazily created instance.
   * @since 7.13.0
   */
  default Optional<IFactoryInstance> getFactoryInstance() {
    return Optional.empty();
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
   * </code> otherwise.
   */
  @Nullable
  default IDataProviderMethod getDataProviderMethod() {
    return null;
  }

  default Class<?>[] getParameterTypes() {
    return new Class<?>[] {};
  }

  /**
   * @return - <code>true</code> if the configuration failure arising out of this method should be
   *     ignored.
   */
  default boolean isIgnoreFailure() {
    return false;
  }
}
