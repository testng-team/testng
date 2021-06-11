package org.testng.internal.invokers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.testng.ClassMethodMap;
import org.testng.IClassListener;
import org.testng.IMethodInstance;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.internal.*;
import org.testng.internal.invokers.ConfigMethodArguments.Builder;
import org.testng.thread.IWorker;

/**
 * FIXME: reduce contention when this class is used through parallel invocation due to
 * invocationCount and threadPoolSize by not invoking the @BeforeClass and @AfterClass which are
 * already invoked on the original method.
 *
 * <p>This class implements Runnable and will invoke the ITestMethod passed in its constructor on
 * its run() method.
 */
public class TestMethodWorker implements IWorker<ITestNGMethod> {

  // Map of the test methods and their associated instances
  // It has to be a set because the same method can be passed several times
  // and associated to a different instance
  private final List<IMethodInstance> m_methodInstances;
  private final Map<String, String> m_parameters;
  private final List<ITestResult> m_testResults = Lists.newArrayList();
  private final ConfigurationGroupMethods m_groupMethods;
  private final ClassMethodMap m_classMethodMap;
  private final ITestContext m_testContext;
  private final List<IClassListener> m_listeners;
  private long currentThreadId;
  private long threadIdToRunOn = -1;
  private boolean completed = true;
  private final ITestInvoker m_testInvoker;
  private final IConfigInvoker m_configInvoker;

  public TestMethodWorker(
      ITestInvoker testInvoker,
      IConfigInvoker configInvoker,
      List<IMethodInstance> testMethods,
      Map<String, String> parameters,
      ConfigurationGroupMethods groupMethods,
      ClassMethodMap classMethodMap,
      ITestContext testContext,
      List<IClassListener> listeners) {
    this.m_testInvoker = testInvoker;
    this.m_configInvoker = configInvoker;
    m_methodInstances = testMethods;
    m_parameters = parameters;
    m_groupMethods = groupMethods;
    m_classMethodMap = classMethodMap;
    m_testContext = testContext;
    m_listeners = listeners;
  }

  /**
   * Retrieves the maximum specified timeout of all ITestNGMethods to be run.
   *
   * @return the max timeout or 0 if no timeout was specified
   */
  @Override
  public long getTimeOut() {
    long result = 0;
    for (IMethodInstance mi : m_methodInstances) {
      ITestNGMethod tm = mi.getMethod();
      if (tm.getTimeOut() > result) {
        result = tm.getTimeOut();
      }
    }

    return result;
  }

  @Override
  public String toString() {
    StringBuilder result =
        new StringBuilder(
            "[Worker thread:"
                + Thread.currentThread().getId()
                + " priority:"
                + getPriority()
                + " ");

    for (IMethodInstance m : m_methodInstances) {
      result.append(m.getMethod()).append(" ");
    }
    result.append("]");

    return result.toString();
  }

  /**
   * Run all the ITestNGMethods passed in through the constructor.
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    this.currentThreadId = Thread.currentThread().getId();
    if (RuntimeBehavior.enforceThreadAffinity()
        && doesTaskHavePreRequistes()
        && currentThreadId != threadIdToRunOn) {
      completed = false;
      return;
    }

    for (IMethodInstance testMthdInst : m_methodInstances) {
      ITestNGMethod testMethod = testMthdInst.getMethod();
      if (canInvokeBeforeClassMethods()) {
        synchronized (testMethod.getInstance()) {
          invokeBeforeClassMethods(testMethod.getTestClass(), testMthdInst);
        }
      }

      // Invoke test method
      try {
        invokeTestMethods(testMethod, testMthdInst.getInstance());
      } finally {
        invokeAfterClassMethods(testMethod.getTestClass(), testMthdInst);
      }
    }
  }

  private boolean doesTaskHavePreRequistes() {
    return threadIdToRunOn != -1;
  }

  protected void invokeTestMethods(ITestNGMethod tm, Object instance) {
    // Potential bug here:  we look up the method index of tm among all
    // the test methods (not very efficient) but if this method appears
    // several times and these methods are run in parallel, the results
    // are unpredictable...  Need to think about this more (and make it
    // more efficient)
    List<ITestResult> testResults =
        m_testInvoker.invokeTestMethods(tm, m_groupMethods, instance, m_testContext);

    if (testResults != null) {
      m_testResults.addAll(testResults);
    }
  }

  private boolean canInvokeBeforeClassMethods() {
    return m_classMethodMap != null;
  }

  /** Invoke the @BeforeClass methods if not done already */
  protected void invokeBeforeClassMethods(ITestClass testClass, IMethodInstance mi) {
    Map<ITestClass, Set<Object>> invokedBeforeClassMethods =
        m_classMethodMap.getInvokedBeforeClassMethods();
    Set<Object> instances =
        invokedBeforeClassMethods.computeIfAbsent(testClass, key -> Sets.newHashSet());
    Object instance = mi.getInstance();
    if (!instances.contains(instance)) {
      instances.add(instance);
      for (IClassListener listener : m_listeners) {
        listener.onBeforeClass(testClass);
      }
      ConfigMethodArguments attributes =
          new Builder()
              .forTestClass(testClass)
              .usingConfigMethodsAs(
                  ((ITestClassConfigInfo) testClass).getInstanceBeforeClassMethods(instance))
              .forSuite(m_testContext.getSuite().getXmlSuite())
              .usingParameters(m_parameters)
              .usingInstance(instance)
              .build();
      m_configInvoker.invokeConfigurations(attributes);
    }
  }

