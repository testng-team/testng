package org.testng.internal;

import static org.testng.internal.Invoker.SAME_CLASS;
import static org.testng.internal.invokers.InvokedMethodListenerMethod.AFTER_INVOCATION;
import static org.testng.internal.invokers.InvokedMethodListenerMethod.BEFORE_INVOCATION;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.IClass;
import org.testng.IConfigurable;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SuiteRunState;
import org.testng.TestNGException;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.ConfigMethodArguments.Builder;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.thread.ThreadUtil;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;

class ConfigInvoker extends BaseInvoker implements IConfigInvoker {

  /**
   * Test methods whose configuration methods have failed.
   */
  protected final Map<ITestNGMethod, Set<Object>> m_methodInvocationResults = Maps
      .newConcurrentMap();

  private final boolean m_continueOnFailedConfiguration;

  private final Set<ITestNGMethod> m_executedConfigMethods = ConcurrentHashMap.newKeySet();

  /**
   * Group failures must be synced as the Invoker is accessed concurrently
   */
  private final Map<String, Boolean> m_beforegroupsFailures = Maps.newConcurrentMap();

  public ConfigInvoker(ITestResultNotifier notifier,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      ITestContext testContext,
      SuiteRunState suiteState,
      IConfiguration configuration) {
    super(notifier, invokedMethodListeners, testContext, suiteState, configuration);
    this.m_continueOnFailedConfiguration =
        testContext.getSuite().getXmlSuite().getConfigFailurePolicy()
            == XmlSuite.FailurePolicy.CONTINUE;
  }

  /**
   * @return false if this class has successfully run all its @Configuration method or true if at
   * least one of these methods failed.
   */
  public boolean hasConfigurationFailureFor(
      ITestNGMethod testNGMethod, String[] groups, IClass testClass, Object instance) {
    boolean result = false;

    Class<?> cls = testClass.getRealClass();

    if (m_suiteState.isFailed()) {
      result = true;
    } else {
      boolean hasConfigurationFailures = classConfigurationFailed(cls, instance);
      if (hasConfigurationFailures) {
        if (!m_continueOnFailedConfiguration) {
          result = true;
        } else {
          Set<Object> set = getInvocationResults(testClass);
          result = set.contains(instance);
        }
        return result;
      }
      // if method is BeforeClass, currentTestMethod will be null
      if (m_continueOnFailedConfiguration && hasConfigFailure(testNGMethod)) {
        Object key = TestNgMethodUtils.getMethodInvocationToken(testNGMethod, instance);
        result = m_methodInvocationResults.get(testNGMethod).contains(key);
      } else if (!m_continueOnFailedConfiguration) {
        for (Class<?> clazz : m_classInvocationResults.keySet()) {
          if (clazz.isAssignableFrom(cls)
              && m_classInvocationResults.get(clazz).contains(instance)) {
            result = true;
            break;
          }
        }
      }
    }

    // check if there are failed @BeforeGroups
    for (String group : groups) {
      if (m_beforegroupsFailures.containsKey(group)) {
        result = true;
        break;
      }
    }
    return result;
  }

  /**
   * Filter all the beforeGroups methods and invoke only those that apply to the current test
   * method
   * @param arguments
   */
  public void invokeBeforeGroupsConfigurations(GroupConfigMethodArguments arguments) {
    List<ITestNGMethod> filteredMethods = Lists.newArrayList();
    String[] groups = arguments.getTestMethod().getGroups();

    for (String group : groups) {
      List<ITestNGMethod> methods = arguments.getGroupMethods().getBeforeGroupMethodsForGroup(group);
      if (methods != null) {
        filteredMethods.addAll(methods);
      }
    }

    ITestNGMethod[] beforeMethodsArray = filteredMethods.toArray(new ITestNGMethod[0]);
    //
    // Invoke the right groups methods
    //
    if (beforeMethodsArray.length > 0) {
      // don't pass the IClass or the instance as the method may be external
      // the invocation must be similar to @BeforeTest/@BeforeSuite
      ConfigMethodArguments configMethodArguments = new Builder()
          .usingConfigMethodsAs(beforeMethodsArray)
          .forSuite(arguments.getSuite())
          .usingParameters(arguments.getParameters())
          .usingInstance(arguments.getInstance())
          .build();
      invokeConfigurations(configMethodArguments);
    }

    //
    // Remove them so they don't get run again
    //
    arguments.getGroupMethods().removeBeforeGroups(groups);
  }

