package org.testng.internal;

import org.testng.IHookCallBack;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IExpectedExceptionsAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.thread.IExecutor;
import org.testng.internal.thread.IFutureResult;
import org.testng.internal.thread.ThreadExecutionException;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.internal.thread.ThreadUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Collection of helper methods to help sort and arrange methods.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class MethodHelper {
//  static private boolean m_quiet = true;

  // ///
  // public methods
  //

  public static ITestNGMethod[] collectAndOrderMethods(List<ITestNGMethod> methods, 
      RunInfo runInfo, IAnnotationFinder finder, List<ITestNGMethod> outExcludedMethods) 
  {
    return internalCollectAndOrderMethods(methods.toArray(new ITestNGMethod[methods.size()]), 
        true /* forTest */, runInfo, finder, false /* unique */, outExcludedMethods);
  }

  /**
   * @param methods
   * @return All the methods that match the filtered groups. If a method belongs
   *         to an excluded group, it is automatically excluded.
   */
  public static ITestNGMethod[] collectAndOrderConfigurationMethods(List<ITestNGMethod> methods, 
      RunInfo runInfo, IAnnotationFinder finder, boolean unique, List<ITestNGMethod> outExcludedMethods) 
  {
    return internalCollectAndOrderMethods(methods.toArray(new ITestNGMethod[methods.size()]), 
          false /* forTests */, runInfo, finder, unique, outExcludedMethods);      
  }

  private static ITestNGMethod[] internalCollectAndOrderMethods(ITestNGMethod[] methods,
      boolean forTests, RunInfo runInfo, IAnnotationFinder finder,
      boolean unique, List<ITestNGMethod> outExcludedMethods) 
  {
    List<ITestNGMethod> includedMethods = Lists.newArrayList();
    collectMethodsByGroup(methods, 
        forTests, 
        includedMethods, 
        outExcludedMethods, 
        runInfo, 
        finder, 
        unique);
    List<ITestNGMethod> vResult = sortMethods(forTests, includedMethods, finder);

    ITestNGMethod[] result = vResult.toArray(new ITestNGMethod[vResult.size()]);

    return result;
  }


  /**
   * @return all the methods that belong to the group specified by the regular
   * expression groupRegExp.  methods[] is the list of all the methods we
   * are choosing from and method is the method that owns the dependsOnGroups
   * statement (only used if a group is missing to flag an error on that method).
   */
  public static ITestNGMethod[] findMethodsThatBelongToGroup(
      ITestNGMethod method,
      ITestNGMethod[] methods, String groupRegexp) 
  {
    boolean foundGroup = false;
    List<ITestNGMethod> vResult = Lists.newArrayList();
    for (ITestNGMethod tm : methods) {
      String[] groups = tm.getGroups();
      for (String group : groups) {
        if (Pattern.matches(groupRegexp, group)) {
          vResult.add(tm);
          foundGroup = true;
        }
      }
    }
    
    if (! foundGroup) {
      method.setMissingGroup(groupRegexp);
    }

    ITestNGMethod[] result = vResult.toArray(new ITestNGMethod[vResult.size()]);
    return result;
  }

  public static ITestNGMethod[] findMethodsNamed(ITestNGMethod m,
      ITestNGMethod[] methods, String[] regexps) 
  {
    String mainMethod = calculateMethodCanonicalName(m);
    List<ITestNGMethod> vResult = Lists.newArrayList();
    String currentRegexp = null;
    for (String fullyQualifiedRegexp : regexps) {
      boolean foundAtLeastAMethod = false;
      
      if(null != fullyQualifiedRegexp) {
        String regexp = escapeRegexp(fullyQualifiedRegexp);
        currentRegexp = regexp;
        boolean usePackage = regexp.indexOf(".") != -1;
  
        for (ITestNGMethod method : methods) {
          Method thisMethod = method.getMethod();
          String thisMethodName = thisMethod.getName();
          String methodName = usePackage ?
              calculateMethodCanonicalName(thisMethod)
              : thisMethodName;
//            ppp("COMPARING\n" + regexp + "\n" + methodName);
          if (Pattern.matches(regexp, methodName)) {
            vResult.add(method);
            foundAtLeastAMethod = true;
          }
        }
      }
      
      if (!foundAtLeastAMethod) {
        if (m.ignoreMissingDependencies()) continue;
        if (m.isAlwaysRun()) continue;
        Method maybeReferringTo = findMethodByName(m, currentRegexp);
        if (maybeReferringTo != null) {
          throw new TestNGException(mainMethod + "() is not allowed to depend on " + maybeReferringTo);
        }
        throw new TestNGException(mainMethod
            + "() is depending on nonexistent method " + currentRegexp);
      }
    }

    ITestNGMethod[] result = vResult.toArray(new ITestNGMethod[vResult.size()]);

    return result;
  }
  
  private static Method findMethodByName(ITestNGMethod mainMethod, String regExp) {
    if (regExp == null) return null;
    int lastDot = regExp.lastIndexOf('.');
    String className, methodName;
    if (lastDot == -1) {
      className = mainMethod.getMethod().getDeclaringClass().getCanonicalName();
      methodName = regExp;
    } else {
      methodName = regExp.substring(lastDot+1);
      className = regExp.substring(0, lastDot);
    }
    
    try {
      Class<?> c = Class.forName(className);
      for (Method m : c.getDeclaredMethods()) {
        if (methodName.equals(m.getName())) return m;
      }
    } catch (Exception e) {} // ignore
    return null;
  }

  /**
   * Escapes $ in regexps as it is not meant for end-line matching, but inner class matches.
   * Impl.is weird as the String methods are not available in 1.4
   */
  private static String escapeRegexp(String regex) {
    if(regex.indexOf('$') == -1) return regex;
    String[] fragments= regex.split("\\$");
    StringBuffer result= new StringBuffer();
    for(int i= 0; i < fragments.length - 1; i++) {
      result.append(fragments[i]).append("\\$");
    }
    result.append(fragments[fragments.length - 1]);
    if(regex.endsWith("$")) result.append("\\$");
    
    return result.toString();
  }
  
  /**
   * Read the expected exceptions, if any (need to handle both the old and new
   * syntax)
   */
  public static ExpectedExceptionsHolder findExpectedExceptions(IAnnotationFinder finder,
      Method method) {
    ExpectedExceptionsHolder result = null;
    IExpectedExceptionsAnnotation expectedExceptions= 
      (IExpectedExceptionsAnnotation) finder.findAnnotation(method,
        IExpectedExceptionsAnnotation.class);
    // Old syntax
    if (expectedExceptions != null) {
      result = new ExpectedExceptionsHolder(expectedExceptions.getValue(), ".*");
    }
    else {
      // New syntax
      ITestAnnotation testAnnotation = 
        (ITestAnnotation) finder.findAnnotation(method, ITestAnnotation.class);
      if (testAnnotation != null) {
        Class<?>[] ee = testAnnotation.getExpectedExceptions();
        if (testAnnotation != null && ee.length > 0) {
          result = new ExpectedExceptionsHolder(ee,
              testAnnotation.getExpectedExceptionsMessageRegExp());
        }
      }
    }
    
    return result;
  }
  
  //
  // End of public methods
  // ///

  public static boolean isEnabled(Class<?> objectClass, IAnnotationFinder finder) {
    ITestAnnotation testClassAnnotation= AnnotationHelper.findTest(finder, objectClass);

    return isEnabled(testClassAnnotation);
  }
  
  public static boolean isEnabled(Method m, IAnnotationFinder finder) {
    ITestAnnotation annotation = AnnotationHelper.findTest(finder, m);
    
    // If no method annotation, look for one on the class
    if (null == annotation) {
      annotation = AnnotationHelper.findTest(finder, m.getDeclaringClass());
    }

    return isEnabled(annotation);   
  }
  
  public static boolean isEnabled(ITestOrConfiguration test) {
    return null == test || (null != test && test.getEnabled());
  }
  
  public static ITestNGMethod[] findMethodsThatBelongToGroup(
      ITestNGMethod method,
      List<ITestNGMethod> methods, String groupRegexp) 
  {
    ITestNGMethod[] allMethods = methods.toArray(new ITestNGMethod[methods
        .size()]);
    return findMethodsThatBelongToGroup(method, allMethods, groupRegexp);
  }
  
  /**
   * @return The transitive closure of all the groups/methods included.
   */
  public static void findGroupTransitiveClosure(XmlMethodSelector xms,
      List<ITestNGMethod> includedMethods,
      List<ITestNGMethod> allMethods,
      String[] includedGroups,
      Set<String> outGroups, Set<ITestNGMethod> outMethods)
  {
    Map<ITestNGMethod, ITestNGMethod> runningMethods = Maps.newHashMap();
    for (ITestNGMethod m : includedMethods) {
      runningMethods.put(m, m);
    }
    
    Map<String, String> runningGroups = Maps.newHashMap();
    for (String thisGroup : includedGroups) {
      runningGroups.put(thisGroup, thisGroup);
    }
    
    boolean keepGoing = true;
    
    Map<ITestNGMethod, ITestNGMethod> newMethods = Maps.newHashMap();
    while (keepGoing) {
      for (ITestNGMethod m : includedMethods) {
        
        //
        // Depends on groups?
        // Adds all included methods to runningMethods
        //
        String[] ig = m.getGroupsDependedUpon();
        for (String g : ig) {
          if (! runningGroups.containsKey(g)) {
            // Found a new included group, add all the methods it contains to
            // our outMethod closure
            runningGroups.put(g, g);
            ITestNGMethod[] im = 
              findMethodsThatBelongToGroup(m, allMethods, g);
            for (ITestNGMethod thisMethod : im) {
              if (! runningMethods.containsKey(thisMethod)) {
                runningMethods.put(thisMethod, thisMethod);
                newMethods.put(thisMethod, thisMethod);
              }
            }
          }
        } // groups
          
        //
        // Depends on methods?
        // Adds all depended methods to runningMethods
        //
        String[] mdu = m.getMethodsDependedUpon();
        for (String tm : mdu) {
          ITestNGMethod thisMethod = findMethodNamed(tm, allMethods);
          if (thisMethod != null && ! runningMethods.containsKey(thisMethod)) {
            runningMethods.put(thisMethod, thisMethod);
            newMethods.put(thisMethod, thisMethod);
          }
        }
          
      } // methods
      
      //
      // Only keep going if new methods have been added
      //
      keepGoing = newMethods.size() > 0;
      includedMethods = Lists.newArrayList();
      includedMethods.addAll(newMethods.keySet());
      newMethods = Maps.newHashMap();
    } // while keepGoing
    
    outMethods.addAll(runningMethods.keySet());
    outGroups.addAll(runningGroups.keySet());
  }
  
  /**
   * Extracts the map of groups and their corresponding methods from the <code>classes</code>.
   */
  public static Map<String, List<ITestNGMethod>> findGroupsMethods(Collection<ITestClass> classes, boolean before) {
    Map<String, List<ITestNGMethod>> result = Maps.newHashMap();
    for (ITestClass cls : classes) {
      ITestNGMethod[] methods = before ? cls.getBeforeGroupsMethods() : cls.getAfterGroupsMethods();
      for (ITestNGMethod method : methods) {
        for (String group : before ? method.getBeforeGroups() : method.getAfterGroups()) {
          List<ITestNGMethod> methodList = result.get(group);
          if (methodList == null) {
            methodList = Lists.newArrayList();
            result.put(group, methodList);
          }
          // NOTE(cbeust, 2007/01/23)
          // BeforeGroups/AfterGroups methods should only be invoked once.
          // I should probably use a map instead of a list for a contains(), but 
          // this list should usually be fairly short
          if (! methodList.contains(method)) {
            methodList.add(method);
          }
        }
      }
    }
    
    return result;
  }
  
  /**
   * Extracts the unique list of <code>ITestNGMethod</code>s.
   */
  public static List<ITestNGMethod> uniqueMethodList(Collection<List<ITestNGMethod>> methods) {
    Map<ITestNGMethod, ITestNGMethod> uniq = Maps.newHashMap();
    
    for (List<ITestNGMethod> l : methods) {
      for (ITestNGMethod m : l) {
        uniq.put(m, m);
      }
    }
    
    List<ITestNGMethod> result = Lists.newArrayList();
    result.addAll(uniq.values());
    
    return result;
  }
  
  private static ITestNGMethod findMethodNamed(String tm, List<ITestNGMethod> allMethods) {
    for (ITestNGMethod m : allMethods) {
      // TODO(cbeust):  account for package
      String methodName = 
        m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName();
      if (methodName.equals(tm)) return m;
    }
    
    return null;
  }
  
  private static boolean includeMethod(ITestOrConfiguration annotation,
      RunInfo runInfo, ITestNGMethod tm, boolean forTests, boolean unique, List<ITestNGMethod> outIncludedMethods)
  {
    boolean result = false;
    
    if (isEnabled(annotation)) {
      if (runInfo.includeMethod(tm, forTests)) {
        if (unique) {
          if (!isMethodAlreadyPresent(outIncludedMethods, tm)) {
            result = true;
          }
        }
        else {
          result = true;
        }
      }
    }
    
    return result;
  }

  /**
   * Collect all the methods that belong to the included groups and exclude all
   * the methods that belong to an excluded group.
   */
  private static void collectMethodsByGroup(ITestNGMethod[] methods,
      boolean forTests, 
      List<ITestNGMethod> outIncludedMethods, 
      List<ITestNGMethod> outExcludedMethods, 
      RunInfo runInfo,
      IAnnotationFinder finder, boolean unique) 
  {
    for (ITestNGMethod tm : methods) {
      boolean in = false;
      Method m = tm.getMethod();
      //
      // @Test method
      //
      if (forTests) {
        in = includeMethod(AnnotationHelper.findTest(finder, m),
            runInfo, tm, forTests, unique, outIncludedMethods);
      }
      
      //
      // @Configuration method
      //
      else {
        IConfigurationAnnotation annotation = AnnotationHelper.findConfiguration(finder, m);
        if (annotation.getAlwaysRun()) {
          in = true;
        }
        else {
          in = includeMethod(AnnotationHelper.findTest(finder, m),
              runInfo, tm, forTests, unique, outIncludedMethods);
        }
      }
      if (in) {
        outIncludedMethods.add(tm);
      }
      else {
        outExcludedMethods.add(tm);
      }
    }
  }

  /**
   * @param result
   * @param tm
   * @return true if a method by a similar name (and same hierarchy) already
   *         exists
   */
  private static boolean isMethodAlreadyPresent(List<ITestNGMethod> result,
      ITestNGMethod tm) {
    for (ITestNGMethod m : result) {
      Method jm1 = m.getMethod();
      Method jm2 = tm.getMethod();
      if (jm1.getName().equals(jm2.getName())) {
        // Same names, see if they are in the same hierarchy
        Class<?> c1 = jm1.getDeclaringClass();
        Class<?> c2 = jm2.getDeclaringClass();
        if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1)) {
          return true;
        }
      }
    }

    return false;
  }

  static public Graph<ITestNGMethod> topologicalSort(ITestNGMethod[] methods,
      List<ITestNGMethod> sequentialList, List<ITestNGMethod> parallelList) {
    Graph<ITestNGMethod> result = new Graph<ITestNGMethod>();

    if (methods.length == 0) return result;
    
    //
    // Create the graph
    //
    for (ITestNGMethod m : methods) {
      result.addNode(m);

      Map<ITestNGMethod, ITestNGMethod> predecessors = Maps.newHashMap();

      String[] methodsDependedUpon = m.getMethodsDependedUpon();
      String[] groupsDependedUpon = m.getGroupsDependedUpon();
      if (methodsDependedUpon.length > 0) {
        ITestNGMethod[] methodsNamed = 
          MethodHelper.findMethodsNamed(m, methods, methodsDependedUpon);
        for (ITestNGMethod pred : methodsNamed) {
          predecessors.put(pred, pred);
        }
      }
      if (groupsDependedUpon.length > 0) {
        for (String group : groupsDependedUpon) {
          ITestNGMethod[] methodsThatBelongToGroup = 
            MethodHelper.findMethodsThatBelongToGroup(m, methods, group);
          for (ITestNGMethod pred : methodsThatBelongToGroup) {
            predecessors.put(pred, pred);
          }
        }
      }

      for (ITestNGMethod predecessor : predecessors.values()) {
        // ppp("METHOD:" + m + " ADDED PREDECESSOR:" + predecessor);
        result.addPredecessor(m, predecessor);
      }
    }

    result.topologicalSort();
    sequentialList.addAll(result.getStrictlySortedNodes());
    parallelList.addAll(result.getIndependentNodes());

    return result;
  }

  public static String calculateMethodCanonicalName(ITestNGMethod m) {
    return calculateMethodCanonicalName(m.getMethod());
  }
  
  private static String calculateMethodCanonicalName(Method m) {
    String packageName = m.getDeclaringClass().getName() + "." + m.getName(); 
    
    // Try to find the method on this class or parents
    Class<?> cls = m.getDeclaringClass();
    while (cls != Object.class) {
      try {
        if (cls.getDeclaredMethod(m.getName(), m.getParameterTypes()) != null) {
          packageName = cls.getName();
          break;
        }
      }
      catch (Exception e) {
        // ignore
      }    
      cls = cls.getSuperclass();
    }
    
    String result = packageName + "." + m.getName();
    return result;
  }

  private static List<ITestNGMethod> sortMethods(boolean forTests, 
      List<ITestNGMethod> allMethods, IAnnotationFinder finder) {
    List<ITestNGMethod> sl = Lists.newArrayList();
    List<ITestNGMethod> pl = Lists.newArrayList();
    ITestNGMethod[] allMethodsArray = allMethods.toArray(new ITestNGMethod[allMethods.size()]);

    // Fix the method inheritance if these are @Configuration methods to make
    // sure base classes are invoked before child classes if 'before' and the
    // other way around if they are 'after'
    if (!forTests && allMethodsArray.length > 0) {
      ITestNGMethod m = allMethodsArray[0];
      boolean before = m.isBeforeClassConfiguration()
          || m.isBeforeMethodConfiguration() || m.isBeforeSuiteConfiguration()
          || m.isBeforeTestConfiguration();
      MethodInheritance.fixMethodInheritance(allMethodsArray, before);
    }

    topologicalSort(allMethodsArray, sl, pl);

    List<ITestNGMethod> result = Lists.newArrayList();
    result.addAll(sl);
    result.addAll(pl);
    return result;
  }

  public static void ppp(String s) {
    System.out.println("[MethodHelper] " + s);
  }

  /**
   * @param method
   * @param allTestMethods
   * @return A sorted array containing all the methods 'method' depends on
   */
  public static List<ITestNGMethod> getMethodsDependedUpon(ITestNGMethod method, ITestNGMethod[] methods) {
    List<ITestNGMethod> parallelList = Lists.newArrayList();
    List<ITestNGMethod> sequentialList = Lists.newArrayList();
    Graph<ITestNGMethod> g = topologicalSort(methods, sequentialList, parallelList);

    List<ITestNGMethod> result = g.findPredecessors(method);
    return result;
  }

  public static Object invokeMethod(Method thisMethod, Object instance, Object[] parameters) 
  throws InvocationTargetException, IllegalAccessException 
  {
    Object result = null;
    // TESTNG-326, allow IObjectFactory to load from non-standard classloader
    // If the instance has a different classloader, its class won't match the method's class
    if (!thisMethod.getDeclaringClass().isAssignableFrom(instance.getClass())) {
      // for some reason, we can't call this method on this class
      // is it static?
      boolean isStatic = Modifier.isStatic(thisMethod.getModifiers());
      if (!isStatic) {
        // not static, so grab a method with the same name and signature in this case
        Class<?> clazz = instance.getClass();
        try {
          thisMethod = clazz.getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
        } catch (Exception e) {
          // ignore, the method may be private
          boolean found = false;
          for (; clazz != null; clazz = clazz.getSuperclass()) {
            try {
              thisMethod = clazz.getDeclaredMethod(thisMethod.getName(), thisMethod.getParameterTypes());
              found = true;
              break;
            } catch (Exception e2) {}
          }
          if (!found) {
            //should we assert here?  Or just allow it to fail on invocation?
            if (thisMethod.getDeclaringClass().getName().equals(instance.getClass().getName())) {
              throw new RuntimeException("Can't invoke method " + thisMethod + ", probably due to classloader mismatch");
            }
            throw new RuntimeException("Can't invoke method " + thisMethod + " on this instance of " + instance.getClass()+ " due to class mismatch");
          }
        }
      }
      
      
      
    }

    boolean isPublic = Modifier.isPublic(thisMethod.getModifiers());

    try {
      if (!isPublic) {
        thisMethod.setAccessible(true);
      }
      result = thisMethod.invoke(instance, parameters);
    }
    finally {
      if (!isPublic) {
        thisMethod.setAccessible(false);
      }
    }

    return result;
  }

  public static Iterator<Object[]> createArrayIterator(final Object[][] objects) {
    ArrayIterator result = new ArrayIterator(objects);
    return result;
  }

  public static Iterator<Object[]> invokeDataProvider(Object instance, 
      Method dataProvider, ITestNGMethod method, ITestContext testContext,
      Object fedInstance, IAnnotationFinder annotationFinder)
  {
    Iterator<Object[]> result = null;
    Method testMethod = method.getMethod();

    // If it returns an Object[][], convert it to an Iterable<Object[]>
    try {
      List<Object> lParameters = Lists.newArrayList();

      // Go through all the parameters declared on this Data Provider and
      // make sure we have at most one Method and one ITestContext.
      // Anything else is an error
      Class<?>[] parameterTypes = dataProvider.getParameterTypes();
      if (parameterTypes.length > 2) {
        throw new TestNGException("DataProvider " + dataProvider + " cannot have more than two parameters");
      }
      
      int i = 0;
      for (Class<?> cls : parameterTypes) {
        boolean isTestInstance = annotationFinder.hasTestInstance(dataProvider, i++); 
        if (cls.equals(Method.class)) {
          lParameters.add(testMethod);
        }
        else if (cls.equals(ITestContext.class)) {
          lParameters.add(testContext);
        }
        else if (isTestInstance) {
          lParameters.add(fedInstance);
        }
      }
      Object[] parameters = lParameters.toArray(new Object[lParameters.size()]);
      
      Class< ? > returnType = dataProvider.getReturnType();
      if (Object[][].class.isAssignableFrom(returnType)) {
        Object[][] oResult = (Object[][]) MethodHelper.invokeMethod(
            dataProvider, instance, parameters);
        method.setParameterInvocationCount(oResult.length);
        result = MethodHelper.createArrayIterator(oResult);
      }
      else if (Iterator.class.isAssignableFrom(returnType)) {
        // Already an Iterable<Object[]>, assign it directly
        result = (Iterator<Object[]>) MethodHelper.invokeMethod(dataProvider,
            instance, parameters);
      }
      else {
        throw new TestNGException("Data Provider " + dataProvider
            + " must return" + " either Object[][] or Iterator<Object>[], not "
            + returnType);
      }
    }
    catch (InvocationTargetException e) {
      throw new TestNGException(e);
    }
    catch (IllegalAccessException e) {
      throw new TestNGException(e);
    }

    return result;
  }

  public static String calculateMethodCanonicalName(Class<?> methodClass, String methodName) {
    Set<Method> methods = ClassHelper.getAvailableMethods(methodClass); // TESTNG-139
    Method result = null;
    for (Method m : methods) {
      if (methodName.equals(m.getName())) {
        result = m;
        break;
      }
    }
    
    return result != null ? calculateMethodCanonicalName(result) : null;
  }

  /**
   * Invokes the <code>run</code> method of the <code>IHookable</code>.
   * 
   * @param instance the instance to invoke the method in
   * @param parameters the parameters to be passed to <code>IHookCallBack</code>
   * @param testClass the test class
   * @param thisMethod the method to be invoked through the <code>IHookCallBack</code>
   * @param testResult the current <code>ITestResult</code> passed to <code>IHookable.run</code>
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws Throwable thrown if the reflective call to <tt>thisMethod</code> results in an exception
   */
  public static void invokeHookable(final Object instance, 
                                    final Object[] parameters, 
                                    ITestClass testClass, 
                                    final Method thisMethod, 
                                    TestResult testResult) 
  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, Throwable 
  {
    Method runMethod = testClass.getRealClass().getMethod("run", 
        new Class[] { IHookCallBack.class, ITestResult.class });
    final Throwable[] error = new Throwable[1];
    
    IHookCallBack callback = new IHookCallBack() {
      public void runTestMethod(ITestResult tr) {
        try {
          invokeMethod(thisMethod, instance, parameters);
         }
         catch(Throwable t) {
           error[0] = t;
           tr.setThrowable( t ); // make Throwable available to IHookable
         }
       }
    };
    runMethod.invoke(instance, new Object[]{callback, testResult});
    if (error[0] != null) {
      throw error[0];
    }
  }
  
  public static long calculateTimeOut(ITestNGMethod tm) {
    long result = tm.getTimeOut() > 0 ? tm.getTimeOut() : tm.getInvocationTimeOut();
    return result;
  }

  /**
   * Invokes a method on a separate thread in order to allow us to timeout the invocation.
   * It uses as implementation an <code>Executor</code> and a <code>CountDownLatch</code>.
   * @param tm the 
   * @param instance
   * @param parameterValues
   * @param testResult
   * @throws InterruptedException
   * @throws ThreadExecutionException
   */
  public static void invokeWithTimeout(ITestNGMethod tm, Object instance, Object[] parameterValues, 
      ITestResult testResult) 
  throws InterruptedException, ThreadExecutionException {
//    ICountDown done= ThreadUtil.createCountDown(1);
//    IThreadFactory factory= ThreadUtil.createFactory();
    IExecutor exec= ThreadUtil.createExecutor(1, tm.getMethod().getName());
  
    InvokeMethodRunnable imr = new InvokeMethodRunnable(tm, instance, parameterValues);
    IFutureResult future= exec.submitRunnable(imr);
    exec.shutdown();
    long realTimeOut = calculateTimeOut(tm);
    boolean finished= exec.awaitTermination(realTimeOut);
  
    if(!finished) {
      exec.stopNow();
      ThreadTimeoutException exception = new ThreadTimeoutException("Method "
        + tm.getMethod()
        + " didn't finish within the time-out "
        + realTimeOut);
      exception.setStackTrace(exec.getStackTraces()[0]);
      testResult.setThrowable(exception);
      testResult.setStatus(ITestResult.FAILURE);
    }
    else {
      Utils.log("Invoker " + Thread.currentThread().hashCode(), 3, 
          "Method " + tm.getMethod() + " completed within the time-out " + tm.getTimeOut());
      
      // We don't need the result from the future but invoking get() on it
      // will trigger the exception that was thrown, if any
      future.get();
//      done.await();
  
      testResult.setStatus(ITestResult.SUCCESS); // if no exception till here than SUCCESS
    }
  }
}

// ///

class ArrayIterator implements Iterator {
  private Object[][] m_objects;

  private int m_count;

  public ArrayIterator(Object[][] objects) {
    m_objects = objects;
    m_count = 0;
  }

  public boolean hasNext() {
    return m_count < m_objects.length;
  }

  public Object next() {
    return m_objects[m_count++];
  }

  public void remove() {
    // TODO Auto-generated method stub

  }

}
