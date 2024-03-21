/*
 * Copyright 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.testng.listener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.ops4j.pax.exam.util.Transactional;
import org.ops4j.spi.ServiceProviderFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.MethodInstance;
import org.testng.internal.NoOpTestClass;

/**
 * TestNG driver for Pax Exam, implementing a number of ITestNGListener interfaces. To run a TestNG
 * test class with Pax Exam, add this class as a listener to your test class:
 *
 * <pre>
 * &#064;Listeners(PaxExam.class)
 * public class MyTest {
 *
 *     &#064;BeforeMethod
 *     public void setUp() {
 *     }
 *
 *     &#064;AfterMethod
 *     public void tearDown() {
 *     }
 *
 *     &#064;Test
 *     public void test1() {
 *     }
 * }
 * </pre>
 *
 * In OSGi and Java EE modes, Pax Exam processes each test class twice, once by test driver and then
 * again inside the test container. The driver delegates each test method invocation to a probe
 * invoker which excutes the test method inside the container via the probe.
 *
 * <p>It would be nice to separate these two aspects and handle them in two separate listeners, but
 * TestNG has no way to override or disable the listener annotated on the test class.
 *
 * <p>TestNG provides a listener callback for configuration methods, but it does not let us
 * intercept them. For this reason, we use an ugly reflection hack to disable them when running
 * under the driver and to make sure they get executed inside the test container only.
 *
 * <p>Dependencies annotated by {@link javax.inject.Inject} get injected into the test class in the
 * container (OSGi and Java EE modes) or by the driver (CDI mode).
 *
 * @author Harald Wellmann
 * @since 2.3.0
 */
public class PaxExam implements ISuiteListener, IMethodInterceptor, IHookable {

  public static final String PAX_EXAM_SUITE_NAME = "PaxExamInternal";

  private static final Logger LOG = LoggerFactory.getLogger(PaxExam.class);

  /**
   * Staged reactor for this test class. This may actually be a reactor already staged for a
   * previous test class, depending on the reactor strategy.
   */
  private StagedExamReactor stagedReactor;

  /**
   * Maps method names to test addresses. The method names are qualified by class and container
   * names. Each method of the test class is cloned for each container.
   */
  private Map<String, TestAddress> methodToAddressMap = new LinkedHashMap<String, TestAddress>();

  /** Reactor manager singleton. */
  private ReactorManager manager;

  /** Shall we use a probe invoker, or invoke test methods directly? */
  private boolean useProbeInvoker;

  /**
   * TestNG calls our intercept() method twice. We remember the first call and do nothing when
   * called again.
   */
  private boolean methodInterceptorCalled;

  /**
   * The test class currently executed. We use this to generate beforeClass and afterClass events,
   * which we do not receive from TestNG.
   */
  private Object currentTestClassInstance;

  private List<ITestNGMethod> methods;

  public PaxExam() {
    LOG.debug("created ExamTestNGListener");
  }

  /**
   * Are we running in the test container or directly under the driver?
   *
   * @param suite current test suite
   * @return true if running in container
   */
  private boolean isRunningInTestContainer(ISuite suite) {
    return suite.getName().equals(PAX_EXAM_SUITE_NAME);
  }

  /**
   * Are we running in the test container or directly under the driver?
   *
   * @param method current test method
   * @return true if running in container
   */
  private boolean isRunningInTestContainer(ITestNGMethod method) {
    return method.getXmlTest().getSuite().getName().equals(PAX_EXAM_SUITE_NAME);
  }

  /**
   * Called by TestNG before the suite starts. When running in the container, this is a no op.
   * Otherwise, we create and stage the reactor.
   *
   * @param suite test suite
   */
  @Override
  public void onStart(ISuite suite) {
    if (!isRunningInTestContainer(suite)) {
      manager = ReactorManager.getInstance();
      stagedReactor = stageReactor(suite);
      manager.beforeSuite(stagedReactor);
    }
  }

  /**
   * Called by TestNG after the suite has finished. When running in the container, this is a no op.
   * Otherwise, we stop the reactor.
   *
   * @param suite test suite
   */
  @Override
  public void onFinish(ISuite suite) {
    if (!isRunningInTestContainer(suite)) {
      // fire an afterClass event for the last test class
      if (currentTestClassInstance != null) {
        manager.afterClass(stagedReactor, currentTestClassInstance.getClass());
      }
      manager.afterSuite(stagedReactor);
    }
  }

  /**
   * Stages the reactor. This involves building the probe including all test methods of the suite
   * and creating one or more test containers.
   *
   * <p>When using a probe invoker, we register the tests with the reactor.
   *
   * <p>Hack: As there is no way to intercept configuration methods, we disable them by reflection.
   *
   * @param suite test suite
   * @return staged reactor
   */
  private synchronized StagedExamReactor stageReactor(ISuite suite) {
    try {
      methods = suite.getAllMethods();
      Class<?> testClass = methods.get(0).getRealClass();
      LOG.debug("test class = {}", testClass);
      disableConfigurationMethods(suite);
      Object testClassInstance = testClass.getDeclaredConstructor().newInstance();
      return stageReactorForClass(testClass, testClassInstance);
    } catch (InstantiationException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException exc) {
      throw new TestContainerException(exc);
    }
  }

