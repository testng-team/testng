package org.testng.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.IClass;
import org.testng.IClassListener;
import org.testng.IConfigurable;
import org.testng.IDataProviderListener;
import org.testng.IHookable;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.SuiteRunState;
import org.testng.TestException;
import org.testng.TestNGException;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.InvokeMethodRunnable.TestNGRuntimeException;
import static org.testng.internal.ParameterHandler.ParameterBag;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.InvokedMethodListenerInvoker;
import org.testng.internal.invokers.InvokedMethodListenerMethod;
import org.testng.internal.thread.ThreadExecutionException;
import org.testng.internal.thread.ThreadUtil;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;

import static org.testng.internal.invokers.InvokedMethodListenerMethod.AFTER_INVOCATION;
import static org.testng.internal.invokers.InvokedMethodListenerMethod.BEFORE_INVOCATION;

/**
 * This class is responsible for invoking methods:
 * - test methods
 * - configuration methods
 * - possibly in a separate thread
 * and then for notifying the result listeners.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class Invoker implements IInvoker {
  private final ITestContext m_testContext;
  private final ITestResultNotifier m_notifier;
  private final IAnnotationFinder m_annotationFinder;
  private final SuiteRunState m_suiteState;
  private final boolean m_skipFailedInvocationCounts;
  private final Collection<IInvokedMethodListener> m_invokedMethodListeners;
  private final boolean m_continueOnFailedConfiguration;
  private final List<IClassListener> m_classListeners;
  private final Collection<IDataProviderListener> m_dataproviderListeners;

  /** Group failures must be synced as the Invoker is accessed concurrently */
  private final Map<String, Boolean> m_beforegroupsFailures = Maps.newConcurrentMap();

  /** Class failures must be synced as the Invoker is accessed concurrently */
  private final Map<Class<?>, Set<Object>> m_classInvocationResults = Maps.newConcurrentMap();

  /** Test methods whose configuration methods have failed. */
  private final Map<ITestNGMethod, Set<Object>> m_methodInvocationResults = Maps.newConcurrentMap();
  private IConfiguration m_configuration;

  /** Predicate to filter methods */
  private static final Predicate<ITestNGMethod, IClass> CAN_RUN_FROM_CLASS = new CanRunFromClassPredicate();
  /** Predicate to filter methods */
  private static final Predicate<ITestNGMethod, IClass> SAME_CLASS = new SameClassNamePredicate();

  private void setClassInvocationFailure(Class<?> clazz, Object instance) {
    synchronized(m_classInvocationResults){
       Set<Object> instances = m_classInvocationResults.get( clazz );
       if (instances == null) {
         instances = Sets.newHashSet();
         m_classInvocationResults.put(clazz, instances);
       }
       instances.add(instance);
    }
  }

  private void setMethodInvocationFailure(ITestNGMethod method, Object instance) {
    Set<Object> instances = m_methodInvocationResults.get(method);
    if (instances == null) {
      instances = Sets.newHashSet();
      m_methodInvocationResults.put(method, instances);
    }
    instances.add(TestNgMethodUtils.getMethodInvocationToken(method, instance));
  }

  public Invoker(IConfiguration configuration,
                 ITestContext testContext,
                 ITestResultNotifier notifier,
                 SuiteRunState state,
                 boolean skipFailedInvocationCounts,
                 Collection<IInvokedMethodListener> invokedMethodListeners,
                 List<IClassListener> classListeners,
                 Collection<IDataProviderListener> dataProviderListeners) {
    m_configuration = configuration;
    m_testContext= testContext;
    m_suiteState= state;
    m_notifier= notifier;
    m_annotationFinder= configuration.getAnnotationFinder();
    m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    m_invokedMethodListeners = invokedMethodListeners;
    m_continueOnFailedConfiguration = testContext.getSuite().getXmlSuite().getConfigFailurePolicy() == XmlSuite.FailurePolicy.CONTINUE;
    m_classListeners = classListeners;
    m_dataproviderListeners = dataProviderListeners;
  }

  /**
   * Invoke configuration methods if they belong to the same TestClass passed
   * in parameter.. <p/>TODO: Calculate ahead of time which methods should be
   * invoked for each class. Might speed things up for users who invoke the
   * same test class with different parameters in the same suite run.
   *
   * If instance is non-null, the configuration will be run on it.  If it is null,
   * the configuration methods will be run on all the instances retrieved
   * from the ITestClass.
   */
  @Override
  public void invokeConfigurations(IClass testClass,
                                   ITestNGMethod[] allMethods,
                                   XmlSuite suite,
                                   Map<String, String> params,
                                   Object[] parameterValues,
                                   Object instance)
  {
    invokeConfigurations(testClass, null, allMethods, suite, params, parameterValues, instance,
        null);
  }

  private void invokeConfigurations(IClass testClass,
                                   ITestNGMethod currentTestMethod,
                                   ITestNGMethod[] allMethods,
                                   XmlSuite suite,
                                   Map<String, String> params,
                                   Object[] parameterValues,
                                   Object instance,
                                   ITestResult testMethodResult)
  {
    if(null == allMethods || allMethods.length == 0) {
      log(5, "No configuration methods found");

      return;
    }

    ITestNGMethod[] methods = TestNgMethodUtils.filterMethods(testClass, allMethods, SAME_CLASS);

    for(ITestNGMethod tm : methods) {
      if(null == testClass) {
        testClass= tm.getTestClass();
      }

      long time = System.currentTimeMillis();
      Object instanceTouse = instance != null ? instance : tm.getInstance();
      ITestResult testResult = new TestResult(testClass, instanceTouse, tm, null, time, time, m_testContext);

      IConfigurationAnnotation configurationAnnotation= null;
      try {
        Object inst = tm.getInstance();
        if (inst == null) {
          inst = instance;
        }
        Class<?> objectClass = inst.getClass();
        ConstructorOrMethod method = tm.getConstructorOrMethod();

        // Only run the configuration if
        // - the test is enabled and
        // - the Configuration method belongs to the same class or a parent
        configurationAnnotation = AnnotationHelper.findConfiguration(m_annotationFinder, method);
        boolean alwaysRun = MethodHelper.isAlwaysRun(configurationAnnotation);
        boolean canProcessMethod = MethodHelper.isEnabled(objectClass, m_annotationFinder) || alwaysRun;
        if (!canProcessMethod) {
          log(3, "Skipping " + Utils.detailedMethodName(tm, true)
                          + " because " + objectClass.getName() + " is not enabled");
          continue;
        }
        if (MethodHelper.isDisabled(configurationAnnotation)) {
          log(3, "Skipping " + Utils.detailedMethodName(tm, true)
                  + " because it is not enabled");
          continue;

        }
        if (!confInvocationPassed(tm, currentTestMethod, testClass, instance) && !alwaysRun) {
          log(3, "Skipping " + Utils.detailedMethodName(tm, true));
          handleConfigurationSkip(tm, testResult, configurationAnnotation, currentTestMethod, instance, suite);
          continue;
        }

        log(3, "Invoking " + Utils.detailedMethodName(tm, true));
        if (testMethodResult != null) {
          ((TestResult) testMethodResult).setMethod(currentTestMethod);
        }

        Object[] parameters = Parameters.createConfigurationParameters(tm.getConstructorOrMethod().getMethod(),
                params,
                parameterValues,
                currentTestMethod,
                m_annotationFinder,
                suite,
                m_testContext,
                testMethodResult);
        testResult.setParameters(parameters);

        runConfigurationListeners(testResult, true /* before */);

        Object newInstance = computeInstance(instance, inst, tm);
        invokeConfigurationMethod(newInstance, tm, parameters, testResult);

        runConfigurationListeners(testResult, false /* after */);
      }
      catch(Throwable ex) {
        handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, currentTestMethod, instance, suite);
      }
    } // for methods
  }

  private static Object computeInstance(Object instance, Object inst, ITestNGMethod tm) {
    if (instance == null || !tm.getConstructorOrMethod().getDeclaringClass().isAssignableFrom(instance.getClass())) {
      return inst;
    }
    return instance;
  }

  /**
   * Marks the current <code>TestResult</code> as skipped and invokes the listeners.
   */
  private void handleConfigurationSkip(ITestNGMethod tm,
                                       ITestResult testResult,
                                       IConfigurationAnnotation annotation,
                                       ITestNGMethod currentTestMethod,
                                       Object instance,
                                       XmlSuite suite) {
    recordConfigurationInvocationFailed(tm, testResult.getTestClass(), annotation, currentTestMethod, instance, suite);
    testResult.setStatus(ITestResult.SKIP);
    runConfigurationListeners(testResult, false /* after */);
  }

  private void handleConfigurationFailure(Throwable ite,
                                          ITestNGMethod tm,
                                          ITestResult testResult,
                                          IConfigurationAnnotation annotation,
                                          ITestNGMethod currentTestMethod,
                                          Object instance,
                                          XmlSuite suite)
  {
    Throwable cause= ite.getCause() != null ? ite.getCause() : ite;

    if(isSkipExceptionAndSkip(cause)) {
      testResult.setThrowable(cause);
      handleConfigurationSkip(tm, testResult, annotation, currentTestMethod, instance, suite);
      return;
    }
    Utils.log("", 3, "Failed to invoke configuration method "
        + tm.getQualifiedName() + ":" + cause.getMessage());
    handleException(cause, tm, testResult, 1);
    runConfigurationListeners(testResult, false /* after */);

    //
    // If in TestNG mode, need to take a look at the annotation to figure out
    // what kind of @Configuration method we're dealing with
    //
    if (null != annotation) {
      recordConfigurationInvocationFailed(tm, testResult.getTestClass(), annotation, currentTestMethod, instance, suite);
    }
  }

  /**
   * Record internally the failure of a Configuration, so that we can determine
   * later if @Test should be skipped.
   */
  private void recordConfigurationInvocationFailed(ITestNGMethod tm,
                                                   IClass testClass,
                                                   IConfigurationAnnotation annotation,
                                                   ITestNGMethod currentTestMethod,
                                                   Object instance,
                                                   XmlSuite suite) {
    // If beforeTestClass or afterTestClass failed, mark either the config method's
    // entire class as failed, or the class under tests as failed, depending on
    // the configuration failure policy
    if (annotation.getBeforeTestClass() || annotation.getAfterTestClass()) {
      // tm is the configuration method, and currentTestMethod is null for BeforeClass
      // methods, so we need testClass
      if (m_continueOnFailedConfiguration) {
        setClassInvocationFailure(testClass.getRealClass(), instance);
      } else {
        setClassInvocationFailure(tm.getRealClass(), instance);
      }
    }

    // If before/afterTestMethod failed, mark either the config method's entire
    // class as failed, or just the current test method as failed, depending on
    // the configuration failure policy
    else if (annotation.getBeforeTestMethod() || annotation.getAfterTestMethod()) {
      if (m_continueOnFailedConfiguration) {
        setMethodInvocationFailure(currentTestMethod, instance);
      } else {
        setClassInvocationFailure(tm.getRealClass(), instance);
      }
    }

    // If beforeSuite or afterSuite failed, mark *all* the classes as failed
    // for configurations.  At this point, the entire Suite is screwed
    else if (annotation.getBeforeSuite() || annotation.getAfterSuite()) {
      m_suiteState.failed();
    }

    // beforeTest or afterTest:  mark all the classes in the same
    // <test> stanza as failed for configuration
    else if (annotation.getBeforeTest() || annotation.getAfterTest()) {
      setClassInvocationFailure(tm.getRealClass(), instance);
      XmlClass[] classes = ClassHelper.findClassesInSameTest(tm.getRealClass(), suite);
      for(XmlClass xmlClass : classes) {
        setClassInvocationFailure(xmlClass.getSupportClass(), instance);
      }
    }
    String[] beforeGroups = annotation.getBeforeGroups();
    for (String group : beforeGroups) {
      m_beforegroupsFailures.put(group, Boolean.FALSE);
    }
  }

  /**
   * @return true if this class or a parent class failed to initialize.
   */
  private boolean classConfigurationFailed(Class<?> cls) {
    for (Class<?> c : m_classInvocationResults.keySet()) {
      if (c == cls || c.isAssignableFrom(cls)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if this class has successfully run all its @Configuration
   * method or false if at least one of these methods failed.
   */
  private boolean confInvocationPassed(ITestNGMethod method, ITestNGMethod currentTestMethod,
      IClass testClass, Object instance) {
    boolean result = true;

    Class<?> cls = testClass.getRealClass();

    if(m_suiteState.isFailed()) {
      result = false;
    } else {
      boolean hasConfigurationFailures = classConfigurationFailed(cls);
      if (hasConfigurationFailures) {
        if (! m_continueOnFailedConfiguration) {
          result = false;
        } else {
          Set<Object> set = getInvocationResults(testClass);
          result = !set.contains(instance);
        }
        return result;
      }
      // if method is BeforeClass, currentTestMethod will be null
      if (m_continueOnFailedConfiguration && hasConfigFailure(currentTestMethod)) {
        Object key = TestNgMethodUtils.getMethodInvocationToken(currentTestMethod, instance);
        result = !m_methodInvocationResults.get(currentTestMethod).contains(key);
      }
      else if (! m_continueOnFailedConfiguration) {
        for (Class<?> clazz : m_classInvocationResults.keySet()) {
          if (clazz.isAssignableFrom(cls)) {
            result = false;
            break;
          }
        }
      }
    }

    // check if there are failed @BeforeGroups
    String[] groups = method.getGroups();
    for (String group : groups) {
      if (m_beforegroupsFailures.containsKey(group)) {
        result = false;
        break;
      }
    }
    return result;
  }

  private boolean hasConfigFailure(ITestNGMethod currentTestMethod) {
    return currentTestMethod != null && m_methodInvocationResults.containsKey(currentTestMethod);
  }

  private Set<Object> getInvocationResults(IClass testClass) {
    Class<?> cls = testClass.getRealClass();
    Set<Object> set = null;
    //We need to continuously search till either our Set is not null (or) till we reached
    //Object class because it is very much possible that the test method is residing in a child class
    //and maybe the parent method has configuration methods which may have had a failure
    //So lets walk up the inheritance tree until either we find failures or till we
    //reached the Object class.
    while (!cls.equals(Object.class)) {
      set = m_classInvocationResults.get(cls);
      if (set != null) {
        break;
      }
      cls = cls.getSuperclass();
    }
    if (set == null) {
      //This should never happen because we have walked up all the way till Object class
      //and yet found no failures, but our logic indicates that there was a failure somewhere up the
      //inheritance order. We don't know what to do at this point.
      throw new IllegalStateException("No failure logs for " + testClass.getRealClass());
    }
    return set;
  }

  /**
   * Effectively invokes a configuration method on all passed in instances.
   */
  //TODO: Change this method to be more like invokeMethod() so that we can handle calls to {@code IInvokedMethodListener} better.
  private void invokeConfigurationMethod(Object targetInstance,
                                         ITestNGMethod tm,
                                         Object[] params,
                                         ITestResult testResult)
          throws InvocationTargetException, IllegalAccessException {
    // Mark this method with the current thread id
    tm.setId(ThreadUtil.currentThreadInfo());

    InvokedMethod invokedMethod = new InvokedMethod(targetInstance, tm, System.currentTimeMillis(), testResult);

    runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, testResult);
    m_notifier.addInvokedMethod(invokedMethod);
    try {
      Reporter.setCurrentTestResult(testResult);
      ConstructorOrMethod method = tm.getConstructorOrMethod();

      IConfigurable configurableInstance = computeConfigurableInstance(method, targetInstance);
      if (RuntimeBehavior.isDryRun()) {
        testResult.setStatus(ITestResult.SUCCESS);
        return;
      }
      if (configurableInstance != null) {
        MethodInvocationHelper.invokeConfigurable(targetInstance, params,
                configurableInstance, method.getMethod(), testResult);
      } else {
        MethodInvocationHelper.invokeMethodConsideringTimeout(tm, method, targetInstance, params, testResult);
      }
    } catch (InvocationTargetException | IllegalAccessException ex) {
      throwConfigurationFailure(testResult, ex);
      throw ex;
    } catch (Throwable ex) {
      throwConfigurationFailure(testResult, ex);
      throw new TestNGException(ex);
    } finally {
      testResult.setEndMillis(System.currentTimeMillis());
      Reporter.setCurrentTestResult(testResult);
      runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
      Reporter.setCurrentTestResult(null);
    }
  }

  private IConfigurable computeConfigurableInstance(ConstructorOrMethod method, Object targetInstance) {
    return IConfigurable.class.isAssignableFrom(method.getDeclaringClass()) ?
            (IConfigurable) targetInstance : m_configuration.getConfigurable();
  }

  private void throwConfigurationFailure(ITestResult testResult, Throwable ex) {
    testResult.setStatus(ITestResult.FAILURE);
    testResult.setThrowable(ex.getCause() == null ? ex : ex.getCause());
  }

  private void runInvokedMethodListeners(InvokedMethodListenerMethod listenerMethod,
                                         IInvokedMethod invokedMethod,
                                         ITestResult testResult) {
    if (noListenersPresent()) {
      return;
    }

    InvokedMethodListenerInvoker invoker = new InvokedMethodListenerInvoker(listenerMethod, testResult, m_testContext);
    for (IInvokedMethodListener currentListener : m_invokedMethodListeners) {
      invoker.invokeListener(currentListener, invokedMethod);
    }
  }

  private boolean noListenersPresent() {
    return (m_invokedMethodListeners == null) || (m_invokedMethodListeners.isEmpty());
  }

  // pass both paramValues and paramIndex to be thread safe in case parallel=true + dataprovider.
  private ITestResult invokeMethod(Object instance,
                                   ITestNGMethod tm,
                                   Object[] parameterValues,
                                   int parametersIndex,
                                   XmlSuite suite,
                                   Map<String, String> params,
                                   ITestClass testClass,
                                   ITestNGMethod[] beforeMethods,
                                   ITestNGMethod[] afterMethods,
                                   ConfigurationGroupMethods groupMethods,
                                   FailureContext failureContext) {
    TestResult testResult = new TestResult();

    invokeBeforeGroupsConfigurations(tm, groupMethods, suite, params, instance);

    //
    // Invoke beforeMethods only if
    // - firstTimeOnly is not set
    // - firstTimeOnly is set, and we are reaching at the first invocationCount
    //
    ITestNGMethod[] setupConfigMethods = TestNgMethodUtils.filterFirstTimeRunnableSetupConfigurationMethods(tm, beforeMethods);
    invokeConfigurations(testClass, tm, setupConfigMethods, suite, params, parameterValues, instance, testResult);

    InvokedMethod invokedMethod = new InvokedMethod(instance, tm, System.currentTimeMillis(), testResult);

    if (!confInvocationPassed(tm, tm, testClass, instance)) {
      Throwable exception = ExceptionUtils.getExceptionDetails(m_testContext, instance);
      ITestResult result = registerSkippedTestResult(tm, instance, System.currentTimeMillis(), exception);
      m_notifier.addSkippedTest(tm, result);
      tm.incrementCurrentInvocationCount();
      testResult.setMethod(tm);
      runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, testResult);
      runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
      ITestNGMethod[] teardownConfigMethods = TestNgMethodUtils.filterLastTimeRunnableTeardownConfigurationMethods(tm, afterMethods);
      invokeConfigurations(testClass, tm, teardownConfigMethods, suite, params, parameterValues, instance, testResult);
      invokeAfterGroupsConfigurations(tm, groupMethods, suite, params, instance);

      return result;
    }

    //
    // Create the ExtraOutput for this method
    //
    try {
      testResult.init(testClass, instance, tm, null, System.currentTimeMillis(), 0, m_testContext);
      testResult.setParameters(parameterValues);
      testResult.setParameterIndex(parametersIndex);
      testResult.setHost(m_testContext.getHost());
      testResult.setStatus(ITestResult.STARTED);

      Reporter.setCurrentTestResult(testResult);

      // Fix from ansgarkonermann
      // invokedMethod is used in the finally, which can be invoked if
      // any of the test listeners throws an exception, therefore,
      // invokedMethod must have a value before we get here
      if (!m_suiteState.isFailed()) {
        runTestListeners(testResult);
      }

      log(3, "Invoking " + tm.getQualifiedName());
      runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, testResult);

      m_notifier.addInvokedMethod(invokedMethod);

      Method thisMethod = tm.getConstructorOrMethod().getMethod();

      if (RuntimeBehavior.isDryRun()) {
        setTestStatus(testResult, ITestResult.SUCCESS);
        return testResult;
      }

      // If this method is a IHookable, invoke its run() method
      IHookable hookableInstance = IHookable.class.isAssignableFrom(tm.getRealClass()) ? (IHookable) instance : m_configuration.getHookable();

      if (MethodHelper.calculateTimeOut(tm) <= 0) {
        if (hookableInstance != null) {
          MethodInvocationHelper.invokeHookable(instance,
              parameterValues, hookableInstance, thisMethod, testResult);
        } else {
          // Not a IHookable, invoke directly
          MethodInvocationHelper.invokeMethod(thisMethod, instance,
              parameterValues);
        }
        setTestStatus(testResult, ITestResult.SUCCESS);
      } else {
        // Method with a timeout
        MethodInvocationHelper.invokeWithTimeout(tm, instance, parameterValues, testResult, hookableInstance);
      }
    }
    catch(InvocationTargetException ite) {
      testResult.setThrowable(ite.getCause());
      setTestStatus(testResult, ITestResult.FAILURE);
    }
    catch(ThreadExecutionException tee) { // wrapper for TestNGRuntimeException
      Throwable cause= tee.getCause();
      if(TestNGRuntimeException.class.equals(cause.getClass())) {
        testResult.setThrowable(cause.getCause());
      }
      else {
        testResult.setThrowable(cause);
      }
      setTestStatus(testResult, ITestResult.FAILURE);
    }
    catch(Throwable thr) { // covers the non-wrapper exceptions
      testResult.setThrowable(thr);
      setTestStatus(testResult, ITestResult.FAILURE);
    }
    finally {
      // Set end time ASAP
      testResult.setEndMillis(System.currentTimeMillis());
      ExpectedExceptionsHolder expectedExceptionClasses
          = new ExpectedExceptionsHolder(m_annotationFinder, tm, new RegexpExpectedExceptionsHolder(m_annotationFinder, tm));
      StatusHolder holder = considerExceptions(tm, testResult, expectedExceptionClasses, failureContext);
      int statusBeforeListenerInvocation = testResult.getStatus();
      runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
      boolean wasResultUnaltered = statusBeforeListenerInvocation == testResult.getStatus();
      handleInvocationResults(tm, testResult, failureContext, holder, wasResultUnaltered);

      // If this method has a data provider and just failed, memorize the number
      // at which it failed.
      // Note: we're not exactly testing that this method has a data provider, just
      // that it has parameters, so might have to revisit this if bugs get reported
      // for the case where this method has parameters that don't come from a data
      // provider
      if (testResult.getThrowable() != null && parameterValues.length > 0) {
        tm.addFailedInvocationNumber(parametersIndex);
      }

      //
      // Increment the invocation count for this method
      //
      tm.incrementCurrentInvocationCount();

      runTestListeners(testResult);

      collectResults(tm, testResult);

      //
      // Invoke afterMethods only if
      // - lastTimeOnly is not set
      // - lastTimeOnly is set, and we are reaching the last invocationCount
      //
      ITestNGMethod[] tearDownConfigMethods = TestNgMethodUtils.filterLastTimeRunnableTeardownConfigurationMethods(tm, afterMethods);
      invokeConfigurations(testClass, tm, tearDownConfigMethods, suite, params, parameterValues, instance, testResult);

      //
      // Invoke afterGroups configurations
      //
      invokeAfterGroupsConfigurations(tm, groupMethods, suite, params, instance);

      // Reset the test result last. If we do this too early, Reporter.log()
      // invocations from listeners will be discarded
      Reporter.setCurrentTestResult(null);
    }

    return testResult;
  }

  private static void setTestStatus(ITestResult result, int status) {
    // set the test to success as long as the testResult hasn't been changed by the user via
    // Reporter.getCurrentTestResult
    if (result.getStatus() == ITestResult.STARTED) {
      result.setStatus(status);
    }
  }

  private void collectResults(ITestNGMethod testMethod, ITestResult result) {
      // Collect the results
      int status = result.getStatus();
      if(ITestResult.SUCCESS == status) {
        m_notifier.addPassedTest(testMethod, result);
      }
      else if(ITestResult.SKIP == status) {
        m_notifier.addSkippedTest(testMethod, result);
      }
      else if(ITestResult.FAILURE == status) {
        m_notifier.addFailedTest(testMethod, result);
      }
      else if(ITestResult.SUCCESS_PERCENTAGE_FAILURE == status) {
        m_notifier.addFailedButWithinSuccessPercentageTest(testMethod, result);
      }
      else {
        assert false : "UNKNOWN STATUS:" + status;
      }
  }

  /**
   * invokeTestMethods() eventually converge here to invoke a single @Test method.
   * <p/>
   * This method is responsible for actually invoking the method. It decides if the invocation
   * must be done:
   * <ul>
   * <li>through an <code>IHookable</code></li>
   * <li>directly (through reflection)</li>
   * <li>in a separate thread (in case it needs to timeout)
   * </ul>
   *
   * <p/>
   * This method is also responsible for invoking @BeforeGroup, @BeforeMethod, @AfterMethod, @AfterGroup
   * if it is the case for the passed in @Test method.
   */
  ITestResult invokeTestMethod(Object instance,
                               ITestNGMethod tm,
                               Object[] parameterValues,
                               int parametersIndex,
                               XmlSuite suite,
                               Map<String, String> params,
                               ITestClass testClass,
                               ITestNGMethod[] beforeMethods,
                               ITestNGMethod[] afterMethods,
                               ConfigurationGroupMethods groupMethods,
                               FailureContext failureContext)
  {
    // Mark this method with the current thread id
    tm.setId(ThreadUtil.currentThreadInfo());

    return invokeMethod(instance, tm, parameterValues, parametersIndex, suite, params,
                                      testClass, beforeMethods, afterMethods, groupMethods,
                                      failureContext);
  }

  /**
   * Filter all the beforeGroups methods and invoke only those that apply
   * to the current test method
   */
  private void invokeBeforeGroupsConfigurations(ITestNGMethod currentTestMethod,
                                                ConfigurationGroupMethods groupMethods,
                                                XmlSuite suite,
                                                Map<String, String> params,
                                                Object instance)
  {
    List<ITestNGMethod> filteredMethods = Lists.newArrayList();
    String[] groups = currentTestMethod.getGroups();
    Map<String, List<ITestNGMethod>> beforeGroupMap = groupMethods.getBeforeGroupsMap();

    for (String group : groups) {
      List<ITestNGMethod> methods = beforeGroupMap.get(group);
      if (methods != null) {
        filteredMethods.addAll(methods);
      }
    }

    ITestNGMethod[] beforeMethodsArray = filteredMethods.toArray(new ITestNGMethod[filteredMethods.size()]);
    //
    // Invoke the right groups methods
    //
    if (beforeMethodsArray.length > 0) {
      // don't pass the IClass or the instance as the method may be external
      // the invocation must be similar to @BeforeTest/@BeforeSuite
      invokeConfigurations(null, beforeMethodsArray, suite, params,
            /* no parameter values */ null, instance);
    }

    //
    // Remove them so they don't get run again
    //
    groupMethods.removeBeforeGroups(groups);
  }

  private void invokeAfterGroupsConfigurations(ITestNGMethod currentTestMethod,
                                               ConfigurationGroupMethods groupMethods,
                                               XmlSuite suite,
                                               Map<String, String> params,
                                               Object instance)
  {
    // Skip this if the current method doesn't belong to any group
    // (only a method that belongs to a group can trigger the invocation
    // of afterGroups methods)
    if (currentTestMethod.getGroups().length == 0) {
      return;
    }

    // See if the currentMethod is the last method in any of the groups
    // it belongs to
    Map<String, String> filteredGroups = Maps.newHashMap();
    String[] groups = currentTestMethod.getGroups();
    for (String group : groups) {
      if (groupMethods.isLastMethodForGroup(group, currentTestMethod)) {
        filteredGroups.put(group, group);
      }
    }

    if (filteredGroups.isEmpty()) {
      return;
    }

    // The list of afterMethods to run
    Map<ITestNGMethod, ITestNGMethod> afterMethods = Maps.newHashMap();

    // Now filteredGroups contains all the groups for which we need to run the afterGroups
    // method.  Find all the methods that correspond to these groups and invoke them.
    Map<String, List<ITestNGMethod>> map = groupMethods.getAfterGroupsMap();
    for (String g : filteredGroups.values()) {
      List<ITestNGMethod> methods = map.get(g);
      // Note:  should put them in a map if we want to make sure the same afterGroups
      // doesn't get run twice
      if (methods != null) {
        for (ITestNGMethod m : methods) {
          afterMethods.put(m, m);
        }
      }
    }

    // Got our afterMethods, invoke them
    ITestNGMethod[] afterMethodsArray = afterMethods.keySet().toArray(new ITestNGMethod[afterMethods.size()]);
    // don't pass the IClass or the instance as the method may be external
    // the invocation must be similar to @BeforeTest/@BeforeSuite
    invokeConfigurations(null, afterMethodsArray, suite, params,
          /* no parameter values */ null, instance);

    // Remove the groups so they don't get run again
    groupMethods.removeAfterGroups(filteredGroups.keySet());
    }

  int retryFailed(Object instance,
                           ITestNGMethod tm,
                           XmlSuite suite,
                           ITestClass testClass,
                           ITestNGMethod[] beforeMethods,
                           ITestNGMethod[] afterMethods,
                           ConfigurationGroupMethods groupMethods,
                           List<ITestResult> result,
                           int failureCount,
                           ITestContext testContext,
                           Map<String, String> parameters,
                           int parametersIndex) {
    FailureContext failure = new FailureContext();
    failure.count = failureCount;
    do {
      failure.instances = Lists.newArrayList ();
      Map<String, String> allParameters = Maps.newHashMap();
      //TODO: This recreates all the parameters every time when we only need
      //one specific set. Should optimize it by only recreating the set needed.
      ParameterHandler handler = new ParameterHandler(m_annotationFinder, m_dataproviderListeners);

      ParameterBag bag = handler.createParameters(tm, parameters, allParameters, testContext);
      Object[] parameterValues = Parameters.getParametersFromIndex(bag.parameterHolder.parameters, parametersIndex);

      result.add(invokeMethod(instance, tm, parameterValues, parametersIndex, suite,
          allParameters, testClass, beforeMethods, afterMethods, groupMethods, failure));
    }
    while (!failure.instances.isEmpty());
    return failure.count;
  }

  /**
   * Invoke all the test methods. Note the plural: the method passed in
   * parameter might be invoked several times if the test class it belongs
   * to has more than one instance (i.e., if an @Factory method has been
   * declared somewhere that returns several instances of this TestClass).
   * If no @Factory method was specified, testMethod will only be invoked
   * once.
   * <p/>
   * Note that this method also takes care of invoking the beforeTestMethod
   * and afterTestMethod, if any.
   *
   * Note (alex): this method can be refactored to use a SingleTestMethodWorker that
   * directly invokes
   * {@link #invokeTestMethod(Object, ITestNGMethod, Object[], int, XmlSuite, Map, ITestClass, ITestNGMethod[], ITestNGMethod[], ConfigurationGroupMethods, FailureContext)}
   * and this would simplify the implementation (see how DataTestMethodWorker is used)
   */
  @Override
  public List<ITestResult> invokeTestMethods(ITestNGMethod testMethod,
                                             XmlSuite suite,
                                             Map<String, String> testParameters,
                                             ConfigurationGroupMethods groupMethods,
                                             Object instance,
                                             ITestContext testContext)
  {
    // Potential bug here if the test method was declared on a parent class
    assert null != testMethod.getTestClass()
        : "COULDN'T FIND TESTCLASS FOR " + testMethod.getRealClass();

    if (!MethodHelper.isEnabled(testMethod.getConstructorOrMethod().getMethod(), m_annotationFinder)) {
      // return if the method is not enabled. No need to do any more calculations
      return Collections.emptyList();
    }

    // By the time this testMethod to be invoked,
    // all dependencies should be already run or we need to skip this method,
    // so invocation count should not affect dependencies check
    String okToProceed = checkDependencies(testMethod, testContext.getAllTestMethods());

    if (okToProceed != null) {
      //
      // Not okToProceed. Test is being skipped
      //
      ITestResult result = registerSkippedTestResult(testMethod, null, System.currentTimeMillis(),
          new Throwable(okToProceed));
      m_notifier.addSkippedTest(testMethod, result);
      return Collections.singletonList(result);
    }


    Map<String, String> parameters =
        testMethod.findMethodParameters(testContext.getCurrentXmlTest());

    // For invocationCount > 1 and threadPoolSize > 1 run this method in its own pool thread.
    if (testMethod.getInvocationCount() > 1 && testMethod.getThreadPoolSize() > 1) {
      return invokePooledTestMethods(testMethod, suite, parameters, groupMethods, testContext);
    }

    long timeOutInvocationCount = testMethod.getInvocationTimeOut();
    //FIXME: Is this correct?
    boolean onlyOne = testMethod.getThreadPoolSize() > 1 ||
      timeOutInvocationCount > 0;

    int invocationCount = onlyOne ? 1 : testMethod.getInvocationCount();

    ExpectedExceptionsHolder expectedExceptionHolder =
        new ExpectedExceptionsHolder(m_annotationFinder, testMethod,
                                     new RegexpExpectedExceptionsHolder(m_annotationFinder, testMethod));
    ITestClass testClass= testMethod.getTestClass();
    List<ITestResult> result = Lists.newArrayList();
    FailureContext failure = new FailureContext();
    ITestNGMethod[] beforeMethods = TestNgMethodUtils.filterBeforeTestMethods(testClass, CAN_RUN_FROM_CLASS);
    ITestNGMethod[] afterMethods = TestNgMethodUtils.filterAfterTestMethods(testClass, CAN_RUN_FROM_CLASS);
    while (invocationCount-- > 0) {
      long start = System.currentTimeMillis();

      Map<String, String> allParameterNames = Maps.newHashMap();
      ParameterHandler handler = new ParameterHandler(m_annotationFinder, m_dataproviderListeners);

      ParameterBag bag = handler.createParameters(testMethod, parameters, allParameterNames, testContext, instance);

      if (bag.hasErrors()) {
        ITestResult tr = bag.errorResult;
        Throwable throwable = tr.getThrowable();
        if (throwable instanceof TestNGException) {
          tr.setStatus(ITestResult.FAILURE);
          m_notifier.addFailedTest(testMethod, tr);
        } else {
          tr.setStatus(ITestResult.SKIP);
          m_notifier.addSkippedTest(testMethod, tr);
        }
        runTestListeners(tr);
        result.add(tr);
        continue;
      }

      Iterator<Object[]> allParameterValues = bag.parameterHolder.parameters;
      int parametersIndex = 0;

      try {

        if (bag.runInParallel()) {
          List<TestMethodWithDataProviderMethodWorker> workers = Lists.newArrayList();
          while (allParameterValues.hasNext()) {
            Object[] next = allParameterValues.next();
            if (next == null) {
              // skipped value
              parametersIndex++;
              continue;
            }
            Object[] parameterValues = Parameters.injectParameters(next,
                    testMethod.getConstructorOrMethod().getMethod(), testContext);

            TestMethodWithDataProviderMethodWorker w =
                    new TestMethodWithDataProviderMethodWorker(this,
                            testMethod, parametersIndex,
                            parameterValues, instance, suite, parameters, testClass,
                            beforeMethods, afterMethods, groupMethods,
                            expectedExceptionHolder, testContext, m_skipFailedInvocationCounts,
                            invocationCount, failure.count, m_notifier);
            workers.add(w);
            // testng387: increment the param index in the bag.
            parametersIndex++;
          }
          PoolService<List<ITestResult>> ps = new PoolService<>(suite.getDataProviderThreadCount());
          List<List<ITestResult>> r = ps.submitTasksAndWait(workers);
          for (List<ITestResult> l2 : r) {
            result.addAll(l2);
          }

        } else {
          while (allParameterValues.hasNext()) {
            Object[] next = allParameterValues.next();
            if (next == null) {
              // skipped value
              parametersIndex++;
              continue;
            }
            Object[] parameterValues = Parameters.injectParameters(next,
                    testMethod.getConstructorOrMethod().getMethod(), testContext);

            List<ITestResult> tmpResults = Lists.newArrayList();
            int tmpResultsIndex = -1;
            try {
              tmpResults.add(invokeTestMethod(instance,
                      testMethod,
                      parameterValues,
                      parametersIndex,
                      suite,
                      parameters,
                      testClass,
                      beforeMethods,
                      afterMethods,
                      groupMethods, failure));
              tmpResultsIndex++;
            } finally {
              boolean lastSucces = false;
              if (tmpResultsIndex >= 0) {
                lastSucces = (tmpResults.get(tmpResultsIndex).getStatus() == ITestResult.SUCCESS);
              }
              if (failure.instances.isEmpty() || lastSucces) {
                result.addAll(tmpResults);
              } else {
                List<ITestResult> retryResults = Lists.newArrayList();

                failure.count = retryFailed(
                        instance, testMethod, suite, testClass, beforeMethods,
                        afterMethods, groupMethods, retryResults,
                        failure.count,
                        testContext, parameters, parametersIndex);
                result.addAll(retryResults);
              }

              // If we have a failure, skip all the
              // other invocationCounts
              if (failure.count > 0
                      && (m_skipFailedInvocationCounts
                      || testMethod.skipFailedInvocations())) {
                while (invocationCount-- > 0) {
                  result.add(registerSkippedTestResult(testMethod, instance, System.currentTimeMillis(), null));
                }
              }
            }// end finally
            parametersIndex++;
          }
        }
      } catch (Throwable cause) {
        ITestResult r =
                new TestResult(testMethod.getTestClass(),
                        instance,
                        testMethod,
                        cause,
                        start,
                        System.currentTimeMillis(),
                        m_testContext);
        r.setStatus(TestResult.FAILURE);
        result.add(r);
        runTestListeners(r);
        m_notifier.addFailedTest(testMethod, r);
      } // catch
    }

    return result;

  }

  private ITestResult registerSkippedTestResult(ITestNGMethod testMethod, Object instance,
      long start, Throwable throwable) {
    ITestResult result =
      new TestResult(testMethod.getTestClass(),
        instance,
        testMethod,
        throwable,
        start,
        System.currentTimeMillis(),
        m_testContext);
    result.setStatus(TestResult.SKIP);
    Reporter.setCurrentTestResult(result);
    runTestListeners(result);

    return result;
  }

  /**
   * Invokes a method that has a specified threadPoolSize.
   */
  private List<ITestResult> invokePooledTestMethods(ITestNGMethod testMethod,
                                                    XmlSuite suite,
                                                    Map<String, String> parameters,
                                                    ConfigurationGroupMethods groupMethods,
                                                    ITestContext testContext)
  {
    //
    // Create the workers
    //
    List<IWorker<ITestNGMethod>> workers = Lists.newArrayList();

    // Create one worker per invocationCount
    for (int i = 0; i < testMethod.getInvocationCount(); i++) {
      // we use clones for reporting purposes
      ITestNGMethod clonedMethod= testMethod.clone();
      clonedMethod.setInvocationCount(1);
      clonedMethod.setThreadPoolSize(1);

      MethodInstance mi = new MethodInstance(clonedMethod);
      workers.add(new SingleTestMethodWorker(this,
          mi,
          suite,
          parameters,
          testContext,
          m_classListeners));
    }

    return runWorkers(testMethod, workers, testMethod.getThreadPoolSize(), groupMethods, suite,
                      parameters);
  }

  static class FailureContext {
    int count = 0;
    List<Object> instances = Lists.newArrayList();
  }

  private static class StatusHolder {
    boolean handled = false;
    int status;
  }

  private StatusHolder considerExceptions(ITestNGMethod tm, ITestResult testresult, ExpectedExceptionsHolder
          exceptionsHolder, FailureContext failure) {
    StatusHolder holder = new StatusHolder();
    holder.status = testresult.getStatus();
    holder.handled = false;

    Throwable ite = testresult.getThrowable();
    if (holder.status == ITestResult.FAILURE && ite != null) {

      //  Invocation caused an exception, see if the method was annotated with @ExpectedException
      if (exceptionsHolder != null) {
        if (exceptionsHolder.isExpectedException(ite)) {
          testresult.setStatus(ITestResult.SUCCESS);
          holder.status = ITestResult.SUCCESS;
        } else {
          if (isSkipExceptionAndSkip(ite)) {
            holder.status = ITestResult.SKIP;
          } else {
            testresult.setThrowable(exceptionsHolder.wrongException(ite));
            holder.status = ITestResult.FAILURE;
          }
        }
      } else {
        handleException(ite, tm, testresult, failure.count++);
        holder.handled = true;
        holder.status = testresult.getStatus();
      }
    } else if (holder.status != ITestResult.SKIP && exceptionsHolder != null) {
      TestException exception = exceptionsHolder.noException(tm);
      if (exception != null) {
        testresult.setThrowable(exception);
        holder.status = ITestResult.FAILURE;
      }
    }
    return holder;
  }

  private void handleInvocationResults(ITestNGMethod testMethod,
                               ITestResult testResult,
                               FailureContext failure,
                               StatusHolder holder,
                               boolean wasResultUnaltered) {
    //
    // Go through all the results and create a TestResult for each of them
    //
    List<ITestResult> resultsToRetry = Lists.newArrayList();

    Throwable ite = testResult.getThrowable();
    int status = computeTestStatusComparingTestResultAndStatusHolder(testResult, holder, wasResultUnaltered);
    boolean handled = holder.handled;

    IRetryAnalyzer retryAnalyzer = testMethod.getRetryAnalyzer();
    boolean willRetry = retryAnalyzer != null && status == ITestResult.FAILURE && failure.instances != null && retryAnalyzer.retry(testResult);

    if (willRetry) {
      resultsToRetry.add(testResult);
      Object instance = testResult.getInstance();
      if (!failure.instances.contains(instance)) {
        failure.instances.add(instance);
      }
      testResult.setStatus(ITestResult.SKIP);
    } else {
      testResult.setStatus(status);
      if (status == ITestResult.FAILURE && !handled) {
        handleException(ite, testMethod, testResult, failure.count++);
      }
    }
  }

  private static int computeTestStatusComparingTestResultAndStatusHolder(ITestResult testResult, StatusHolder holder,
                                                                         boolean wasResultUnaltered) {
    if (wasResultUnaltered) {
      return holder.status;
    }
    return testResult.getStatus();
  }

  private boolean isSkipExceptionAndSkip(Throwable ite) {
    return SkipException.class.isAssignableFrom(ite.getClass()) && ((SkipException) ite).isSkip();
  }

  /**
   * To reduce thread contention and also to correctly handle thread-confinement
   * this method invokes the @BeforeGroups and @AfterGroups corresponding to the current @Test method.
   */
  private List<ITestResult> runWorkers(ITestNGMethod testMethod,
      List<IWorker<ITestNGMethod>> workers,
      int threadPoolSize,
      ConfigurationGroupMethods groupMethods,
      XmlSuite suite,
      Map<String, String> parameters)
  {
    // Invoke @BeforeGroups on the original method (reduce thread contention,
    // and also solve thread confinement)
    ITestClass testClass= testMethod.getTestClass();
    Object[] instances = testClass.getInstances(true);
    for(Object instance: instances) {
      invokeBeforeGroupsConfigurations(testMethod, groupMethods, suite, parameters, instance);
    }


    long maxTimeOut= -1; // 10 seconds

    for(IWorker<ITestNGMethod> tmw : workers) {
      long mt= tmw.getTimeOut();
      if(mt > maxTimeOut) {
        maxTimeOut= mt;
      }
    }

    ThreadUtil.execute("methods", workers, threadPoolSize, maxTimeOut, true);

    //
    // Collect all the TestResults
    //
    List<ITestResult> result = Lists.newArrayList();
    for (IWorker<ITestNGMethod> tmw : workers) {
      if (tmw instanceof TestMethodWorker) {
        result.addAll(((TestMethodWorker)tmw).getTestResults());
      }
    }

    for(Object instance: instances) {
      invokeAfterGroupsConfigurations(testMethod, groupMethods, suite, parameters, instance);
    }

    return result;
  }

  /**
   * Checks to see of the test method has certain dependencies that prevents
   * TestNG from executing it
   * @param testMethod test method being checked for
   * @return error message or null if dependencies have been run successfully
   */
  private String checkDependencies(ITestNGMethod testMethod,
                                   ITestNGMethod[] allTestMethods)
  {
    // If this method is marked alwaysRun, no need to check for its dependencies
    if (testMethod.isAlwaysRun()) {
      return null;
    }

    // Any missing group?
    if (testMethod.getMissingGroup() != null
        && !testMethod.ignoreMissingDependencies()) {
      return "Method " + testMethod + " depends on nonexistent group \"" + testMethod.getMissingGroup() + "\"";
    }

    // If this method depends on groups, collect all the methods that
    // belong to these groups and make sure they have been run successfully
    String[] groups = testMethod.getGroupsDependedUpon();
    if (null != groups && groups.length > 0) {
      // Get all the methods that belong to the group depended upon
      for (String element : groups) {
        ITestNGMethod[] methods =
            MethodGroupsHelper.findMethodsThatBelongToGroup(testMethod,
                m_testContext.getAllTestMethods(),
                element);
        if (methods.length == 0 && !testMethod.ignoreMissingDependencies()) {
          // Group is missing
          return "Method " + testMethod + " depends on nonexistent group \"" + element + "\"";
        }
        if (!haveBeenRunSuccessfully(testMethod, methods)) {
          return "Method " + testMethod +
              " depends on not successfully finished methods in group \"" + element + "\"";
        }
      }
    } // depends on groups

    // If this method depends on other methods, make sure all these other
    // methods have been run successfully
    if (TestNgMethodUtils.cannotRunMethodIndependently(testMethod)) {
      ITestNGMethod[] methods = MethodHelper.findDependedUponMethods(testMethod, allTestMethods);

      if (!haveBeenRunSuccessfully(testMethod, methods)) {
        return "Method " + testMethod + " depends on not successfully finished methods";
      }
    }

    return null;
  }

  /**
   * @return the test results that apply to one of the instances of the testMethod.
   */
  private Set<ITestResult> keepSameInstances(ITestNGMethod method, Set<ITestResult> results) {
    Set<ITestResult> result = Sets.newHashSet();
    for (ITestResult r : results) {
      Object o = method.getInstance();
        // Keep this instance if 1) It's on a different class or 2) It's on the same class
        // and on the same instance
        Object instance = r.getInstance() != null
            ? r.getInstance() : r.getMethod().getInstance();
        if (r.getTestClass() != method.getTestClass() || instance == o) result.add(r);
    }
    return result;
  }

  /**
   * @return true if all the methods have been run successfully
   */
  private boolean haveBeenRunSuccessfully(ITestNGMethod testMethod, ITestNGMethod[] methods) {
    // Make sure the method has been run successfully
    for (ITestNGMethod method : methods) {
      Set<ITestResult> results = keepSameInstances(testMethod, m_notifier.getPassedTests(method));
      Set<ITestResult> failedAndSkippedMethods = Sets.newHashSet();
      failedAndSkippedMethods.addAll(m_notifier.getFailedTests(method));
      failedAndSkippedMethods.addAll(m_notifier.getSkippedTests(method));
      Set<ITestResult> failedresults = keepSameInstances(testMethod, failedAndSkippedMethods);

      // If failed results were returned on the same instance, then these tests didn't pass
      if (failedresults != null && failedresults.size() > 0) {
        return false;
      }

      for (ITestResult result : results) {
        if(!result.isSuccess()) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * An exception was thrown by the test, determine if this method
   * should be marked as a failure or as failure_but_within_successPercentage
   */
  private void handleException(Throwable throwable,
                               ITestNGMethod testMethod,
                               ITestResult testResult,
                               int failureCount) {
    if (throwable != null && testResult.getThrowable() == null) {
      testResult.setThrowable(throwable);
    }
    int successPercentage= testMethod.getSuccessPercentage();
    int invocationCount= testMethod.getInvocationCount();
    float numberOfTestsThatCanFail= ((100 - successPercentage) * invocationCount) / 100f;

    if(failureCount < numberOfTestsThatCanFail) {
      testResult.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
    }
    else {
      testResult.setStatus(ITestResult.FAILURE);
    }

  }

  interface Predicate<K, T> {
    boolean isTrue(K k, T v);
  }

  static class CanRunFromClassPredicate implements Predicate <ITestNGMethod, IClass> {
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

  private void runConfigurationListeners(ITestResult tr, boolean before) {
    if (before) {
      TestListenerHelper.runPreConfigurationListeners(tr, m_notifier.getConfigurationListeners());
    } else {
      TestListenerHelper.runPostConfigurationListeners(tr, m_notifier.getConfigurationListeners());
    }
  }

  void runTestListeners(ITestResult tr) {
    TestListenerHelper.runTestListeners(tr, m_notifier.getTestListeners());
  }

  private void log(int level, String s) {
    Utils.log("Invoker " + Thread.currentThread().hashCode(), level, s);
  }

}
