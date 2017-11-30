package org.testng.internal;

import org.testng.ClassMethodMap;
import org.testng.IClassListener;
import org.testng.IMethodInstance;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlSuite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FIXME: reduce contention when this class is used through parallel invocation due to
 * invocationCount and threadPoolSize by not invoking the @BeforeClass and @AfterClass
 * which are already invoked on the original method.
 *
 * This class implements Runnable and will invoke the ITestMethod passed in its
 * constructor on its run() method.
 */
public class TestMethodWorker implements IWorker<ITestNGMethod> {

  // Map of the test methods and their associated instances
  // It has to be a set because the same method can be passed several times
  // and associated to a different instance
  private List<IMethodInstance> m_methodInstances;
  private final IInvoker m_invoker;
  private final Map<String, String> m_parameters;
  private final XmlSuite m_suite;
  private List<ITestResult> m_testResults = Lists.newArrayList();
  private final ConfigurationGroupMethods m_groupMethods;
  private final ClassMethodMap m_classMethodMap;
  private final ITestContext m_testContext;
  private final List<IClassListener> m_listeners;

  public TestMethodWorker(IInvoker invoker,
                          List<IMethodInstance> testMethods,
                          XmlSuite suite,
                          Map<String, String> parameters,
                          ConfigurationGroupMethods groupMethods,
                          ClassMethodMap classMethodMap,
                          ITestContext testContext,
                          List<IClassListener> listeners)
  {
    m_invoker = invoker;
    m_methodInstances = testMethods;
    m_suite = suite;
    m_parameters = parameters;
    m_groupMethods = groupMethods;
    m_classMethodMap = classMethodMap;
    m_testContext = testContext;
    m_listeners = listeners;
  }

  /**
   * Retrieves the maximum specified timeout of all ITestNGMethods to
   * be run.
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
    StringBuilder result = new StringBuilder("[Worker thread:" + Thread.currentThread().getId()
        + " priority:" + getPriority() + " ");

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
    for (IMethodInstance testMthdInst : m_methodInstances) {
      ITestNGMethod testMethod = testMthdInst.getMethod();
      ITestClass testClass = testMethod.getTestClass();

      invokeBeforeClassMethods(testClass, testMthdInst);

      // Invoke test method
      try {
        invokeTestMethods(testMethod, testMthdInst.getInstance(), m_testContext);
      } finally {
        invokeAfterClassMethods(testClass, testMthdInst);
      }
    }
  }

  protected void invokeTestMethods(ITestNGMethod tm, Object instance,
      ITestContext testContext)
  {
    // Potential bug here:  we look up the method index of tm among all
    // the test methods (not very efficient) but if this method appears
    // several times and these methods are run in parallel, the results
    // are unpredictable...  Need to think about this more (and make it
    // more efficient)
    List<ITestResult> testResults =
        m_invoker.invokeTestMethods(tm,
            m_suite,
            m_parameters,
            m_groupMethods,
            instance,
            testContext);

    if (testResults != null) {
      m_testResults.addAll(testResults);
    }
  }

  /**
   * Invoke the @BeforeClass methods if not done already
   * @param testClass
   * @param mi
   */
  protected void invokeBeforeClassMethods(ITestClass testClass, IMethodInstance mi) {
    // if no BeforeClass than return immediately
    // used for parallel case when BeforeClass were already invoked
    if (m_classMethodMap == null) {
      return;
    }

    // the whole invocation must be synchronized as other threads must
    // get a full initialized test object (not the same for @After)
    // Synchronization on local variables is generally considered a bad practice, but this is an exception.
    // We need to ensure that two threads that are querying for the same "Class" then they
    // should be mutually exclusive. In all other cases, parallelism can be allowed.
    //DO NOT REMOVE THIS SYNC LOCK.
    synchronized (testClass) {
      Map<ITestClass, Set<Object>> invokedBeforeClassMethods = m_classMethodMap.getInvokedBeforeClassMethods();
      Set<Object> instances = invokedBeforeClassMethods.get(testClass);
      if (null == instances) {
        instances = new HashSet<>();
        invokedBeforeClassMethods.put(testClass, instances);
      }
      Object instance = mi.getInstance();
      if (!instances.contains(instance)) {
        instances.add(instance);
        for (IClassListener listener : m_listeners) {
          listener.onBeforeClass(testClass);
        }
        m_invoker.invokeConfigurations(testClass,
                testClass.getBeforeClassMethods(),
                m_suite,
                m_parameters,
                null, /* no parameter values */
                instance);
      }
    }
  }