  private StagedExamReactor stageReactorForClass(Class<?> testClass, Object testClassInstance) {
    try {
      ExamReactor examReactor = manager.prepareReactor(testClass, testClassInstance);
      useProbeInvoker = !manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI);
      if (useProbeInvoker) {
        addTestsToReactor(examReactor, testClassInstance, methods);
      }
      return manager.stageReactor();
    } catch (IOException | ExamConfigurationException exc) {
      throw new TestContainerException(exc);
    }
  }

  /**
   * Disables the {@code @BeforeMethod} and {@code @AfterMethod} configuration methods of all test
   * classes, overriding the corresponding private fields of {@code TestClass}.
   *
   * <p>These methods shall run only once inside the test container, but not directly under the
   * driver.
   *
   * <p>This is a rather ugly hack, but there does not seem to be any other way.
   *
   * @param suite test suite
   */
  private void disableConfigurationMethods(ISuite suite) {
    Set<ITestClass> seen = new HashSet<ITestClass>();
    for (ITestNGMethod method : suite.getAllMethods()) {
      ITestClass testClass = method.getTestClass();
      if (!seen.contains(testClass)) {
        disableConfigurationMethods(testClass);
        seen.add(testClass);
      }
    }
  }

  /**
   * Adds all tests of the suite to the reactor and creates a probe builder.
   *
   * <p>TODO This driver currently assumes that all test classes of the suite use the default probe
   * builder. It builds one probe containing all tests of the suite. This is why the
   * testClassInstance argument is just an arbitrary instance of one of the classes of the suite.
   *
   * @param reactor unstaged reactor
   * @param testClassInstance not used
   * @param testMethods all methods of the suite.
   * @throws IOException
   * @throws ExamConfigurationException
   */
  private void addTestsToReactor(
      ExamReactor reactor, Object testClassInstance, List<ITestNGMethod> testMethods)
      throws IOException, ExamConfigurationException {
    TestProbeBuilder probe = manager.createProbeBuilder(testClassInstance);
    for (ITestNGMethod m : testMethods) {
      TestAddress address = probe.addTest(m.getRealClass(), m.getMethodName());
      manager.storeTestMethod(address, m);
    }
    reactor.addProbe(probe);
  }

  /**
   * Callback from TestNG which lets us intercept a test method invocation. The two cases of running
   * in the container or under the driver are handled in separate methods.
   */
  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    if (isRunningInTestContainer(testResult.getMethod())) {
      runInTestContainer(callBack, testResult);
    } else {
      runByDriver(callBack, testResult);
    }
  }

  /**
   * Runs a test method in the container. Before invoking the method, we inject its dependencies.
   *
   * <p>TODO Unlike JUnit, TestNG instantiates each test class only once, so maybe we should also
   * inject the dependencies just once.
   *
   * @param callBack TestNG callback for test method
   * @param testResult test result container
   */
  private void runInTestContainer(IHookCallBack callBack, ITestResult testResult) {
    Object testClassInstance = testResult.getInstance();
    inject(testClassInstance);
    if (isTransactional(testResult)) {
      runInTransaction(callBack, testResult);
    } else {
      callBack.runTestMethod(testResult);
    }
    return;
  }

  /**
   * Checks if the current test method is transactional.
   *
   * @param testResult TestNG method and result wrapper
   * @return true if the method or the enclosing class is annotated with {@link Transactional}.
   */
  private boolean isTransactional(ITestResult testResult) {
    boolean transactional = false;
    Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
    if (method.getAnnotation(Transactional.class) != null) {
      transactional = true;
    } else {
      if (method.getDeclaringClass().getAnnotation(Transactional.class) != null) {
        transactional = true;
      }
    }
    return transactional;
  }

  /**
   * Runs a test method enclosed by a Java EE auto-rollback transaction obtained from the JNDI
   * context.
   *
   * @param callBack TestNG callback for test method
   * @param testResult test result container
   */
  private void runInTransaction(IHookCallBack callBack, ITestResult testResult) {
    UserTransaction tx = null;
    try {
      InitialContext ctx = new InitialContext();
      tx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
      tx.begin();
      callBack.runTestMethod(testResult);
    } catch (NamingException | NotSupportedException | SystemException exc) {
      throw new TestContainerException(exc);
    } finally {
      rollback(tx);
    }
  }

  /**
   * Rolls back the given transaction, if not null.
   *
   * @param tx transaction
   */
  private void rollback(UserTransaction tx) {
    if (tx != null) {
      try {
        tx.rollback();
      } catch (IllegalStateException | SecurityException | SystemException exc) {
        throw new TestContainerException(exc);
      }
    }
  }

  /**
   * Performs field injection on the given object. The injection method is looked up via the Java SE
   * service loader.
   *
   * @param testClassInstance test class instance
   */
  private void inject(Object testClassInstance) {
    InjectorFactory injectorFactory =
        ServiceProviderFinder.loadUniqueServiceProvider(InjectorFactory.class);
    Injector injector = injectorFactory.createInjector();
    injector.injectFields(testClassInstance);
  }

  /**
   * Runs a test method under the driver.
   *
   * <p>Fires beforeClass and afterClass events when the current class changes, as we do not get
   * these events from TestNG. This requires the test methods to be sorted by class, see {@link
   * #intercept(List, ITestContext)}.
   *
   * <p>When using a probe invoker, we delegate the test method invocation to the invoker so that
   * the test will be executed in the container context.
   *
   * <p>Otherwise, we directly run the test method.
   *
   * @param callBack TestNG callback for test method
   * @param testResult test result container
   * @throws ExamConfigurationException
   * @throws IOException
   */
  private void runByDriver(IHookCallBack callBack, ITestResult testResult) {
    LOG.info("running {}", testResult.getName());
    Object testClassInstance = testResult.getMethod().getInstance();
    if (testClassInstance != currentTestClassInstance) {
      if (currentTestClassInstance != null) {
        manager.afterClass(stagedReactor, currentTestClassInstance.getClass());
      }
      Class<?> testClass = testClassInstance.getClass();
      stagedReactor = stageReactorForClass(testClass, testClassInstance);
      if (!useProbeInvoker) {
        manager.inject(testClassInstance);
      }
      manager.beforeClass(stagedReactor, testClassInstance);
      currentTestClassInstance = testClassInstance;
    }

    if (!useProbeInvoker) {
      callBack.runTestMethod(testResult);
      return;
    }

    TestAddress address = methodToAddressMap.get(testResult.getName());
    TestAddress root = address.root();

    LOG.debug(
        "Invoke "
            + testResult.getName()
            + " @ "
            + address
            + " Arguments: "
            + Arrays.toString(root.arguments()));
    try {
      stagedReactor.invoke(address);
      testResult.setStatus(ITestResult.SUCCESS);
    }
    // CHECKSTYLE:SKIP : StagedExamReactor API
    catch (Exception e) {
      Throwable t = ExceptionHelper.unwind(e);
      LOG.error("Exception", e);
      testResult.setStatus(ITestResult.FAILURE);
      testResult.setThrowable(t);
    }
  }

  /**
   * Callback from TestNG which lets us manipulate the list of test methods in the suite. When
   * running under the driver and using a probe invoker, we now construct the test addresses to be
   * used be the probe invoker, and we sort the methods by class to make sure we can fire
   * beforeClass and afterClass events later on.
   *
   * <p>For some reason, TestNG invokes this callback twice. The second time over, we return the
   * unchanged method list.
   */
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> testMethods, ITestContext context) {
    if (methodInterceptorCalled
        || !useProbeInvoker
        || isRunningInTestContainer(context.getSuite())) {
      return testMethods;
    }

    methodInterceptorCalled = true;
    boolean mangleMethodNames = manager.getNumConfigurations() > 1;
    TestDirectory testDirectory = TestDirectory.getInstance();
    List<IMethodInstance> newInstances = new ArrayList<IMethodInstance>();
    Set<TestAddress> targets = stagedReactor.getTargets();
    for (TestAddress address : targets) {
      ITestNGMethod frameworkMethod = (ITestNGMethod) manager.lookupTestMethod(address.root());
      if (frameworkMethod == null) {
        continue;
      }
      Method javaMethod = frameworkMethod.getConstructorOrMethod().getMethod();

      if (mangleMethodNames) {
        frameworkMethod = new ReactorTestNGMethod(frameworkMethod, javaMethod, address);
      }

      MethodInstance newInstance = new MethodInstance(frameworkMethod);
      newInstances.add(newInstance);
      methodToAddressMap.put(frameworkMethod.getMethodName(), address);
      testDirectory.add(
          address,
          new TestInstantiationInstruction(
              frameworkMethod.getRealClass().getName() + ";" + javaMethod.getName()));
    }
    newInstances.sort(new IMethodInstanceComparator());
    return newInstances;
  }

  /**
   * Disables BeforeMethod and AfterMethod configuration methods in the given test class.
   *
   * <p>NOTE: Ugly reflection hack, as TestNG does not provide an API for overriding before and
   * after methods.
   *
   * @param testClass TestNG test class wrapper
   */
  private void disableConfigurationMethods(ITestClass testClass) {
    if (testClass instanceof NoOpTestClass) {
      ((NoOpTestClass) testClass).setBeforeTestMethods(new ITestNGMethod[0]);
      ((NoOpTestClass) testClass).setAfterTestMethod(new ITestNGMethod[0]);
    }
  }
}