  public void invokeAfterGroupsConfigurations(GroupConfigMethodArguments arguments) {
    // Skip this if the current method doesn't belong to any group
    // (only a method that belongs to a group can trigger the invocation
    // of afterGroups methods)
    if (arguments.getTestMethod().getGroups().length == 0) {
      return;
    }

    // See if the currentMethod is the last method in any of the groups
    // it belongs to
    Map<String, String> filteredGroups = Maps.newHashMap();
    String[] groups = arguments.getTestMethod().getGroups();
    for (String group : groups) {
      if (arguments.getGroupMethods().isLastMethodForGroup(group,
          arguments.getTestMethod())) {
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
    for (String g : filteredGroups.values()) {
      List<ITestNGMethod> methods = arguments.getGroupMethods().getAfterGroupMethodsForGroup(g);
      // Note:  should put them in a map if we want to make sure the same afterGroups
      // doesn't get run twice
      if (methods != null) {
        for (ITestNGMethod m : methods) {
          afterMethods.put(m, m);
        }
      }
    }

    // Got our afterMethods, invoke them
    ITestNGMethod[] afterMethodsArray = afterMethods.keySet().toArray(new ITestNGMethod[0]);
    // don't pass the IClass or the instance as the method may be external
    // the invocation must be similar to @BeforeTest/@BeforeSuite
    ConfigMethodArguments configMethodArguments = new Builder()
        .usingConfigMethodsAs(afterMethodsArray)
        .forSuite(arguments.getSuite())
        .usingParameters(arguments.getParameters())
        .usingInstance(arguments.getInstance())
        .build();

    invokeConfigurations(configMethodArguments);

    // Remove the groups so they don't get run again
    arguments.getGroupMethods().removeAfterGroups(filteredGroups.keySet());
  }

  public void invokeConfigurations(ConfigMethodArguments arguments) {
    if (arguments.getConfigMethods().length == 0) {
      log(5, "No configuration methods found");
      return;
    }

    ITestNGMethod[] methods = TestNgMethodUtils.filterMethods(arguments.getTestClass(),
        arguments.getConfigMethods(), SAME_CLASS);
    Object[] parameters = new Object[]{};

    for (ITestNGMethod tm : methods) {
      if (null == arguments.getTestClass()) {
        arguments.setTestClass(tm.getTestClass());
      }

      ITestResult testResult = TestResult.newContextAwareTestResult(tm, m_testContext);
      testResult.setStatus(ITestResult.STARTED);

      IConfigurationAnnotation configurationAnnotation = null;
      try {
        Object inst = tm.getInstance();
        if (inst == null) {
          inst = arguments.getInstance();
        }
        Class<?> objectClass = inst.getClass();
        ConstructorOrMethod method = tm.getConstructorOrMethod();

        // Only run the configuration if
        // - the test is enabled and
        // - the Configuration method belongs to the same class or a parent
        configurationAnnotation = AnnotationHelper.findConfiguration(annotationFinder(), method);
        boolean alwaysRun = MethodHelper.isAlwaysRun(configurationAnnotation);
        boolean canProcessMethod =
            MethodHelper.isEnabled(objectClass, annotationFinder()) || alwaysRun;
        if (!canProcessMethod) {
          log(
              3,
              "Skipping "
                  + Utils.detailedMethodName(tm, true)
                  + " because "
                  + objectClass.getName()
                  + " is not enabled");
          continue;
        }
        if (MethodHelper.isDisabled(configurationAnnotation)) {
          log(3, "Skipping " + Utils.detailedMethodName(tm, true) + " because it is not enabled");
          continue;
        }
        if (hasConfigurationFailureFor(arguments.getTestMethod(), tm.getGroups() ,
            arguments.getTestClass(),
            arguments.getInstance()) && !alwaysRun) {
          log(3, "Skipping " + Utils.detailedMethodName(tm, true));
          InvokedMethod invokedMethod =
              new InvokedMethod(arguments.getInstance(), tm, System.currentTimeMillis(), testResult);
          runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, testResult);
          testResult.setStatus(ITestResult.SKIP);
          runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
          handleConfigurationSkip(
              tm, testResult, configurationAnnotation,
              arguments.getTestMethod(),
              arguments.getInstance(),
              arguments.getSuite());
          continue;
        }

        log(3, "Invoking " + Utils.detailedMethodName(tm, true));
        if (arguments.getTestMethodResult() != null) {
          ((TestResult) arguments.getTestMethodResult()).setMethod(
              arguments.getTestMethod());
        }

        parameters =
            Parameters.createConfigurationParameters(
                tm.getConstructorOrMethod().getMethod(),
                arguments.getParameters(),
                arguments.getParameterValues(),
                arguments.getTestMethod(),
                annotationFinder(),
                arguments.getSuite(),
                m_testContext,
                arguments.getTestMethodResult());
        testResult.setParameters(parameters);

        runConfigurationListeners(testResult, true /* before */);

        Object newInstance = computeInstance(arguments.getInstance(), inst, tm);
        if (isConfigMethodEligibleForScrutiny(tm)) {
          if (m_executedConfigMethods.add(arguments.getTestMethod())) {
            invokeConfigurationMethod(newInstance, tm, parameters, testResult);
          }
        } else {
          invokeConfigurationMethod(newInstance, tm, parameters, testResult);
        }
        copyAttributesFromNativelyInjectedTestResult(parameters,
            arguments.getTestMethodResult());
        runConfigurationListeners(testResult, false /* after */);
      } catch (Throwable ex) {
        handleConfigurationFailure(
            ex, tm, testResult, configurationAnnotation,
            arguments.getTestMethod(),
            arguments.getInstance(),
            arguments.getSuite());
        copyAttributesFromNativelyInjectedTestResult(parameters,
            arguments.getTestMethodResult());
      }
    } // for methods
  }

  /**
   * Effectively invokes a configuration method on all passed in instances.
   */
  // TODO: Change this method to be more like invokeMethod() so that we can handle calls to {@code
  // IInvokedMethodListener} better.
  private void invokeConfigurationMethod(
      Object targetInstance, ITestNGMethod tm, Object[] params, ITestResult testResult)
      throws InvocationTargetException, IllegalAccessException {
    // Mark this method with the current thread id
    tm.setId(ThreadUtil.currentThreadInfo());

    InvokedMethod invokedMethod =
        new InvokedMethod(targetInstance, tm, System.currentTimeMillis(), testResult);

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
        MethodInvocationHelper.invokeConfigurable(
            targetInstance, params, configurableInstance, method.getMethod(), testResult);
      } else {
        MethodInvocationHelper.invokeMethodConsideringTimeout(
            tm, method, targetInstance, params, testResult);
      }
      testResult.setStatus(ITestResult.SUCCESS);
    } catch (InvocationTargetException | IllegalAccessException ex) {
      throwConfigurationFailure(testResult, ex);
      testResult.setStatus(ITestResult.FAILURE);
      throw ex;
    } catch (Throwable ex) {
      throwConfigurationFailure(testResult, ex);
      testResult.setStatus(ITestResult.FAILURE);
      throw new TestNGException(ex);
    } finally {
      testResult.setEndMillis(System.currentTimeMillis());
      Reporter.setCurrentTestResult(testResult);
      runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
      Reporter.setCurrentTestResult(null);
    }
  }

  private void throwConfigurationFailure(ITestResult testResult, Throwable ex) {
    testResult.setStatus(ITestResult.FAILURE);
    testResult.setThrowable(ex.getCause() == null ? ex : ex.getCause());
  }

  private IConfigurable computeConfigurableInstance(
      ConstructorOrMethod method, Object targetInstance) {
    return IConfigurable.class.isAssignableFrom(method.getDeclaringClass())
        ? (IConfigurable) targetInstance
        : m_configuration.getConfigurable();
  }


  private void runConfigurationListeners(ITestResult tr, boolean before) {
    if (before) {
      TestListenerHelper.runPreConfigurationListeners(tr, m_notifier.getConfigurationListeners());
    } else {
      TestListenerHelper.runPostConfigurationListeners(tr, m_notifier.getConfigurationListeners());
    }
  }

  /**
   * Marks the current <code>TestResult</code> as skipped and invokes the listeners.
   */
  private void handleConfigurationSkip(
      ITestNGMethod tm,
      ITestResult testResult,
      IConfigurationAnnotation annotation,
      ITestNGMethod currentTestMethod,
      Object instance,
      XmlSuite suite) {
    recordConfigurationInvocationFailed(
        tm, testResult.getTestClass(), annotation, currentTestMethod, instance, suite);
    testResult.setStatus(ITestResult.SKIP);
    runConfigurationListeners(testResult, false /* after */);
  }

  private boolean hasConfigFailure(ITestNGMethod currentTestMethod) {
    return currentTestMethod != null && m_methodInvocationResults.containsKey(currentTestMethod);
  }


  private void handleConfigurationFailure(
      Throwable ite,
      ITestNGMethod tm,
      ITestResult testResult,
      IConfigurationAnnotation annotation,
      ITestNGMethod currentTestMethod,
      Object instance,
      XmlSuite suite) {
    Throwable cause = ite.getCause() != null ? ite.getCause() : ite;

    if (isSkipExceptionAndSkip(cause)) {
      testResult.setThrowable(cause);
      handleConfigurationSkip(tm, testResult, annotation, currentTestMethod, instance, suite);
      return;
    }
    Utils.log(
        "",
        3,
        "Failed to invoke configuration method "
            + tm.getQualifiedName()
            + ":"
            + cause.getMessage());
    handleException(cause, tm, testResult, 1);
    testResult.setStatus(ITestResult.FAILURE);
    runConfigurationListeners(testResult, false /* after */);

    //
    // If in TestNG mode, need to take a look at the annotation to figure out
    // what kind of @Configuration method we're dealing with
    //
    if (null != annotation) {
      recordConfigurationInvocationFailed(
          tm, testResult.getTestClass(), annotation, currentTestMethod, instance, suite);
    }
  }

  private static boolean isConfigMethodEligibleForScrutiny(ITestNGMethod tm) {
    if (!tm.isBeforeMethodConfiguration()) {
      return false;
    }
    if (!(tm instanceof ConfigurationMethod)) {
      return false;
    }
    ConfigurationMethod cfg = (ConfigurationMethod) tm;
    return cfg.isFirstTimeOnly();
  }

  /**
   * @return true if this class or a parent class failed to initialize.
   */
  private boolean classConfigurationFailed(Class<?> cls, Object instance) {
    return m_classInvocationResults
        .entrySet()
        .stream()
        .anyMatch(
            classSetEntry -> {
              Set<Object> obj = classSetEntry.getValue();
              Class<?> c = classSetEntry.getKey();
              boolean containsBeforeTestOrBeforeSuiteFailure = obj.contains(null);
              return c == cls
                  || c.isAssignableFrom(cls)
                  && (obj.contains(instance) || containsBeforeTestOrBeforeSuiteFailure);
            });
  }

  private static void copyAttributesFromNativelyInjectedTestResult(
      Object[] source, ITestResult target) {
    if (source == null || target == null) {
      return;
    }
    Arrays.stream(source)
        .filter(each -> each instanceof ITestResult)
        .findFirst()
        .ifPresent(eachSource -> TestResult.copyAttributes((ITestResult) eachSource, target));
  }

  private void setMethodInvocationFailure(ITestNGMethod method, Object instance) {
    Set<Object> instances =
        m_methodInvocationResults.computeIfAbsent(method, k -> Sets.newHashSet());
    instances.add(TestNgMethodUtils.getMethodInvocationToken(method, instance));
  }

  private void setClassInvocationFailure(Class<?> clazz, Object instance) {
    synchronized (m_classInvocationResults) {
      Set<Object> instances =
          m_classInvocationResults.computeIfAbsent(clazz, k -> Sets.newHashSet());
      instances.add(instance);
    }
  }

  /**
   * Record internally the failure of a Configuration, so that we can determine later if @Test
   * should be skipped.
   */
  private void recordConfigurationInvocationFailed(
      ITestNGMethod tm,
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
      for (XmlClass xmlClass : classes) {
        setClassInvocationFailure(xmlClass.getSupportClass(), instance);
      }
    }
    String[] beforeGroups = annotation.getBeforeGroups();
    for (String group : beforeGroups) {
      m_beforegroupsFailures.put(group, Boolean.FALSE);
    }
  }

  private static Object computeInstance(Object instance, Object inst, ITestNGMethod tm) {
    if (instance == null
        || !tm.getConstructorOrMethod().getDeclaringClass().isAssignableFrom(instance.getClass())) {
      return inst;
    }
    return instance;
  }

  private Set<Object> getInvocationResults(IClass testClass) {
    Class<?> cls = testClass.getRealClass();
    Set<Object> set = null;
    // We need to continuously search till either our Set is not null (or) till we reached
    // Object class because it is very much possible that the test method is residing in a child
    // class
    // and maybe the parent method has configuration methods which may have had a failure
    // So lets walk up the inheritance tree until either we find failures or till we
    // reached the Object class.
    while (!cls.equals(Object.class)) {
      set = m_classInvocationResults.get(cls);
      if (set != null) {
        break;
      }
      cls = cls.getSuperclass();
    }
    if (set == null) {
      // This should never happen because we have walked up all the way till Object class
      // and yet found no failures, but our logic indicates that there was a failure somewhere up
      // the
      // inheritance order. We don't know what to do at this point.
      throw new IllegalStateException("No failure logs for " + testClass.getRealClass());
    }
    return set;
  }

}
