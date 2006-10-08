package org.testng.internal;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.ClassMethodMap;
import org.testng.IClass;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SuiteRunState;
import org.testng.TestException;
import org.testng.TestNGException;
import org.testng.internal.InvokeMethodRunnable.TestNGRuntimeException;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IConfiguration;
import org.testng.internal.thread.ICountDown;
import org.testng.internal.thread.IExecutor;
import org.testng.internal.thread.IFutureResult;
import org.testng.internal.thread.IPooledExecutor;
import org.testng.internal.thread.IThreadFactory;
import org.testng.internal.thread.ThreadExecutionException;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.internal.thread.ThreadUtil;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

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
  private ITestContext m_testContext;
  private ITestResultNotifier m_notifier;
  private IAnnotationFinder m_annotationFinder;
  private SuiteRunState m_suiteState;

  public Invoker(ITestContext testContext,
                 ITestResultNotifier notifier,
                 SuiteRunState state,
                 IAnnotationFinder annotationFinder) {
    m_testContext= testContext;
    m_suiteState= state;
    m_notifier= notifier;
    m_annotationFinder= annotationFinder;
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
  public void invokeConfigurations(IClass testClass,
                                   ITestNGMethod[] allMethods,
                                   XmlSuite suite,
                                   Map<String, String> params,
                                   Object instance)
  {
    if(null == allMethods) {
      log(5, "No @Configuration methods found");

      return;
    }

    ITestNGMethod[] methods= filterMethodsUnique(testClass, allMethods);

    for(ITestNGMethod tm : methods) {
      if(null == testClass) {
        testClass= tm.getTestClass();
      }

      ITestResult testResult= new TestResult(testClass, instance,
                                             tm,
                                             null,
                                             System.currentTimeMillis(),
                                             System.currentTimeMillis());

      IConfiguration configurationAnnotation= null;
      try {
        Object[] instances= tm.getInstances();
        if (instances == null || instances.length == 0) instances = new Object[] { instance };
        Class objectClass= instances[0].getClass();
        Method method= tm.getMethod();

        // Only run the configuration if
        // - the test is enabled and
        // - the Configuration method belongs to the same class or a parent
        if(MethodHelper.isEnabled(objectClass, m_annotationFinder)) {
          configurationAnnotation= (IConfiguration)
          AnnotationHelper.findConfiguration(m_annotationFinder, method);

          boolean before= (null != configurationAnnotation)
            ? configurationAnnotation.getBeforeTestClass()
            : false;

          boolean after= (null != configurationAnnotation)
            ? configurationAnnotation.getAfterTestClass()
            : false;
          boolean alwaysRun= false;
          if(null != configurationAnnotation) {
            if ((configurationAnnotation.getAfterSuite()
                || configurationAnnotation.getAfterTest()
                || configurationAnnotation.getAfterTestClass()
                || configurationAnnotation.getAfterTestMethod())
              && configurationAnnotation.getAlwaysRun())
            {
              alwaysRun= true;
            }

          }

          if(!confInvocationPassed(tm.getRealClass()) && !alwaysRun) {
            testResult.setStatus(ITestResult.SKIP);
            m_notifier.addSkippedTest(tm, testResult);
            runTestListeners(testResult);

            continue;
          }

          boolean isClassConfiguration= (null != configurationAnnotation) && (before || after);

          log(3, "Invoking " + tm);

          Object[] parameters= Parameters.createConfigurationParameters(tm.getMethod(),
                                                                        params,
                                                                        m_annotationFinder,
                                                                        suite);
          testResult.setParameters(parameters);
//          Reporter.setCurrentOutput(tm.getExtraOutput().getOutput());
          Object[] newInstances= (null != instance) ? new Object[] { instance } : instances;

          invokeConfigurationMethod(newInstances, tm, parameters, 
              isClassConfiguration, testResult);
//          Reporter.setCurrentOutput(null);
        } // if is enabled
        else {
          log(3,
              "Skipping "
              + method.getDeclaringClass().getName() + "." + method.getName() + "()"
              + " because "
              + objectClass.getName()
              + " is not enabled");
        }
      }
      catch(InvocationTargetException ex) {
        handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, suite);
      }
      // Don't wrap TestNGExceptions, it could be a missing parameter on a
      // @Configuration method
      catch(TestNGException ex) {
        handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, suite);
        Utils.log("", 1, ex.getMessage());
      }
      catch(Throwable ex) { // covers the non-wrapper exceptions
        handleConfigurationFailure(ex, tm, testResult, configurationAnnotation, suite);
      }
    } // for methods
  }

  private void handleConfigurationFailure(Throwable ite,
                                          ITestNGMethod tm,
                                          ITestResult testResult,
                                          IConfiguration annotation,
                                          XmlSuite suite) 
  {
    handleException(ite.getCause(), tm, testResult, 1);
    runTestListeners(testResult);

    // 
    // If in TestNG mode, need to take a look at the annotation to figure out
    // what kind of @Configuration method we're dealing with
    //
    if (null != annotation) {
      // If beforeTestClass/beforeTestMethod or afterTestClass/afterTestMethod 
      // failed, mark this entire class as failed, but only this class (the other 
      // classes should keep running normally)
      if(annotation.getBeforeTestClass()
        || annotation.getAfterTestClass()
        || annotation.getBeforeTestMethod()
        || annotation.getAfterTestMethod()) 
      {
        setClassInvocationFailure(tm.getRealClass(), false);
      }
  
      // If beforeSuite or afterSuite failed, mark *all* the classes as failed
      // for configurations.  At this point, the entire Suite is screwed
      else if (annotation.getBeforeSuite() || annotation.getAfterSuite()) {
        m_suiteState.failed();
      }
  
      // beforeTest or afterTest:  mark all the classes in the same
      // <test> stanza as failed for configuration
      else if (annotation.getBeforeTest() || annotation.getAfterTest()) {
        XmlClass[] classes= findClassesInSameTest(tm.getRealClass(), suite);
        for(XmlClass xmlClass : classes) {
          setClassInvocationFailure(xmlClass.getSupportClass(), false);
        }
      }
    }
    else  {
      // TODO: remove this code. We should never pass through it,
      // as JUnit runs now independently
      //
      // If we're in JUnit mode, mark the entire class failed
      //
      String methodName = tm.getMethod().getName();
      boolean isJUnit = "setUp".equals(methodName) ||
          "tearDown".equals(methodName);
      if (isJUnit) {
        setClassInvocationFailure(tm.getRealClass(), false);
      }
    }      
  }

  /**
   * @return All the classes that belong to the same <test> tag as @param cls
   */
  private XmlClass[] findClassesInSameTest(Class cls, XmlSuite suite) {
    Map<String, XmlClass> vResult= new HashMap<String, XmlClass>();
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
   * @return true if this class has successfully run all its @Configuration
   * method or false if at least one of these methods failed.
   */
  private boolean confInvocationPassed(Class cls) {
    boolean result= true;

    if(m_suiteState.isFailed()) {
      result= false;
    }
    else {
      if(m_classInvocationResults.containsKey(cls)) {
        result= m_classInvocationResults.get(cls);
      }
      else {
        for(Class clazz: m_classInvocationResults.keySet()) {
          if(clazz.isAssignableFrom(cls)) {
            result= false;
            break;
          }
        }
      }
    }

    return result;
  }

  /** Class failures must be synched as the Invoker is accessed concurrently */
  private Map<Class, Boolean> m_classInvocationResults= new Hashtable<Class, Boolean>();

  private void setClassInvocationFailure(Class clazz, boolean flag) {
    m_classInvocationResults.put(clazz, flag);
  }

  private void invokeConfigurationMethod(Object[] instances,
                                         ITestNGMethod tm,
                                         Object[] params,
                                         boolean isClass,
                                         ITestResult testResult)
    throws InvocationTargetException, IllegalAccessException 
  {

    // Mark this method with the current thread id
    tm.setId(Thread.currentThread().hashCode());
    long timeOut= tm.getTimeOut();

    for(Object targetInstance : instances) {
      InvokedMethod im= new InvokedMethod(targetInstance,
                                          tm,
                                          params,
                                          false, /* isTest */
                                          isClass, /* ??? */
                                          System.currentTimeMillis());

      m_notifier.addInvokedMethod(im);

      try {
        Reporter.setCurrentTestResult(testResult);
        MethodHelper.invokeMethod(tm.getMethod(), targetInstance, params);
      } 
      finally {
        Reporter.setCurrentTestResult(testResult);
      }      
    }
  }
  
  /**
   * Both invokeTestMethod() and invokeConfigurationMethod() eventually
   * converge here.  This method is responsible for actually invoking the
   * method.  If a timeOut was specified for this test method, it will be invoked
   * in a separate thread so that we can interrupt it when the timeOut expires.
   *
   */
  private List<ITestResult> invokeMethod(Object[] instances,
                                         final ITestNGMethod tm,
                                         Object[] parameterValues,
                                         XmlSuite suite,
                                         Map<String, String> params,
                                         ITestClass testClass,
                                         ITestNGMethod[] beforeMethods,
                                         ITestNGMethod[] afterMethods,
                                         ConfigurationGroupMethods groupMethods)
  {
    List<ITestResult> results = new ArrayList<ITestResult>();
    Method thisMethod= null;

    // Mark this method with the current thread id
    tm.setId(Thread.currentThread().hashCode());

    long timeOut= tm.getTimeOut();

    for(int i= 0; i < instances.length; i++) {
      
      //
      // Invoke beforeGroups configurations
      //
      invokeBeforeGroupsConfigurations(testClass, tm, groupMethods, suite, params, instances[i]);

      //
      // Invoke beforeMethod configurations
      //
      invokeConfigurations(testClass, beforeMethods, suite, params, instances[i]);
      
      //
      // Create the ExtraOutput for this method
      //
      TestResult testResult = null;
      try {
        testResult= new TestResult(testClass, instances[i],
                                   tm,
                                   null,
                                   System.currentTimeMillis(),
                                   0);
        testResult.setParameters(parameterValues);
        testResult.setHost(m_testContext.getHost());
        testResult.setStatus(ITestResult.STARTED);
        runTestListeners(testResult);
        results.add(testResult);

        InvokedMethod invokedMethod= new InvokedMethod(instances[i],
                                                       tm,
                                                       parameterValues,
                                                       true,
                                                       false,
                                                       System.currentTimeMillis());

        m_notifier.addInvokedMethod(invokedMethod);
        thisMethod= tm.getMethod();

        if(confInvocationPassed(thisMethod.getDeclaringClass())) {
          log(3, "Invoking " + thisMethod.getDeclaringClass().getName() + "." + thisMethod.getName());

          // If no timeOut, just invoke the method
          if(timeOut <= 0) {
            //
            // If this method is a IHookable, invoke its run() method
            //
            if (IHookable.class.isAssignableFrom(thisMethod.getDeclaringClass())) {
              invokeHookable(instances, parameterValues, testClass, thisMethod, i, testResult);
            }
            //
            // Not a IHookable, invoke directly
            //
            else {
              try {
                Reporter.setCurrentTestResult(testResult);
                MethodHelper.invokeMethod(thisMethod, instances[i], parameterValues);
                testResult.setStatus(ITestResult.SUCCESS);
              } 
              finally {
                Reporter.setCurrentTestResult(null);
              }                            
            }
          }
          else {
            //
            // Timeout was specified, use an Executor/CountDownLatch
            //
            ICountDown done= ThreadUtil.createCountDown(1);
            IThreadFactory factory= ThreadUtil.createFactory(tm.getMethod().getName());
            IExecutor exec= ThreadUtil.createExecutor(1, factory);

            try {
              Reporter.setCurrentTestResult(testResult);
              InvokeMethodRunnable imr = 
                  new InvokeMethodRunnable(tm,
                      instances[i],
                      parameterValues,
                      done);
              IFutureResult future= exec.submitRunnable(imr);
              exec.shutdown();
              boolean finished= exec.awaitTermination(timeOut);

              if(!finished) {
                exec.stopNow();
                testResult.setThrowable(new ThreadTimeoutException("Method "
                                                            + thisMethod
                                                            + " didn't finish within the time-out "
                                                            + timeOut));
                testResult.setStatus(ITestResult.FAILURE);
              }
              else {
                log(3, "Method " + thisMethod + " completed within the time-out " + timeOut);
                
                // We don't need the result from the future but invoking get() on it
                // will trigger the exception that was thrown, if any
                future.get();
                done.await();

                testResult.setStatus(ITestResult.SUCCESS); // if no exception till here than SUCCESS
              }

            }
            catch(InterruptedException e) {
              System.err.println("WARN: invocation of method " + thisMethod
                                 + " has been interrupted.");
            }
            finally {
              Reporter.setCurrentTestResult(null);
            }                            
          }
        }
        else {
          testResult.setStatus(ITestResult.SKIP);
        }
      }
      catch(InvocationTargetException ite) {
        testResult.setThrowable(ite.getCause());
      }
      catch(ThreadExecutionException tee) { // wrapper for TestNGRuntimeException
        Throwable cause= tee.getCause();
        if(TestNGRuntimeException.class.equals(cause.getClass())) {
          testResult.setThrowable(cause.getCause());
        }
        else {
          testResult.setThrowable(cause);
        }
      }
      catch(Throwable thr) { // covers the non-wrapper exceptions
        testResult.setThrowable(thr);
      }
      finally {
        //
        // Increment the invocation count for this method
        //
        tm.incrementCurrentInvocationCount();

        if (testResult != null) testResult.setEndMillis(System.currentTimeMillis());
        //
        // Invoke afterMethods
        //
        invokeConfigurations(testClass, afterMethods, suite, params, instances[i]);
        
        //
        // Invoke beforeGroups configurations
        //
        invokeAfterGroupsConfigurations(testClass, tm, 
            groupMethods, suite, params, instances[i]);
      }

      
    } // for instances
    
    return results;
  }

  /**
   * Filter all the beforeGroups methods and invoke only those that apply
   * to the current test method
   */
  private void invokeBeforeGroupsConfigurations(ITestClass testClass, 
      ITestNGMethod tm, 
      ConfigurationGroupMethods groupMethods, 
      XmlSuite suite, Map<String, String> params, 
      Object instance) 
  {
    synchronized(groupMethods) {
      List<ITestNGMethod> filteredMethods = new ArrayList<ITestNGMethod>();
      String[] groups = tm.getGroups();
      Map<String, List<ITestNGMethod>> beforeGroupMap = groupMethods.getBeforeGroupsMap();
      
      for (String group : groups) {
        List<ITestNGMethod> methods = beforeGroupMap.get(group);
        if (methods != null) {
          filteredMethods.addAll(methods);
        }
      }
      
      //
      // Invoke the right groups methods
      //
      ITestNGMethod[] beforeMethodsArray = 
        filteredMethods.toArray(new ITestNGMethod[filteredMethods.size()]);
      invokeConfigurations(testClass, beforeMethodsArray, suite, params, instance);
      
      //
      // Remove them so they don't get run again
      //
      groupMethods.removeBeforeGroups(groups);
    }
  }

  private void invokeAfterGroupsConfigurations(ITestClass testClass, 
      ITestNGMethod currentTestMethod,
      ConfigurationGroupMethods groupMethods,
      XmlSuite suite, Map<String, String> params, Object instance) 
  {
    // Skip this if no afterGroups have been defined
    if (testClass.getAfterGroupsMethods().length == 0) return;
    
    // Skip this if the current method doesn't belong to any group
    // (only a method that belongs to a group can trigger the invocation
    // of afterGroups methods)
    if (currentTestMethod.getGroups().length == 0) return;
    
    // See if the currentMethod is the last method in any of the groups
    // it belongs to
    Map<String, String> filteredGroups = new HashMap<String, String>();
    String[] groups = currentTestMethod.getGroups();
    synchronized(groupMethods) {
      for (String group : groups) {
        if (groupMethods.isLastMethodForGroup(group, currentTestMethod)) {
          filteredGroups.put(group, group);
        }
      }
      
      // The list of afterMethods to run
      Map<ITestNGMethod, ITestNGMethod> afterMethods = new HashMap<ITestNGMethod, ITestNGMethod>();
      
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
      ITestNGMethod[] afterMethodsArray = 
        afterMethods.keySet().toArray(new ITestNGMethod[afterMethods.size()]);
      invokeConfigurations(testClass, afterMethodsArray, suite, params, instance);

      // Remove the groups so they don't get run again
      groupMethods.removeAfterGroups(filteredGroups.keySet());      
    }
  }

  private void invokeHookable(Object[] instances, Object[] parameters, 
      ITestClass testClass, Method thisMethod, int i, TestResult testResult) 
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, Throwable 
  {
    Method runMethod = null;
    runMethod = testClass.getRealClass().getMethod("run", 
        new Class[] { IHookCallBack.class, ITestResult.class });
    final Method a = thisMethod;
    final Object b = instances[i];
    final Object[] c = parameters;
    final Throwable[] error = new Throwable[1];
    IHookCallBack callback = new IHookCallBack() {
      public void runTestMethod(ITestResult tr) {
        try {
          MethodHelper.invokeMethod(a, b, c);
         }
         catch(Throwable t) {
           error[0] = t;
         }
       }
    };
    runMethod.invoke(instances[i],new Object[]{callback, testResult});
    if (error[0] != null) {
      throw error[0];
    }
    testResult.setStatus(ITestResult.SUCCESS);
  }
  
  /**
   * If the method has parameters, fill them in.  Either by using a @DataProvider
   * if any was provided, or by looking up <parameters> in testng.xml
   */
  private Iterator<Object[]> handleParameters(ITestNGMethod testMethod, 
                                              Map<String, String> allParameterNames,
                                              ITestClass testClass, 
                                              Map<String, String> parameters, 
                                              XmlSuite xmlSuite)
  {
    Iterator<Object[]> result = null;
    
    //
    // Do we have a @DataProvider?  If yes, then we have several
    // sets of parameters for this method
    //
    Method dataProvider = 
      Parameters.findDataProvider(testMethod.getTestClass().getRealClass(),
                                  testMethod.getMethod(), 
                                  m_annotationFinder);

    if (null != dataProvider) {
      int parameterCount = testMethod.getMethod().getParameterTypes().length;

      for (int i = 0; i < parameterCount; i++) {
        String n = "param" + i;
        allParameterNames.put(n, n);
      }

      boolean isStatic = 
        (dataProvider.getModifiers() & Modifier.STATIC) != 0;
      Object instance = isStatic ? null : testClass.getInstances(true)[0];
      result  = MethodHelper.invokeDataProvider(
          instance, /* a test instance or null if the dataprovider is static*/
          dataProvider, 
          testMethod);
    }
    else {
      //
      // Normal case:  we have only one set of parameters coming from testng.xml
      //
      allParameterNames.putAll(parameters);
      // Create an Object[][] containing just one row of parameters
      Object[][] allParameterValuesArray = new Object[1][];
      allParameterValuesArray[0] = Parameters.createTestParameters(testMethod.getMethod(),
            parameters,
            m_annotationFinder,
            xmlSuite);
      
      // Mark that this method needs to have at least a certain
      // number of invocations (needed later to call AfterGroups
      // at the right time).
      testMethod.setParameterInvocationCount(allParameterValuesArray.length);
      // Turn it into an Iterable
      result  = MethodHelper.createArrayIterator(allParameterValuesArray);
    }
    
    return result;
  }

  /**
   * Invoke all the test methods.  Note the plural:  the method passed in
   * parameter might be invoked several times if the test class it belongs
   * to has more than one instance (i.e., if an @Factory method has been
   * declared somewhere that returns several instances of this TestClass).
   * If no @Factory method was specified, testMethod will only be invoked
   * once.
   * <p/>
   * Note that this method also takes care of invoking the beforeTestMethod
   * and afterTestMethod, if any.
   */
  public List<ITestResult> invokeTestMethods(ITestNGMethod testMethod,
                                XmlSuite suite,
                                Map<String, String> parameters,
                                ITestNGMethod[] allTestMethods,
                                int testMethodIndex,
                                ConfigurationGroupMethods groupMethods)
  {
    List<ITestResult> result = null;
    
    ITestClass testClass= testMethod.getTestClass();
    Method method= testMethod.getMethod();

    // Potential bug here if the test method was declared on a parent class
    assert null != testClass : "COULDN'T FIND TESTCLASS FOR " + method.getDeclaringClass();

    long start= System.currentTimeMillis();

    //
    // Invoke before method methods
    //
    ITestNGMethod[] beforeMethods = filterMethods(testClass, testClass.getBeforeTestMethods());
    ITestNGMethod[] afterMethods = filterMethods(testClass, testClass.getAfterTestMethods());

    int invocationCount = testMethod.getInvocationCount();
    if (isWithinThreadedMethod()) invocationCount = 1;
    int failureCount = 0;
    // This boolean is used to exit this loop early if we are using a pool
    // thread.  The reason is that in this case, all the invocationCount methods
    // will be invoked at once in different threads
    boolean more = true;
    int threadPoolSize = testMethod.getThreadPoolSize();

    Class[] expectedExceptionClasses = MethodHelper.findExpectedExceptions(m_annotationFinder, testMethod.getMethod());
    while(invocationCount-- > 0 && more) {
      boolean okToProceed = checkDependencies(testMethod, testClass, allTestMethods);

      Object[] parameterValues = null;
      if (okToProceed) {
        //
        // Invoke test method
        //

        //
        // Invoke the test method if it's enabled
        //
        if (MethodHelper.isEnabled(testMethod.getMethod(), m_annotationFinder)) {

            
            // TODO: we should never hit this block, as JUnit is run
            // completely independent now
            // 
            // Special behavior for JUnit:  call setName on the instance with
            // the name of the method to be invoked
            //
//            if (JUnitUtils.isAssignableFromTestCase(testClass.getRealClass())) {
//              String name = testMethod.getMethodName();
//              try {
//                Method m = 
//                  testClass.getRealClass().getMethod("setName", new Class[] { String.class });
//                for (Object instance : instances) {
//                  m.invoke(instance, new Object[] { name });
//                } 
//              }
//              catch (Exception e) {
//                e.printStackTrace();
//              }
//            } // JUnit

            //
            // If threadPoolSize specified, run this method in its own
            // pool thread.  The extra boolean is here to make sure
            // we don't invoke the invoker recursively forever.
            //
            if (threadPoolSize > 1 && ! isWithinThreadedMethod()) {
              //
              // Create the workers
              //
              Map<ITestClass, ITestClass> beforeMethodsMap = new HashMap<ITestClass, ITestClass>();
              beforeMethodsMap.put(testMethod.getTestClass(), testMethod.getTestClass());
              Map<ITestClass, ITestClass> afterMethodsMap = new HashMap<ITestClass, ITestClass>();
              List<TestMethodWorker> workers= new ArrayList<TestMethodWorker>();
              ClassMethodMap classMethodMap = new ClassMethodMap(allTestMethods, true);
              for (int i = 0; i < testMethod.getInvocationCount(); i++) {
                workers.add(new TestMethodWorker(this,
                    new ITestNGMethod[] {testMethod},
                    suite, 
                    parameters,
                    beforeMethodsMap, 
                    afterMethodsMap,
                    allTestMethods,
                    groupMethods,
                    classMethodMap,
                    true /*force AfterClass execution*/));
              }
              setWithinThreadedMethod(true);
              try {
                result = runWorkers(testMethod, workers, threadPoolSize);
              }
              finally {
                setWithinThreadedMethod(false);
                more = false;
                failureCount = handleInvocationResults(testMethod, result, failureCount, expectedExceptionClasses);
              }
            }
            
            //
            // No threads, regular invocation
            //
            else {
              Map<String, String> allParameterNames = new HashMap<String, String>();
              
              Iterator<Object[]> allParameterValues =
                handleParameters(testMethod, allParameterNames, testClass, parameters, suite);

              while (allParameterValues.hasNext()) {
                parameterValues = allParameterValues.next();
//                Reporter.setCurrentOutput(testMethod.getExtraOutput().getOutput());
                Object[] instances = testClass.getInstances(true);

                result = invokeMethod(instances,
                                      testMethod,
                                      parameterValues,
                                      suite,
                                      allParameterNames,
                                      testClass,
                                      beforeMethods,
                                      afterMethods,
                                      groupMethods);
                
                failureCount = handleInvocationResults(testMethod, result, failureCount, expectedExceptionClasses);
              } // for parameters
            }

        } // isTestMethodEnabled 

      } // okToProceed
      else {
        //
        // Test is being skipped
        //
        ITestResult testResult= new TestResult(testClass, null,
                                               testMethod,
                                               null,
                                               start,
                                               System.currentTimeMillis());
        testResult.setEndMillis(System.currentTimeMillis());
        testResult.setParameters(parameterValues);
        String missingGroup = testMethod.getMissingGroup();
        if (missingGroup != null) {
          testResult.setThrowable(
            new Throwable("Method " + testMethod + 
                " depends on nonexistent group \"" + missingGroup + "\""));
        }

        testResult.setStatus(ITestResult.SKIP);
        m_notifier.addSkippedTest(testMethod, testResult);
        runTestListeners(testResult);
      }
    }
//    Reporter.setCurrentOutput(null);
    
    return result;
    
  } // invokeTestMethod

  /**
   * @param testMethod
   * @param result
   * @param failureCount
   * @param expectedExceptionClasses
   * @return
   */
  private int handleInvocationResults(ITestNGMethod testMethod, 
                                      List<ITestResult> result, 
                                      int failureCount, 
                                      Class[] expectedExceptionClasses) {
    //
    // Go through all the results and create a TestResult for each of them
    //
    for(ITestResult testResult : result) {
      Throwable ite= testResult.getThrowable();
      int status= testResult.getStatus();

      // Exception thrown?
      if(ite != null) {

        //  Invocation caused an exception, see if the method was annotated with @ExpectedException
        if(isExpectedException(ite, expectedExceptionClasses)) {
          testResult.setStatus(ITestResult.SUCCESS);
          status= ITestResult.SUCCESS;
        }
        else {
          handleException(ite, testMethod, testResult, failureCount++);
          status= testResult.getStatus();
        }
      }

      // No exception thrown, make sure we weren't expecting one
      else if(status != ITestResult.SKIP) {
        if (expectedExceptionClasses.length > 0) {
          testResult.setThrowable(
              new TestException("Expected an exception in test method " + testMethod));
          status= ITestResult.FAILURE;
        }
      }

      testResult.setStatus(status);

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

      if (! isWithinThreadedMethod()) {
        runTestListeners(testResult);
      }
    } // for results
    
    return failureCount;
  }

  
  private boolean m_withinThreadedMethod = false;
  
  private boolean isWithinThreadedMethod() {
    return m_withinThreadedMethod;
  }
  
  private void setWithinThreadedMethod(boolean f) {
    m_withinThreadedMethod = f;
  }

  private List<ITestResult> runWorkers(ITestNGMethod testMethod, List<TestMethodWorker> workers, int threadPoolSize)
  {
    
    long maxTimeOut= 10 * 1000; // 10 seconds

    for(TestMethodWorker tmw : workers) {
      long mt= tmw.getMaxTimeOut();
      if(mt > maxTimeOut) {
        maxTimeOut= mt;
      }
    }
    
    ThreadUtil.execute(workers, threadPoolSize, maxTimeOut);

    //
    // Collect all the TestResults
    //
    List<ITestResult> result = new ArrayList<ITestResult>();
    for (TestMethodWorker tmw : workers) {
      result.addAll(tmw.getTestResults());
    }
    
    return result;
  }

  /**
   * @param testMethod
   * @param testClass
   * @return dependencies have been run successfully
   */
  private boolean checkDependencies(ITestNGMethod testMethod, 
      ITestClass testClass, ITestNGMethod[] allTestMethods)
  {
    boolean result= true;

    // If this method is marked alwaysRun, no need to check for its
    // dependencies
    if (testMethod.isAlwaysRun()) {
      return true;
    }
    
    // Any missing group?
    if (testMethod.getMissingGroup() != null) {
      return false;
    }

    // If this method depends on groups, collect all the methods that
    // belong to these groups and make sure they have been run successfully
    if(dependsOnGroups(testMethod)) {
      String[] groupsDependedUpon= testMethod.getGroupsDependedUpon();

      // Get all the methods that belong to the group depended upon
      for(int i= 0; i < groupsDependedUpon.length; i++) {
        ITestNGMethod[] methods = 
          MethodHelper.findMethodsThatBelongToGroup(testMethod, 
              m_testContext.getAllTestMethods(),
              groupsDependedUpon[i]);

        result = result && haveBeenRunSuccessfully(methods);
        
        // As soon as we detect a failure, return right away and let the caller
        // know that the dependencies are not satisfied
//        if (! result) {
//          return false;
//        }
      }
    } // depends on groups

    // If this method depends on other methods, make sure all these other
    // methods have been run successfully
    if(dependsOnMethods(testMethod)) {
      ITestNGMethod[] methods = 
        MethodHelper.findMethodsNamed(testMethod.getMethod().getName(),
                                                       allTestMethods,
                                                       testMethod.getMethodsDependedUpon());

      result= result && haveBeenRunSuccessfully(methods);
    }

    return result;
  }

  /**
   * @return true if all the methods have been run successfully
   */
  private boolean haveBeenRunSuccessfully(ITestNGMethod[] methods) {
    // Make sure the method has been run successfully
    for(int j= 0; j < methods.length; j++) {
      Set<ITestResult> results= m_notifier.getPassedTests(methods[j]);
      
      // If no results were returned, then these tests didn't pass
      if (results == null || results.size() == 0) return false;
      
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
    testResult.setThrowable(throwable);
    int successPercentage= testMethod.getSuccessPercentage();
    int invocationCount= testMethod.getInvocationCount();
    float numberOfTestsThatCanFail= ((100 - successPercentage) * invocationCount) / 100;

    if(failureCount < numberOfTestsThatCanFail) {
      testResult.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
      m_notifier.addFailedButWithinSuccessPercentageTest(testMethod, testResult);
    }
    else {
      testResult.setStatus(ITestResult.FAILURE);
      m_notifier.addFailedTest(testMethod, testResult);
    }

  }

  /**
   * @param ite The exception that was just thrown
   * @param expectedExceptions The list of expected exceptions for this
   * test method
   * @return true if the exception that was just thrown is part of the
   * expected exceptions
   */
  private boolean isExpectedException(Throwable ite, Class[] exceptions) {
    if(null == exceptions) {
      return false;
    }

    Class realExceptionClass= ite.getClass();

    for(int i= 0; i < exceptions.length; i++) {
      if(exceptions[i].isAssignableFrom(realExceptionClass)) {
        return true;
      }
    }

    return false;
  }

  /**
   * @return Only the ITestNGMethods applicable for this testClass
   */
  private ITestNGMethod[] filterMethods(IClass testClass, ITestNGMethod[] methods) {
    List<ITestNGMethod> vResult= new ArrayList<ITestNGMethod>();

    for(ITestNGMethod tm : methods) {
      if(tm.canRunFromClass(testClass)) {
        log(9, "Keeping method " + tm + " for class " + testClass);
        vResult.add(tm);
      }
      else {
        log(9, "Filtering out method " + tm + " for class " + testClass);
      }
    }

    ITestNGMethod[] result= vResult.toArray(new ITestNGMethod[vResult.size()]);

    return result;
  }

  private ITestNGMethod[] filterMethodsUnique(IClass testClass, ITestNGMethod[] methods) {
    if(null == testClass) {
      return methods;
    }

    List<ITestNGMethod> vResult= new ArrayList<ITestNGMethod>();

    for(ITestNGMethod tm : methods) {
      if(null == testClass) {
        testClass= tm.getTestClass();
      }

      if(tm.getTestClass().getName().equals(testClass.getName())) {
        log(9, "        Keeping method " + tm + " for class " + testClass);

        vResult.add(tm);
      }
      else {
        log(9, "        Filtering out method " + tm + " for class " + testClass);
      }
    }

    ITestNGMethod[] result= vResult.toArray(new ITestNGMethod[vResult.size()]);

    return result;
  }

  /**
   * @return true if this method depends on certain groups.
   */
  private boolean dependsOnGroups(ITestNGMethod tm) {
    String[] groups= tm.getGroupsDependedUpon();
    boolean result= (null != groups) && (groups.length > 0);

    return result;
  }

  /**
   * @return true if this method depends on certain groups.
   */
  private boolean dependsOnMethods(ITestNGMethod tm) {
    String[] methods= tm.getMethodsDependedUpon();
    boolean result= (null != methods) && (methods.length > 0);

    return result;
  }

  public void runTestListeners(ITestResult tr) {
    runTestListeners(tr, m_notifier.getTestListeners());
  }
  
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

  private static void ppp(String s) {
    System.out.println("[Invoker]" + s);
  }

  private void log(int level, String s) {
    Utils.log("Invoker " + Thread.currentThread().hashCode(), level, s);
  }
}
