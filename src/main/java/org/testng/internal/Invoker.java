package org.testng.internal;

import static org.testng.internal.invokers.InvokedMethodListenerMethod.AFTER_INVOCATION;
import static org.testng.internal.invokers.InvokedMethodListenerMethod.BEFORE_INVOCATION;

import org.testng.IClass;
import org.testng.IConfigurable;
import org.testng.IConfigurationListener;
import org.testng.IConfigurationListener2;
import org.testng.IHookable;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.SuiteRunState;
import org.testng.TestException;
import org.testng.TestNGException;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.NoInjection;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.InvokeMethodRunnable.TestNGRuntimeException;
import org.testng.internal.ParameterHolder.ParameterOrigin;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.Sets;
import org.testng.internal.invokers.InvokedMethodListenerInvoker;
import org.testng.internal.invokers.InvokedMethodListenerMethod;
import org.testng.internal.thread.ThreadExecutionException;
import org.testng.internal.thread.ThreadUtil;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
  private final List<IInvokedMethodListener> m_invokedMethodListeners;
  private final boolean m_continueOnFailedConfiguration;

  /** Group failures must be synced as the Invoker is accessed concurrently */
  private Map<String, Boolean> m_beforegroupsFailures = Maps.newHashtable();

  /** Class failures must be synced as the Invoker is accessed concurrently */
  private Map<Class<?>, Set<Object>> m_classInvocationResults = Maps.newHashtable();

  /** Test methods whose configuration methods have failed. */
  private Map<ITestNGMethod, Set<Object>> m_methodInvocationResults = Maps.newHashtable();
  private IConfiguration m_configuration;

  /** Predicate to filter methods */
  private static Predicate<ITestNGMethod, IClass> CAN_RUN_FROM_CLASS = new CanRunFromClassPredicate();
  /** Predicate to filter methods */
  private static final Predicate<ITestNGMethod, IClass> SAME_CLASS = new SameClassNamePredicate();

  private void setClassInvocationFailure(Class<?> clazz, Object instance) {
    Set<Object> instances = m_classInvocationResults.get( clazz );
    if (instances == null) {
      instances = Sets.newHashSet();
      m_classInvocationResults.put(clazz, instances);
    }
    instances.add(instance);
  }

  private void setMethodInvocationFailure(ITestNGMethod method, Object instance) {
    Set<Object> instances = m_methodInvocationResults.get(method);
    if (instances == null) {
      instances = Sets.newHashSet();
      m_methodInvocationResults.put(method, instances);
    }
    instances.add(getMethodInvocationToken(method, instance));
  }

  public Invoker(IConfiguration configuration,
                 ITestContext testContext,
                 ITestResultNotifier notifier,
                 SuiteRunState state,
                 boolean skipFailedInvocationCounts,
                 List<IInvokedMethodListener> invokedMethodListeners) {
    m_configuration = configuration;
    m_testContext= testContext;
    m_suiteState= state;
    m_notifier= notifier;
    m_annotationFinder= configuration.getAnnotationFinder();
    m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    m_invokedMethodListeners = invokedMethodListeners;
    m_continueOnFailedConfiguration = XmlSuite.CONTINUE.equals(testContext.getSuite().getXmlSuite().getConfigFailurePolicy());
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
    if(null == allMethods) {
      log(5, "No configuration methods found");

      return;
    }

    ITestNGMethod[] methods= filterMethods(testClass, allMethods, SAME_CLASS);

    for(ITestNGMethod tm : methods) {
      if(null == testClass) {
        testClass= tm.getTestClass();
      }

      ITestResult testResult= new TestResult(testClass,
                                             instance,
                                             tm,
                                             null,
                                             System.currentTimeMillis(),
                                             System.currentTimeMillis(),
                                             m_testContext);

      IConfigurationAnnotation configurationAnnotation= null;
      try {
        Object[] instances= tm.getInstances();
        if (instances == null || instances.length == 0) {
          instances = new Object[] { instance };
        }
        Class<?> objectClass= instances[0].getClass();
        Method method= tm.getMethod();

        // Only run the configuration if
        // - the test is enabled and
        // - the Configuration method belongs to the same class or a parent
        if(MethodHelper.isEnabled(objectClass, m_annotationFinder)) {
          configurationAnnotation = AnnotationHelper.findConfiguration(m_annotationFinder, method);

          if (MethodHelper.isEnabled(configurationAnnotation)) {
            boolean isClassConfiguration = isClassConfiguration(configurationAnnotation);
            boolean isSuiteConfiguration = isSuiteConfiguration(configurationAnnotation);
            boolean alwaysRun= isAlwaysRun(configurationAnnotation);

            if (!confInvocationPassed(tm, currentTestMethod, testClass, instance) && !alwaysRun) {
              handleConfigurationSkip(tm, testResult, configurationAnnotation, currentTestMethod, instance, suite);
              continue;
            }

            log(3, "Invoking " + Utils.detailedMethodName(tm, true));

            Object[] parameters = Parameters.createConfigurationParameters(tm.getMethod(),
                params,
                parameterValues,
                currentTestMethod,
                m_annotationFinder,
                suite,
                m_testContext,
                testMethodResult);
            testResult.setParameters(parameters);

            Object[] newInstances= (null != instance) ? new Object[] { instance } : instances;

            runConfigurationListeners(testResult, true /* before */);

            invokeConfigurationMethod(newInstances, tm,
              parameters, isClassConfiguration, isSuiteConfiguration, testResult);

            // TODO: probably we should trigger the event for each instance???
            testResult.setEndMillis(System.currentTimeMillis());
            runConfigurationListeners(testResult, false /* after */);
          }
          else {
            log(3,
                "Skipping "
                + Utils.detailedMethodName(tm, true)
                + " because it is not enabled");
          }
        } // if is enabled
        else {
          log(3,
              "Skipping "
              + Utils.detailedMethodName(tm, true)
              + " because "
              + objectClass.getName()
              + " is not enabled");
        }
      }
      catch(InvocationTargetException ex) {
        handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, currentTestMethod, instance, suite);
      }
      catch(TestNGException ex) {
        // Don't wrap TestNGExceptions, it could be a missing parameter on a
        // @Configuration method
        handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, currentTestMethod, instance, suite);
      }
      catch(Throwable ex) { // covers the non-wrapper exceptions
        handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, currentTestMethod, instance, suite);
      }
    } // for methods
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

  /**
   * Is the current <code>IConfiguration</code> a class-level method.
   */
  private  boolean isClassConfiguration(IConfigurationAnnotation configurationAnnotation) {
    if (null == configurationAnnotation) {
      return false;
    }

    boolean before = configurationAnnotation.getBeforeTestClass();
    boolean after = configurationAnnotation.getAfterTestClass();

    return before || after;
  }

  /**
   * Is the current <code>IConfiguration</code> a suite level method.
   */
  private  boolean isSuiteConfiguration(IConfigurationAnnotation configurationAnnotation) {
    if (null == configurationAnnotation) {
      return false;
    }

    boolean before = configurationAnnotation.getBeforeSuite();
    boolean after = configurationAnnotation.getAfterSuite();

    return before || after;
  }

  /**
   * Is the <code>IConfiguration</code> marked as alwaysRun.
   */
  private boolean isAlwaysRun(IConfigurationAnnotation configurationAnnotation) {
    if(null == configurationAnnotation) {
      return false;
    }

    boolean alwaysRun= false;
    if ((configurationAnnotation.getAfterSuite()
        || configurationAnnotation.getAfterTest()
        || configurationAnnotation.getAfterTestClass()
        || configurationAnnotation.getAfterTestMethod())
        && configurationAnnotation.getAlwaysRun())
    {
        alwaysRun= true;
    }

    return alwaysRun;
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

    if(SkipException.class.isAssignableFrom(cause.getClass())) {
      SkipException skipEx= (SkipException) cause;
      if(skipEx.isSkip()) {
        testResult.setThrowable(skipEx);
        handleConfigurationSkip(tm, testResult, annotation, currentTestMethod, instance, suite);
        return;
      }
    }
    Utils.log("", 3, "Failed to invoke configuration method "
        + tm.getRealClass().getName() + "." + tm.getMethodName() + ":" + cause.getMessage());
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
   * @return All the classes that belong to the same <test> tag as @param cls
   */
  private XmlClass[] findClassesInSameTest(Class<?> cls, XmlSuite suite) {
    Map<String, XmlClass> vResult= Maps.newHashMap();
    String className= cls.getName();
    for(XmlTest test : suite.getTests()) {
      for(XmlClass testClass : test.getXmlClasses()) {
        if(testClass.getName().equals(className)) {

          // Found it, add all the classes in this test in the result
          for(XmlClass thisClass : test.getXmlClasses()) {
            vResult.put(thisClass.getName(), thisClass);
          }
          // Note:  we need to iterate through the entire suite since the same
          // class might appear in several <test> tags
        }
      }
    }

    XmlClass[] result= vResult.values().toArray(new XmlClass[vResult.size()]);

    return result;
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
      XmlClass[] classes= findClassesInSameTest(tm.getRealClass(), suite);
      for(XmlClass xmlClass : classes) {
        setClassInvocationFailure(xmlClass.getSupportClass(), instance);
      }
    }
    String[] beforeGroups= annotation.getBeforeGroups();
    if(null != beforeGroups && beforeGroups.length > 0) {
      for(String group: beforeGroups) {
        m_beforegroupsFailures.put(group, Boolean.FALSE);
      }
    }
  }

  /**
   * @return true if this class or a parent class failed to initialize.
   */
  private boolean classConfigurationFailed(Class<?> cls) {
    for (Class<?> c : m_classInvocationResults.keySet()) {
      if (c == cls || cls.isAssignableFrom(c)) {
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
    boolean result= true;

    // If continuing on config failure, check invocation results for the class
    // under test, otherwise use the method's declaring class
    Class<?> cls = m_continueOnFailedConfiguration ?
            testClass.getRealClass() : method.getMethod().getDeclaringClass();

    if(m_suiteState.isFailed()) {
      result= false;
    }
    else {
      if (classConfigurationFailed(cls)) {
        if (! m_continueOnFailedConfiguration) {
          result = !classConfigurationFailed(cls);
        } else {
          result = !m_classInvocationResults.get(cls).contains(instance);
        }
      }
      // if method is BeforeClass, currentTestMethod will be null
      else if (m_continueOnFailedConfiguration &&
              currentTestMethod != null &&
              m_methodInvocationResults.containsKey(currentTestMethod)) {
        result = !m_methodInvocationResults.get(currentTestMethod).contains(getMethodInvocationToken(currentTestMethod, instance));
      }
      else if (! m_continueOnFailedConfiguration) {
        for(Class<?> clazz: m_classInvocationResults.keySet()) {
//          if (clazz == cls) {
          if(clazz.isAssignableFrom(cls)) {
            result= false;
            break;
          }
        }
      }
    }

    // check if there are failed @BeforeGroups
    String[] groups= method.getGroups();
    if(null != groups && groups.length > 0) {
      for(String group: groups) {
        if(m_beforegroupsFailures.containsKey(group)) {
          result= false;
          break;
        }
      }
    }
    return result;
  }

   // Creates a token for tracking a unique invocation of a method on an instance.
   // Is used when configFailurePolicy=continue.
  private Object getMethodInvocationToken(ITestNGMethod method, Object instance) {
    return String.format("%s+%d", instance.toString(), method.getCurrentInvocationCount());
  }

  /**
   * Effectively invokes a configuration method on all passed in instances.
   * TODO: Should change this method to be more like invokeMethod() so that we can
   * handle calls to {@code IInvokedMethodListener} better.
   *
   * @param instances the instances to invoke the configuration method on
   * @param tm the configuration method
   * @param params the parameters needed for method invocation
   * @param isClass flag if the configuration method is a class level method // FIXME: this looks like a missusage
   * @param testResult
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  private void invokeConfigurationMethod(Object[] instances,
                                         ITestNGMethod tm,
                                         Object[] params,
                                         boolean isClass,
                                         boolean isSuite,
                                         ITestResult testResult)
    throws InvocationTargetException, IllegalAccessException
  {
    // Mark this method with the current thread id
    tm.setId(ThreadUtil.currentThreadInfo());

    // Only a @BeforeMethod/@AfterMethod needs to be run before each instance, all the other
    // configuration methods only need to be run once
    List<Object> actualInstances = Lists.newArrayList();
    if (tm.isBeforeMethodConfiguration() || tm.isAfterMethodConfiguration()) {
      actualInstances.addAll(Arrays.asList(instances));
    } else {
      actualInstances.add(instances[0]);
    }
    for(Object targetInstance : actualInstances) {
      InvokedMethod invokedMethod= new InvokedMethod(targetInstance,
                                          tm,
                                          params,
                                          false, /* isTest */
                                          isClass, /* ??? */
                                          System.currentTimeMillis(),
                                          testResult);

      runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, testResult);
      m_notifier.addInvokedMethod(invokedMethod);
      try {
        Reporter.setCurrentTestResult(testResult);
        Method method = tm.getMethod();

        //
        // If this method is a IHookable, invoke its run() method
        //
        IConfigurable configurableInstance =
          IConfigurable.class.isAssignableFrom(tm.getMethod().getDeclaringClass()) ?
          (IConfigurable) targetInstance : m_configuration.getConfigurable();
        if (configurableInstance != null) {
          //
          // If this method is a IConfigurable, invoke its run() method
          //
          MethodInvocationHelper.invokeConfigurable(targetInstance, params, configurableInstance, method,
              testResult);
        }
        else {
          //
          // Not a IConfigurable, invoke directly
          //
          if (MethodHelper.calculateTimeOut(tm) <= 0) {
            MethodInvocationHelper.invokeMethod(method, targetInstance, params);
          }
          else {
            MethodInvocationHelper.invokeWithTimeout(tm, targetInstance, params, testResult);
            if (!testResult.isSuccess()) {
              // A time out happened
              throwConfigurationFailure(testResult, testResult.getThrowable());
              throw testResult.getThrowable();
            }
          }
        }
        // Only run the method once if it's @BeforeSuite or @AfterSuite
        if (isSuite) {
          break;
        }
      }
      catch (InvocationTargetException ex) {
       throwConfigurationFailure(testResult, ex);
       throw ex;
      }
      catch (IllegalAccessException ex) {
        throwConfigurationFailure(testResult, ex);
        throw ex;
      }
      catch (NoSuchMethodException ex) {
        throwConfigurationFailure(testResult, ex);
        throw new TestNGException(ex);
      }
      catch (Throwable ex) {
        throwConfigurationFailure(testResult, ex);
        throw new TestNGException(ex);
      }
      finally {
        Reporter.setCurrentTestResult(testResult);
        runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
        Reporter.setCurrentTestResult(null);
      }
    }
  }

  private void throwConfigurationFailure(ITestResult testResult, Throwable ex)
  {
    testResult.setStatus(ITestResult.FAILURE);;
    testResult.setThrowable(ex.getCause() == null ? ex : ex.getCause());
  }

  private void runInvokedMethodListeners(InvokedMethodListenerMethod listenerMethod, IInvokedMethod invokedMethod,
      ITestResult testResult)
  {
    if ( noListenersPresent() ) {
      return;
    }

    InvokedMethodListenerInvoker invoker = new InvokedMethodListenerInvoker(listenerMethod, testResult, m_testContext);
    for (IInvokedMethodListener currentListener : m_invokedMethodListeners) {
      invoker.invokeListener(currentListener, invokedMethod);
    }
  }

  private boolean noListenersPresent() {
    return (m_invokedMethodListeners == null) || (m_invokedMethodListeners.size() == 0);
  }

  // pass both paramValues and paramIndex to be thread safe in case parallel=true + dataprovider.
  private ITestResult invokeMethod(Object[] instances,
                                   int instanceIndex,
                                   final ITestNGMethod tm,
                                   Object[] parameterValues,
                                   int parametersIndex,
                                   XmlSuite suite,
                                   Map<String, String> params,
                                   ITestClass testClass,
                                   ITestNGMethod[] beforeMethods,
                                   ITestNGMethod[] afterMethods,
                                   ConfigurationGroupMethods groupMethods) {
    TestResult testResult = new TestResult();

    //
    // Invoke beforeGroups configurations
    //
    Object instance = instances[instanceIndex];
    invokeBeforeGroupsConfigurations(testClass, tm, groupMethods, suite, params,
        instance);

    //
    // Invoke beforeMethods only if
    // - firstTimeOnly is not set
    // - firstTimeOnly is set, and we are reaching at the first invocationCount
    //
    invokeConfigurations(testClass, tm,
      filterConfigurationMethods(tm, beforeMethods, true /* beforeMethods */),
      suite, params, parameterValues,
      instance, testResult);

    //
    // Create the ExtraOutput for this method
    //
    InvokedMethod invokedMethod = null;
    try {
      testResult.init(testClass, instance,
                                 tm,
                                 null,
                                 System.currentTimeMillis(),
                                 0,
                                 m_testContext);
      testResult.setParameters(parameterValues);
      testResult.setHost(m_testContext.getHost());
      testResult.setStatus(ITestResult.STARTED);

      invokedMethod= new InvokedMethod(instance,
          tm,
          parameterValues,
          true /* isTest */,
          false /* isConfiguration */,
          System.currentTimeMillis(),
          testResult);

      // Fix from ansgarkonermann
      // invokedMethod is used in the finally, which can be invoked if
      // any of the test listeners throws an exception, therefore,
      // invokedMethod must have a value before we get here
      runTestListeners(testResult);

      runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, testResult);

      m_notifier.addInvokedMethod(invokedMethod);

      Method thisMethod= tm.getMethod();

      if(confInvocationPassed(tm, tm, testClass, instance)) {
        log(3, "Invoking " + thisMethod.getDeclaringClass().getName() + "." +
            thisMethod.getName());

        // If no timeOut, just invoke the method
        if (MethodHelper.calculateTimeOut(tm) <= 0) {
          Reporter.setCurrentTestResult(testResult);
          //
          // If this method is a IHookable, invoke its run() method
          //
          IHookable hookableInstance =
            IHookable.class.isAssignableFrom(thisMethod.getDeclaringClass()) ?
            (IHookable) instance : m_configuration.getHookable();
          if (hookableInstance != null) {
            MethodInvocationHelper.invokeHookable(instance,
                parameterValues, hookableInstance, thisMethod, testResult);
          }
          //
          // Not a IHookable, invoke directly
          //
          else {
            MethodInvocationHelper.invokeMethod(thisMethod, instance,
                parameterValues);
          }
          testResult.setStatus(ITestResult.SUCCESS);
        }
        else {
          //
          // Method with a timeout
          //
          Reporter.setCurrentTestResult(testResult);
          MethodInvocationHelper.invokeWithTimeout(tm, instance, parameterValues, testResult);
        }
      }
      else {
        testResult.setStatus(ITestResult.SKIP);
      }
    }
    catch(InvocationTargetException ite) {
      testResult.setThrowable(ite.getCause());
      testResult.setStatus(ITestResult.FAILURE);
    }
    catch(ThreadExecutionException tee) { // wrapper for TestNGRuntimeException
      Throwable cause= tee.getCause();
      if(TestNGRuntimeException.class.equals(cause.getClass())) {
        testResult.setThrowable(cause.getCause());
      }
      else {
        testResult.setThrowable(cause);
      }
      testResult.setStatus(ITestResult.FAILURE);
    }
    catch(Throwable thr) { // covers the non-wrapper exceptions
      testResult.setThrowable(thr);
      testResult.setStatus(ITestResult.FAILURE);
    }
    finally {
      ExpectedExceptionsHolder expectedExceptionClasses
          = MethodHelper.findExpectedExceptions(m_annotationFinder, tm.getMethod());
      List<ITestResult> results = Lists.newArrayList();
      results.add(testResult);
      handleInvocationResults(tm, results, null, 0, expectedExceptionClasses, false,
          false /* collect results */);

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

      if (testResult != null) {
        testResult.setEndMillis(System.currentTimeMillis());
      }

      // Run invokedMethodListeners after updating TestResult
      runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
      runTestListeners(testResult);
      collectResults(tm, results, testResult);

      //
      // Invoke afterMethods only if
      // - lastTimeOnly is not set
      // - lastTimeOnly is set, and we are reaching the last invocationCount
      //
      invokeConfigurations(testClass, tm,
          filterConfigurationMethods(tm, afterMethods, false /* beforeMethods */),
          suite, params, parameterValues,
          instance,
          testResult);

      //
      // Invoke afterGroups configurations
      //
      invokeAfterGroupsConfigurations(testClass, tm, groupMethods, suite,
          params, instance);

      // Reset the test result last. If we do this too early, Reporter.log()
      // invocations from listeners will be discarded
      Reporter.setCurrentTestResult(null);
    }

    return testResult;
  }

  private void collectResults(ITestNGMethod testMethod, List<ITestResult> results, TestResult testResult) {
    for (int i = 0; i < results.size(); i++) {
      // Collect the results
      int status = results.get(i).getStatus();
      if(ITestResult.SUCCESS == status) {
        m_notifier.addPassedTest(testMethod, testResult);
      }
      else if(ITestResult.SKIP == status) {
        m_notifier.addSkippedTest(testMethod, testResult);
      }
      else if(ITestResult.FAILURE == status) {
        m_notifier.addFailedTest(testMethod, testResult);
      }
      else if(ITestResult.SUCCESS_PERCENTAGE_FAILURE == status) {
        m_notifier.addFailedButWithinSuccessPercentageTest(testMethod, testResult);
      }
      else {
        assert false : "UNKNOWN STATUS:" + status;
      }
    }
  }

  /**
   * The array of methods contains @BeforeMethods if isBefore if true, @AfterMethods
   * otherwise.  This function removes all the methods that should not be run at this
   * point because they are either firstTimeOnly or lastTimeOnly and we haven't reached
   * the current invocationCount yet
   */
  private ITestNGMethod[] filterConfigurationMethods(ITestNGMethod tm,
      ITestNGMethod[] methods, boolean isBefore)
  {
    List<ITestNGMethod> result = Lists.newArrayList();
    for (ITestNGMethod m : methods) {
      ConfigurationMethod cm = (ConfigurationMethod) m;
      if (isBefore) {
        if (! cm.isFirstTimeOnly() ||
            (cm.isFirstTimeOnly() && tm.getCurrentInvocationCount() == 0))
        {
          result.add(m);
        }
      }
      else {
        int current = tm.getCurrentInvocationCount();
        boolean isLast = false;
        // If we have parameters, set the boolean if we are about to run
        // the last invocation
        if (tm.getParameterInvocationCount() > 0) {
          isLast = current == tm.getParameterInvocationCount();
        }
        // If we have invocationCount > 1, set the boolean if we are about to
        // run the last invocation
        else if (tm.getInvocationCount() > 1) {
          isLast = current == tm.getInvocationCount();
        }
        if (! cm.isLastTimeOnly() || (cm.isLastTimeOnly() && isLast)) {
          result.add(m);
        }
      }
    }

    return result.toArray(new ITestNGMethod[result.size()]);
  }

  /**
   * {@link #invokeTestMethods()} eventually converge here to invoke a single @Test method.
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
   * This method is also reponsible for invoking @BeforeGroup, @BeforeMethod, @AfterMethod, @AfterGroup
   * if it is the case for the passed in @Test method.
   */
  protected List<ITestResult> invokeTestMethod(Object[] instances,
                                             final ITestNGMethod tm,
                                             Object[] parameterValues,
                                             int parametersIndex,
                                             XmlSuite suite,
                                             Map<String, String> params,
                                             ITestClass testClass,
                                             ITestNGMethod[] beforeMethods,
                                             ITestNGMethod[] afterMethods,
                                             ConfigurationGroupMethods groupMethods)
  {
    List<ITestResult> results = Lists.newArrayList();

    // Mark this method with the current thread id
    tm.setId(ThreadUtil.currentThreadInfo());

    for(int i= 0; i < instances.length; i++) {
      results.add(invokeMethod(instances, i, tm, parameterValues, parametersIndex, suite, params,
          testClass, beforeMethods, afterMethods, groupMethods));
    }

    return results;
  }

  /**
   * Filter all the beforeGroups methods and invoke only those that apply
   * to the current test method
   */
  private void invokeBeforeGroupsConfigurations(ITestClass testClass,
                                                ITestNGMethod currentTestMethod,
                                                ConfigurationGroupMethods groupMethods,
                                                XmlSuite suite,
                                                Map<String, String> params,
                                                Object instance)
  {
    synchronized(groupMethods) {
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
      if(beforeMethodsArray.length > 0) {
        // don't pass the IClass or the instance as the method may be external
        // the invocation must be similar to @BeforeTest/@BeforeSuite
        invokeConfigurations(null, beforeMethodsArray, suite, params,
            null, /* no parameter values */
            null);
      }

      //
      // Remove them so they don't get run again
      //
      groupMethods.removeBeforeGroups(groups);
    }
  }

  private void invokeAfterGroupsConfigurations(ITestClass testClass,
                                               ITestNGMethod currentTestMethod,
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
    synchronized(groupMethods) {
      for (String group : groups) {
        if (groupMethods.isLastMethodForGroup(group, currentTestMethod)) {
          filteredGroups.put(group, group);
        }
      }

      if(filteredGroups.isEmpty()) {
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
          null, /* no parameter values */
          null);

      // Remove the groups so they don't get run again
      groupMethods.removeAfterGroups(filteredGroups.keySet());
    }
  }

  private Object[] getParametersFromIndex(Iterator<Object[]> parametersValues, int index) {
    while (parametersValues.hasNext()) {
      Object[] parameters = parametersValues.next();

      if (index == 0) {
        return parameters;
      }
      index--;
    }
    return null;
  }

  int retryFailed(Object[] instances,
                           int instanceIndex,
                           final ITestNGMethod tm,
                           XmlSuite suite,
                           ITestClass testClass,
                           ITestNGMethod[] beforeMethods,
                           ITestNGMethod[] afterMethods,
                           ConfigurationGroupMethods groupMethods,
                           List<ITestResult> result,
                           int failureCount,
                           ExpectedExceptionsHolder expectedExceptionHolder,
                           ITestContext testContext,
                           Map<String, String> parameters,
                           int parametersIndex) {
    List<Object> failedInstances;

    do {
      failedInstances = Lists.newArrayList();
      Map<String, String> allParameters = Maps.newHashMap();
      /**
       * TODO: This recreates all the parameters every time when we only need
       * one specific set. Should optimize it by only recreating the set needed.
       */
      ParameterBag bag = createParameters(tm, parameters,
          allParameters, null, suite, testContext, null /* fedInstance */, null /* testResult */);
      Object[] parameterValues =
          getParametersFromIndex(bag.parameterHolder.parameters, parametersIndex);

      result.add(invokeMethod(instances, instanceIndex, tm, parameterValues,parametersIndex, suite,
          allParameters, testClass, beforeMethods, afterMethods, groupMethods));
      failureCount = handleInvocationResults(tm, result, failedInstances,
          failureCount, expectedExceptionHolder, true, true /* collect results */);
    }
    while (!failedInstances.isEmpty());
    return failureCount;
  }

  private ParameterBag createParameters(ITestNGMethod testMethod,
                                        Map<String, String> parameters,
                                        Map<String, String> allParameterNames,
                                        Object[] parameterValues,
                                        XmlSuite suite,
                                        ITestContext testContext,
                                        Object fedInstance,
                                        ITestResult testResult)
  {
    Object instance;
    if (fedInstance != null) {
      instance = fedInstance;
    }
    else {
      instance = testMethod.getInstance();
    }

    ParameterBag bag= handleParameters(testMethod,
        instance, allParameterNames, parameters, parameterValues, suite, testContext, fedInstance,
        testResult);

    return bag;
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
   * {@link #invokeTestMethod(Object[], ITestNGMethod, Object[], XmlSuite, Map, ITestClass, ITestNGMethod[], ITestNGMethod[], ConfigurationGroupMethods)}
   * and this would simplify the implementation (see how DataTestMethodWorker is used)
   */
  @Override
  public List<ITestResult> invokeTestMethods(ITestNGMethod testMethod,
                                             ITestNGMethod[] allTestMethods,
                                             int testMethodIndex,
                                             XmlSuite suite,
                                             Map<String, String> testParameters,
                                             ConfigurationGroupMethods groupMethods,
                                             Object[] instances,
                                             ITestContext testContext)
  {
    // Potential bug here if the test method was declared on a parent class
    assert null != testMethod.getTestClass()
    : "COULDN'T FIND TESTCLASS FOR " + testMethod.getMethod().getDeclaringClass();

    List<ITestResult> result = Lists.newArrayList();

    if (!MethodHelper.isEnabled(testMethod.getMethod(), m_annotationFinder)) {
      /*
       * return if the method is not enabled. No need to do any more calculations
       */
      return result;
    }

    ITestClass testClass= testMethod.getTestClass();
    long start = System.currentTimeMillis();

    // For invocationCount > 1 and threadPoolSize > 1 the method will be invoked on a thread pool
    long timeOutInvocationCount = testMethod.getInvocationTimeOut();
    //FIXME: Is this correct?
    boolean onlyOne = testMethod.getThreadPoolSize() > 1 ||
      timeOutInvocationCount > 0;

    int invocationCount = onlyOne ? 1 : testMethod.getInvocationCount();
    int failureCount = 0;

    ExpectedExceptionsHolder expectedExceptionHolder =
        MethodHelper.findExpectedExceptions(m_annotationFinder, testMethod.getMethod());
    while(invocationCount-- > 0) {
      boolean okToProceed = checkDependencies(testMethod, allTestMethods);

      if (!okToProceed) {
        //
        // Not okToProceed. Test is being skipped
        //
        ITestResult testResult = new TestResult(testClass, null /* instance */,
                                               testMethod,
                                               null /* cause */,
                                               start,
                                               System.currentTimeMillis(),
                                               m_testContext);
        String missingGroup = testMethod.getMissingGroup();
        if (missingGroup != null) {
          testResult.setThrowable(new Throwable("Method " + testMethod
              + " depends on nonexistent group \"" + missingGroup + "\""));
        }

        testResult.setStatus(ITestResult.SKIP);
        result.add(testResult);
        m_notifier.addSkippedTest(testMethod, testResult);
        runTestListeners(testResult);
        return result;
      }

      //
      // If threadPoolSize specified, run this method in its own pool thread.
      //
      Map<String, String> parameters =
          testMethod.findMethodParameters(testContext.getCurrentXmlTest());
      if (testMethod.getThreadPoolSize() > 1 && testMethod.getInvocationCount() > 1) {
          return invokePooledTestMethods(testMethod, allTestMethods, suite,
              parameters, groupMethods, testContext);
      }
      //
      // No threads, regular invocation
      //
      else {
        ITestNGMethod[] beforeMethods = filterMethods(testClass, testClass.getBeforeTestMethods(),
            CAN_RUN_FROM_CLASS);
        ITestNGMethod[] afterMethods = filterMethods(testClass, testClass.getAfterTestMethods(),
            CAN_RUN_FROM_CLASS);

        Map<String, String> allParameterNames = Maps.newHashMap();
        ParameterBag bag = createParameters(testMethod,
            parameters, allParameterNames, null, suite, testContext, instances[0],
            null);

        if (bag.hasErrors()) {
          failureCount = handleInvocationResults(testMethod,
              bag.errorResults, null, failureCount, expectedExceptionHolder, true,
              true /* collect results */);
          ITestResult tr = registerSkippedTestResult(testMethod, instances[0], start,
              bag.errorResults.get(0).getThrowable());
          result.add(tr);
          continue;
        }

        Iterator<Object[]> allParameterValues = bag.parameterHolder.parameters;
        int parametersIndex = 0;

        try {
          List<TestMethodWithDataProviderMethodWorker> workers = Lists.newArrayList();

          if (bag.parameterHolder.origin == ParameterOrigin.ORIGIN_DATA_PROVIDER &&
              bag.parameterHolder.dataProviderHolder.annotation.isParallel()) {
            while (allParameterValues.hasNext()) {
              Object[] parameterValues = injectParameters(allParameterValues.next(),
                  testMethod.getMethod(), testContext, null /* test result */);
              TestMethodWithDataProviderMethodWorker w =
                new TestMethodWithDataProviderMethodWorker(this,
                    testMethod, parametersIndex,
                    parameterValues, instances, suite, parameters, testClass,
                    beforeMethods, afterMethods, groupMethods,
                    expectedExceptionHolder, testContext, m_skipFailedInvocationCounts,
                    invocationCount, failureCount, m_notifier);
              workers.add(w);
              // testng387: increment the param index in the bag.
              parametersIndex++;
            }
            PoolService<List<ITestResult>> ps =
                new PoolService<List<ITestResult>>(suite.getDataProviderThreadCount());
            List<List<ITestResult>> r = ps.submitTasksAndWait(workers);
            for (List<ITestResult> l2 : r) {
              result.addAll(l2);
            }

          } else {
            while (allParameterValues.hasNext()) {
              Object[] parameterValues = injectParameters(allParameterValues.next(),
                  testMethod.getMethod(), testContext, null /* test result */);

              List<ITestResult> tmpResults = Lists.newArrayList();

              try {
                tmpResults.addAll(invokeTestMethod(instances,
                                                   testMethod,
                                                   parameterValues,
                                                   parametersIndex,
                                                   suite,
                                                   parameters,
                                                   testClass,
                                                   beforeMethods,
                                                   afterMethods,
                                                   groupMethods));
              }
              finally {
                List<Object> failedInstances = Lists.newArrayList();

                failureCount = handleInvocationResults(testMethod, tmpResults,
                    failedInstances, failureCount, expectedExceptionHolder, true,
                    false /* don't collect results */);
                if (failedInstances.isEmpty()) {
                  result.addAll(tmpResults);
                } else {
                  for (int i = 0; i < failedInstances.size(); i++) {
                    List<ITestResult> retryResults = Lists.newArrayList();

                    failureCount =
                     retryFailed(failedInstances.toArray(),
                     i, testMethod, suite, testClass, beforeMethods,
                     afterMethods, groupMethods, retryResults,
                     failureCount, expectedExceptionHolder,
                     testContext, parameters, parametersIndex);
                  result.addAll(retryResults);
                  }
                }

                //
                // If we have a failure, skip all the
                // other invocationCounts
                //
                if (failureCount > 0
                      && (m_skipFailedInvocationCounts
                            || testMethod.skipFailedInvocations())) {
                  while (invocationCount-- > 0) {
                    result.add(registerSkippedTestResult(testMethod, instances[0], start, null));
                  }
                  break;
                }
              }// end finally
              parametersIndex++;
            }
          }
        }
        catch (Throwable cause) {
          ITestResult r =
              new TestResult(testMethod.getTestClass(),
                instances[0],
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
    }

    return result;

  } // invokeTestMethod

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
    runTestListeners(result);

    return result;
  }

  /**
   * Gets an array of parameter values returned by data provider or the ones that
   * are injected based on parameter type. The method also checks for {@code NoInjection}
   * annotation
   * @param parameterValues parameter values from a data provider
   * @param method method to be invoked
   * @param context test context
   * @param testResult test result
   * @return
   */
  private Object[] injectParameters(Object[] parameterValues, Method method,
      ITestContext context, ITestResult testResult)
    throws TestNGException {
    List<Object> vResult = Lists.newArrayList();
    int i = 0;
    int numValues = parameterValues.length;
    int numParams = method.getParameterTypes().length;

    if (numValues > numParams && ! method.isVarArgs()) {
      throw new TestNGException("The data provider is trying to pass " + numValues
          + " parameters but the method "
          + method.getDeclaringClass().getName() + "#" + method.getName()
          + " takes " + numParams);
    }

    // beyond this, numValues <= numParams
    for (Class<?> cls : method.getParameterTypes()) {
      Annotation[] annotations = method.getParameterAnnotations()[i];
      boolean noInjection = false;
      for (Annotation a : annotations) {
        if (a instanceof NoInjection) {
          noInjection = true;
          break;
        }
      }
      Object injected = Parameters.getInjectedParameter(cls, method, context, testResult);
      if (injected != null && ! noInjection) {
        vResult.add(injected);
      } else {
        try {
          if (method.isVarArgs()) vResult.add(parameterValues);
          else vResult.add(parameterValues[i++]);
        } catch (ArrayIndexOutOfBoundsException ex) {
          throw new TestNGException("The data provider is trying to pass " + numValues
              + " parameters but the method "
              + method.getDeclaringClass().getName() + "#" + method.getName()
              + " takes " + numParams
              + " and TestNG is unable in inject a suitable object", ex);
        }
      }
    }
    return vResult.toArray(new Object[vResult.size()]);
  }

  private ParameterBag handleParameters(ITestNGMethod testMethod,
      Object instance,
      Map<String, String> allParameterNames,
      Map<String, String> parameters,
      Object[] parameterValues,
      XmlSuite suite,
      ITestContext testContext,
      Object fedInstance,
      ITestResult testResult)
  {
    try {
      return new ParameterBag(
          Parameters.handleParameters(testMethod,
            allParameterNames,
            instance,
            new Parameters.MethodParameters(parameters,
                testMethod.findMethodParameters(testContext.getCurrentXmlTest()),
                parameterValues,
                testMethod.getMethod(), testContext, testResult),
            suite,
            m_annotationFinder,
            fedInstance),
          null /* TestResult */);
    }
//    catch(TestNGException ex) {
//      throw ex;
//    }
    catch(Throwable cause) {
      return new ParameterBag(null /* ParameterHolder */,
          new TestResult(
              testMethod.getTestClass(),
              instance,
              testMethod,
              cause,
              System.currentTimeMillis(),
              System.currentTimeMillis(),
              m_testContext));
    }
  }

  /**
   * Invokes a method that has a specified threadPoolSize.
   */
  private List<ITestResult> invokePooledTestMethods(ITestNGMethod testMethod,
                                                    ITestNGMethod[] allTestMethods,
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
          allTestMethods,
          testContext));
    }

    return runWorkers(testMethod, workers, testMethod.getThreadPoolSize(), groupMethods, suite, parameters);
  }

  /**
   * @param testMethod
   * @param result
   * @param failureCount
   * @param expectedExceptionsHolder
   * @return
   */
  int handleInvocationResults(ITestNGMethod testMethod,
                                      List<ITestResult> result,
                                      List<Object> failedInstances,
                                      int failureCount,
                                      ExpectedExceptionsHolder expectedExceptionsHolder,
                                      boolean triggerListeners,
                                      boolean collectResults)
  {
    //
    // Go through all the results and create a TestResult for each of them
    //
    List<ITestResult> resultsToRetry = Lists.newArrayList();

    for (int i = 0; i < result.size(); i++) {
      ITestResult testResult = result.get(i);
      Throwable ite= testResult.getThrowable();
      int status= testResult.getStatus();

      // Exception thrown?
      if (ite != null) {

        //  Invocation caused an exception, see if the method was annotated with @ExpectedException
        if (isExpectedException(ite, expectedExceptionsHolder)) {
          if (messageRegExpMatches(expectedExceptionsHolder.messageRegExp, ite)) {
            testResult.setStatus(ITestResult.SUCCESS);
            status= ITestResult.SUCCESS;
          }
          else {
            testResult.setThrowable(
                new TestException("The exception was thrown with the wrong message:" +
                    " expected \"" + expectedExceptionsHolder.messageRegExp + "\"" +
                    " but got \"" + ite.getMessage() + "\"", ite));
            status= ITestResult.FAILURE;
          }
        } else if (SkipException.class.isAssignableFrom(ite.getClass())){
          SkipException skipEx= (SkipException) ite;
          if(skipEx.isSkip()) {
            status = ITestResult.SKIP;
          }
          else {
            handleException(ite, testMethod, testResult, failureCount++);
            status = ITestResult.FAILURE;
          }
        } else if (ite != null && expectedExceptionsHolder != null) {
          testResult.setThrowable(
              new TestException("Expected exception "
                 + expectedExceptionsHolder.expectedClasses[0].getName()
                 + " but got " + ite, ite));
          status= ITestResult.FAILURE;
        } else {
          handleException(ite, testMethod, testResult, failureCount++);
          status= testResult.getStatus();
        }
      }

      // No exception thrown, make sure we weren't expecting one
      else if(status != ITestResult.SKIP && expectedExceptionsHolder != null) {
        Class<?>[] classes = expectedExceptionsHolder.expectedClasses;
        if (classes != null && classes.length > 0) {
          testResult.setThrowable(
              new TestException("Method " + testMethod + " should have thrown an exception of "
                  + expectedExceptionsHolder.expectedClasses[0]));
          status= ITestResult.FAILURE;
        }
      }

      testResult.setStatus(status);

      boolean retry = false;

      if (testResult.getStatus() == ITestResult.FAILURE) {
        IRetryAnalyzer retryAnalyzer = testMethod.getRetryAnalyzer();

        if (retryAnalyzer != null && failedInstances != null) {
          retry = retryAnalyzer.retry(testResult);
        }

        if (retry) {
          resultsToRetry.add(testResult);
          if (failedInstances != null) {
            failedInstances.add(testResult.getInstance());
          }
        }
      }
      if (collectResults) {
        // Collect the results
        if(ITestResult.SUCCESS == status) {
          m_notifier.addPassedTest(testMethod, testResult);
        }
        else if(ITestResult.SKIP == status) {
          m_notifier.addSkippedTest(testMethod, testResult);
        }
        else if(ITestResult.FAILURE == status) {
          m_notifier.addFailedTest(testMethod, testResult);
        }
        else if(ITestResult.SUCCESS_PERCENTAGE_FAILURE == status) {
          m_notifier.addFailedButWithinSuccessPercentageTest(testMethod, testResult);
        }
        else {
          assert false : "UNKNOWN STATUS:" + status;
        }
//        if (triggerListeners && status != ITestResult.SUCCESS) {
//          runTestListeners(testResult);
//        }
      }
    } // for results

    return removeResultsToRetryFromResult(resultsToRetry, result, failureCount);
  }

  /**
   *   message / regEx  .*      other
   *   null             true    false
   *   non-null         true    match
   */
  private boolean messageRegExpMatches(String messageRegExp, Throwable ite) {
    if (".*".equals(messageRegExp)) {
      return true;
    } else {
      if (ite.getMessage() == null) {
        return false;
      } else {
        return Pattern.matches(messageRegExp, ite.getMessage());
      }
    }
  }

  private int removeResultsToRetryFromResult(List<ITestResult> resultsToRetry,
      List<ITestResult> result, int failureCount) {
    if (resultsToRetry != null) {
      for (ITestResult res : resultsToRetry) {
        result.remove(res);
        failureCount--;
      }
    }
    return failureCount;
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
      invokeBeforeGroupsConfigurations(testClass, testMethod, groupMethods, suite, parameters, instance);
    }


    long maxTimeOut= -1; // 10 seconds

    for(IWorker<ITestNGMethod> tmw : workers) {
      long mt= tmw.getTimeOut();
      if(mt > maxTimeOut) {
        maxTimeOut= mt;
      }
    }

    ThreadUtil.execute(workers, threadPoolSize, maxTimeOut, true);

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
      invokeAfterGroupsConfigurations(testClass, testMethod, groupMethods, suite, parameters, instance);
    }

    return result;
  }

  /**
   * Checks to see of the test method has certain dependencies that prevents
   * TestNG from executing it
   * @param testMethod test method being checked for
   * @param testClass
   * @return dependencies have been run successfully
   */
  private boolean checkDependencies(ITestNGMethod testMethod,
      ITestNGMethod[] allTestMethods)
  {
    boolean result = true;

    // If this method is marked alwaysRun, no need to check for its dependencies
    if (testMethod.isAlwaysRun()) {
      return true;
    }

    // Any missing group?
    if (testMethod.getMissingGroup() != null
          && !testMethod.ignoreMissingDependencies()) {
      return false;
    }

    // If this method depends on groups, collect all the methods that
    // belong to these groups and make sure they have been run successfully
    if (dependsOnGroups(testMethod)) {
      String[] groupsDependedUpon = testMethod.getGroupsDependedUpon();

      // Get all the methods that belong to the group depended upon
      for (String element : groupsDependedUpon) {
        ITestNGMethod[] methods =
          MethodGroupsHelper.findMethodsThatBelongToGroup(testMethod,
              m_testContext.getAllTestMethods(),
              element);

        result = result && haveBeenRunSuccessfully(testMethod, methods);
      }
    } // depends on groups

    // If this method depends on other methods, make sure all these other
    // methods have been run successfully
    if (result && dependsOnMethods(testMethod)) {
      ITestNGMethod[] methods =
        MethodHelper.findDependedUponMethods(testMethod, allTestMethods);

      result = result && haveBeenRunSuccessfully(testMethod, methods);
    }

    return result;
  }

  /**
   * @return the test results that apply to one of the instances of the testMethod.
   */
  private Set<ITestResult> keepSameInstances(ITestNGMethod method, Set<ITestResult> results) {
    Set<ITestResult> result = Sets.newHashSet();
    for (ITestResult r : results) {
      for (Object o : method.getInstances()) {
        // Keep this instance if 1) It's on a different class or 2) It's on the same class
        // and on the same instance
        Object instance = r.getInstance() != null
            ? r.getInstance() : r.getMethod().getInstances()[0];
        if (r.getTestClass() != method.getTestClass() || instance == o) result.add(r);
      }
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

//  private boolean containsInstance(Set<ITestResult> failedresults, Object[] instances) {
//    for (ITestResult tr : failedresults) {
//      for (Object o : instances) {
//        if (o == tr.getInstance()) {
//          return true;
//        }
//      }
//    }
//    return false;
//  }

  /**
   * An exception was thrown by the test, determine if this method
   * should be marked as a failure or as failure_but_within_successPercentage
   */
  private void handleException(Throwable throwable,
                               ITestNGMethod testMethod,
                               ITestResult testResult,
                               int failureCount) {
    testResult.setThrowable(throwable);
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

  /**
   * @param ite The exception that was just thrown
   * @param expectedExceptions The list of expected exceptions for this
   * test method
   * @return true if the exception that was just thrown is part of the
   * expected exceptions
   */
  private boolean isExpectedException(Throwable ite, ExpectedExceptionsHolder exceptionHolder) {
    if (exceptionHolder == null) {
      return false;
    }

    // TestException is the wrapper exception that TestNG will be throwing when an exception was
    // expected but not thrown
    if (ite.getClass() == TestException.class) {
      return false;
    }

    Class<?>[] exceptions = exceptionHolder.expectedClasses;
    Class<?> realExceptionClass= ite.getClass();

    for (Class<?> exception : exceptions) {
      if (exception.isAssignableFrom(realExceptionClass)) {
        return true;
      }
    }

    return false;
  }

  static interface Predicate<K, T> {
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

  /**
   * @return Only the ITestNGMethods applicable for this testClass
   */
  private ITestNGMethod[] filterMethods(IClass testClass, ITestNGMethod[] methods,
      Predicate<ITestNGMethod, IClass> predicate) {
    List<ITestNGMethod> vResult= Lists.newArrayList();

    for(ITestNGMethod tm : methods) {
      if (predicate.isTrue(tm, testClass)) {
        log(10, "Keeping method " + tm + " for class " + testClass);
        vResult.add(tm);
      } else {
        log(10, "Filtering out method " + tm + " for class " + testClass);
      }
    }

    ITestNGMethod[] result= vResult.toArray(new ITestNGMethod[vResult.size()]);

    return result;
  }

  /**
   * @return true if this method depends on certain groups.
   */
  private boolean dependsOnGroups(ITestNGMethod tm) {
    String[] groups = tm.getGroupsDependedUpon();
    boolean result = (null != groups) && (groups.length > 0);
    return result;
  }

  /**
   * @return true if this method depends on certain groups.
   */
  private boolean dependsOnMethods(ITestNGMethod tm) {
    String[] methods = tm.getMethodsDependedUpon();
    boolean result = (null != methods) && (methods.length > 0);
    return result;
  }

  private void runConfigurationListeners(ITestResult tr, boolean before) {
    if (before) {
      for(IConfigurationListener icl: m_notifier.getConfigurationListeners()) {
        if (icl instanceof IConfigurationListener2) {
          ((IConfigurationListener2) icl).beforeConfiguration(tr);
        }
      }
    } else {
      for(IConfigurationListener icl: m_notifier.getConfigurationListeners()) {
        switch(tr.getStatus()) {
          case ITestResult.SKIP:
            icl.onConfigurationSkip(tr);
            break;
          case ITestResult.FAILURE:
            icl.onConfigurationFailure(tr);
            break;
          case ITestResult.SUCCESS:
            icl.onConfigurationSuccess(tr);
            break;
        }
      }
    }
  }

  void runTestListeners(ITestResult tr) {
    runTestListeners(tr, m_notifier.getTestListeners());
  }

  // TODO: move this from here as it is directly called from TestNG
  public static void runTestListeners(ITestResult tr, List<ITestListener> listeners) {
    for (ITestListener itl : listeners) {
      switch(tr.getStatus()) {
        case ITestResult.SKIP: {
          itl.onTestSkipped(tr);
          break;
        }
        case ITestResult.SUCCESS_PERCENTAGE_FAILURE: {
          itl.onTestFailedButWithinSuccessPercentage(tr);
          break;
        }
        case ITestResult.FAILURE: {
          itl.onTestFailure(tr);
          break;
        }
        case ITestResult.SUCCESS: {
          itl.onTestSuccess(tr);
          break;
        }

        case ITestResult.STARTED: {
          itl.onTestStart(tr);
          break;
        }

        default: {
          assert false : "UNKNOWN STATUS:" + tr;
        }
      }
    }
  }

  private void log(int level, String s) {
    Utils.log("Invoker " + Thread.currentThread().hashCode(), level, s);
  }

  /**
   * This class holds a {@code ParameterHolder} and in case of an error, a non-null
   * {@code TestResult} containing the cause
   */
  private static class ParameterBag {
    final ParameterHolder parameterHolder;
    final List<ITestResult> errorResults = Lists.newArrayList();

    public ParameterBag(ParameterHolder params, TestResult tr) {
      parameterHolder = params;
      if (tr != null) {
        errorResults.add(tr);
      }
    }

    public boolean hasErrors() {
      return !errorResults.isEmpty();
    }
  }

}