  /** Invoke the @AfterClass methods if not done already */
  protected void invokeAfterClassMethods(ITestClass testClass, IMethodInstance mi) {
    // if no BeforeClass than return immediately
    // used for parallel case when BeforeClass were already invoked
    if (m_classMethodMap == null) {
      return;
    }

    //
    // Invoke after class methods if this test method is the last one
    //
    List<Object> invokeInstances = Lists.newArrayList();
    ITestNGMethod tm = mi.getMethod();
    boolean removalSuccessful = m_classMethodMap.removeAndCheckIfLast(tm, mi.getInstance());
    if (!removalSuccessful) {
      return;
    }
    Map<ITestClass, Set<Object>> invokedAfterClassMethods =
        m_classMethodMap.getInvokedAfterClassMethods();
    Set<Object> instances =
        invokedAfterClassMethods.computeIfAbsent(testClass, key -> Sets.newHashSet());
    Object inst = mi.getInstance();
    if (!instances.contains(inst)) {
      invokeInstances.add(inst);
    }

    for (IClassListener listener : m_listeners) {
      listener.onAfterClass(testClass);
    }
    for (Object invokeInstance : invokeInstances) {
      ConfigMethodArguments attributes =
          new Builder()
              .forTestClass(testClass)
              .usingConfigMethodsAs(testClass.getAfterClassMethods())
              .forSuite(m_testContext.getSuite().getXmlSuite())
              .usingParameters(m_parameters)
              .usingInstance(invokeInstance)
              .build();
      m_configInvoker.invokeConfigurations(attributes);
    }
  }

  protected int indexOf(ITestNGMethod tm, ITestNGMethod[] allTestMethods) {
    for (int i = 0; i < allTestMethods.length; i++) {
      if (allTestMethods[i] == tm) {
        return i;
      }
    }
    return -1;
  }

  public List<ITestResult> getTestResults() {
    return m_testResults;
  }

  @Override
  public List<ITestNGMethod> getTasks() {
    List<ITestNGMethod> result = Lists.newArrayList();
    for (IMethodInstance m : m_methodInstances) {
      result.add(m.getMethod());
    }
    return result;
  }

  @Override
  public int compareTo(@Nonnull IWorker<ITestNGMethod> other) {
    if (m_methodInstances.isEmpty()) {
      return 0;
    }
    List<ITestNGMethod> otherTasks = other.getTasks();
    if (otherTasks.isEmpty()) {
      return 0;
    }
    return TestMethodComparator.compareStatic(
        m_methodInstances.get(0).getMethod(), otherTasks.get(0));
  }

  /** The priority of a worker is the priority of the first method it's going to run. */
  @Override
  public int getPriority() {
    return m_methodInstances.size() > 0 ? m_methodInstances.get(0).getMethod().getPriority() : 0;
  }

  @Override
  public long getCurrentThreadId() {
    return currentThreadId;
  }

  @Override
  public void setThreadIdToRunOn(long threadIdToRunOn) {
    this.threadIdToRunOn = threadIdToRunOn;
  }

  @Override
  public boolean completed() {
    return this.completed;
  }
}

/** Extends {@code TestMethodWorker} and is used to work on only a single method instance */
class SingleTestMethodWorker extends TestMethodWorker {
  private static final ConfigurationGroupMethods EMPTY_GROUP_METHODS =
      new ConfigurationGroupMethods(
          new TestMethodContainer(() -> new ITestNGMethod[0]), new HashMap<>(), new HashMap<>());

  public SingleTestMethodWorker(
      TestInvoker testInvoker,
      ConfigInvoker configInvoker,
      IMethodInstance testMethod,
      Map<String, String> parameters,
      ITestContext testContext,
      List<IClassListener> listeners) {
    super(
        testInvoker,
        configInvoker,
        Collections.singletonList(testMethod),
        parameters,
        EMPTY_GROUP_METHODS,
        null,
        testContext,
        listeners);
  }
}
