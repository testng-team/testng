package org.testng.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.IClass;
import org.testng.IClassListener;
import org.testng.IDataProviderListener;
import org.testng.IInvokedMethodListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SuiteRunState;
import org.testng.internal.ConfigMethodAttributes.Builder;
import org.testng.internal.ITestInvoker.FailureContext;
import org.testng.xml.XmlSuite;

/**
 * This class is responsible for invoking methods: - test methods - configuration methods - possibly
 * in a separate thread and then for notifying the result listeners.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class Invoker implements IInvoker {

  /** Predicate to filter methods */
  static final Predicate<ITestNGMethod, IClass> CAN_RUN_FROM_CLASS =
      new CanRunFromClassPredicate();
  /** Predicate to filter methods */
  static final Predicate<ITestNGMethod, IClass> SAME_CLASS = new SameClassNamePredicate();

  private final TestInvoker m_testInvoker;
  private final ConfigInvoker m_configInvoker;

  public Invoker(
      IConfiguration configuration,
      ITestContext testContext,
      ITestResultNotifier notifier,
      SuiteRunState state,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners,
      Collection<IDataProviderListener> dataProviderListeners) {
    m_configInvoker = new ConfigInvoker(notifier, invokedMethodListeners, testContext, state, configuration);
    m_testInvoker = new TestInvoker(notifier, testContext, state, configuration,
        invokedMethodListeners,
        dataProviderListeners, classListeners, skipFailedInvocationCounts, m_configInvoker);
  }

  public ConfigInvoker getConfigInvoker() {
    return m_configInvoker;
  }

  public TestInvoker getTestInvoker() {
    return m_testInvoker;
  }

  /**
   * Invoke configuration methods if they belong to the same TestClass passed in parameter..
   *
   * <p>TODO: Calculate ahead of time which methods should be invoked for each class. Might speed
   * things up for users who invoke the same test class with different parameters in the same suite
   * run.
   *
   * <p>If instance is non-null, the configuration will be run on it. If it is null, the
   * configuration methods will be run on all the instances retrieved from the ITestClass.
   */
  @Override
  public void invokeConfigurations(
      IClass testClass,
      ITestNGMethod[] allMethods,
      XmlSuite suite,
      Map<String, String> params,
      Object[] parameterValues,
      Object instance) {
    ConfigMethodAttributes attributes = new Builder()
        .forTestClass(testClass)
        .usingConfigMethodsAs(allMethods)
        .forSuite(suite).usingParameters(params)
        .usingParameterValues(parameterValues)
        .usingInstance(instance)
        .build();
    m_configInvoker.invokeConfigurations(attributes);
  }

  /**
   * Invoke all the test methods. Note the plural: the method passed in parameter might be invoked
   * several times if the test class it belongs to has more than one instance (i.e., if an @Factory
   * method has been declared somewhere that returns several instances of this TestClass). If
   * no @Factory method was specified, testMethod will only be invoked once.
   *
   * <p>Note that this method also takes care of invoking the beforeTestMethod and afterTestMethod,
   * if any.
   *
   * <p>Note (alex): this method can be refactored to use a SingleTestMethodWorker that directly
   * invokes {@link ITestInvoker#invokeTestMethod(Object, ITestNGMethod, Object[], int, XmlSuite, Map,
   * ITestClass, ITestNGMethod[], ITestNGMethod[], ConfigurationGroupMethods, FailureContext)} and
   * this would simplify the implementation (see how DataTestMethodWorker is used)
   */
  @Override
  public List<ITestResult> invokeTestMethods(
      ITestNGMethod testMethod,
      Map<String, String> testParameters,
      ConfigurationGroupMethods groupMethods,
      Object instance,
      ITestContext testContext) {
    return m_testInvoker
        .invokeTestMethods(testMethod, groupMethods, instance, testContext);
  }

  interface Predicate<K, T> {
    boolean isTrue(K k, T v);
  }

  static class CanRunFromClassPredicate implements Predicate<ITestNGMethod, IClass> {
    @Override
    public boolean isTrue(ITestNGMethod m, IClass v) {
      return m.canRunFromClass(v);
    }
  }

  static class SameClassNamePredicate implements Predicate<ITestNGMethod, IClass> {
    @Override
    public boolean isTrue(ITestNGMethod m, IClass c) {
      return c == null || m.getTestClass().getName().equals(c.getName());
    }
  }

  static void log(int level, String s) {
    Utils.log("Invoker " + Thread.currentThread().hashCode(), level, s);
  }
}