  /**
   * Invoke the @AfterClass methods if not done already
   * @param testClass
   * @param mi
   */
  protected void invokeAfterClassMethods(ITestClass testClass, IMethodInstance mi) {
    // if no BeforeClass than return immediately
    // used for parallel case when BeforeClass were already invoked
    if (m_classMethodMap == null) {
      return;
    }

    //
    // Invoke after class methods if this test method is the last one
    //
    List<Object> invokeInstances= Lists.newArrayList();
    ITestNGMethod tm= mi.getMethod();
    boolean removalSuccessful = m_classMethodMap.removeAndCheckIfLast(tm, mi.getInstance());
    if (!removalSuccessful) {
      return;
    }
    Map<ITestClass, Set<Object>> invokedAfterClassMethods = m_classMethodMap.getInvokedAfterClassMethods();
    Set<Object> instances = invokedAfterClassMethods.get(testClass);
    if (null == instances) {
      instances = new HashSet<>();
      invokedAfterClassMethods.put(testClass, instances);
    }
    Object inst = mi.getInstance();
    if (!instances.contains(inst)) {
      invokeInstances.add(inst);
    }

    for (IClassListener listener : m_listeners) {
      listener.onAfterClass(testClass);
    }
    for (Object invokeInstance : invokeInstances) {
      m_invoker.invokeConfigurations(testClass,
              testClass.getAfterClassMethods(),
              m_suite,
              m_parameters,
              null, /* no parameter values */
              invokeInstance);
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
  public List<ITestNGMethod> getTasks()
  {
    List<ITestNGMethod> result = Lists.newArrayList();
    for (IMethodInstance m : m_methodInstances) {
      result.add(m.getMethod());
    }
    return result;
  }

  @Override
  public int compareTo(IWorker<ITestNGMethod> other) {
    return getPriority() - other.getPriority();
  }

  /**
   * The priority of a worker is the priority of the first method it's going to run.
   */
  @Override
  public int getPriority() {
    return m_methodInstances.size() > 0
        ? m_methodInstances.get(0).getMethod().getPriority()
        : 0;
  }
}

/**
 * Extends {@code TestMethodWorker} and is used to work on only a single method
 * instance
 */
class SingleTestMethodWorker extends TestMethodWorker {
  private static final ConfigurationGroupMethods EMPTY_GROUP_METHODS =
    new ConfigurationGroupMethods(new ITestNGMethod[0],
        new HashMap<String, List<ITestNGMethod>>(), new HashMap<String, List<ITestNGMethod>>());

  public SingleTestMethodWorker(IInvoker invoker,
                                IMethodInstance testMethod,
                                XmlSuite suite,
                                Map<String, String> parameters,
                                ITestContext testContext,
                                List<IClassListener> listeners)
  {
    super(invoker,
            asList(testMethod),
          suite,
          parameters,
          EMPTY_GROUP_METHODS,
          null,
          testContext,
          listeners);
  }

  //TODO Resorted to introducing this method to keep JDK7 happy. Can be removed once we move to JDK8
  private static List<IMethodInstance> asList(IMethodInstance testMethod) {
    List<IMethodInstance> methods = Lists.newLinkedList();
    methods.add(testMethod);
    return methods;
  }

}
