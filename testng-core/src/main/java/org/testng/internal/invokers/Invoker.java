package org.testng.internal.invokers;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import org.testng.DataProviderHolder;
import org.testng.IClass;
import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.SuiteRunState;
import org.testng.internal.IConfiguration;
import org.testng.internal.ITestResultNotifier;

/**
 * This class is responsible for invoking methods: - test methods - configuration methods - possibly
 * in a separate thread and then for notifying the result listeners.
 */
public class Invoker implements IInvoker {

  /** Predicate to filter methods */
  static final BiPredicate<ITestNGMethod, IClass> CAN_RUN_FROM_CLASS =
      ITestNGMethod::canRunFromClass;
  /** Predicate to filter methods */
  static final BiPredicate<ITestNGMethod, IClass> SAME_CLASS =
      (m, c) -> c == null || m.getTestClass().getName().equals(c.getName());

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
      DataProviderHolder holder) {
    m_configInvoker =
        new ConfigInvoker(notifier, invokedMethodListeners, testContext, state, configuration);
    m_testInvoker =
        new TestInvoker(
            notifier,
            testContext,
            state,
            configuration,
            invokedMethodListeners,
            holder,
            classListeners,
            skipFailedInvocationCounts,
            m_configInvoker);
  }

  public ConfigInvoker getConfigInvoker() {
    return m_configInvoker;
  }

  public TestInvoker getTestInvoker() {
    return m_testInvoker;
  }
}
