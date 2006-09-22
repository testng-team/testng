package org.testng.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.TestRunner;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotation;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IConfiguration;
import org.testng.internal.annotations.ITest;
import org.testng.internal.annotations.ITestOrConfiguration;

/**
 * Collection of helper methods to help sort and arrange methods.
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class MethodHelper {
  static private boolean m_quiet = true;

  // ///
  // public methods
  //

  public static ITestNGMethod[] collectAndOrderMethods(
      List<ITestNGMethod> methods, boolean forTests, RunInfo runInfo,
      IAnnotationFinder finder, List<ITestNGMethod> outExcludedMethods) 
  {
    return collectAndOrderMethods((ITestNGMethod[]) methods
        .toArray(new ITestNGMethod[methods.size()]), forTests, runInfo, finder,
        false /* unique */, outExcludedMethods);
  }

  public static ITestNGMethod[] collectAndOrderMethods(
      List<ITestNGMethod> methods, boolean forTests, RunInfo runInfo,
      IAnnotationFinder finder, boolean unique,
      List<ITestNGMethod> outExcludedMethods) 
  {
    return collectAndOrderMethods((ITestNGMethod[]) methods
        .toArray(new ITestNGMethod[methods.size()]), forTests, runInfo, finder,
        unique,
        outExcludedMethods);
  }

  public static ITestNGMethod[] collectAndOrderMethods(ITestNGMethod[] methods,
      boolean forTests, RunInfo runInfo, IAnnotationFinder finder,
      List<ITestNGMethod> outExcludedMethods) 
  {
    return collectAndOrderMethods(methods, forTests, runInfo, finder, 
        false /* unique */, outExcludedMethods);
  }

  public static ITestNGMethod[] collectAndOrderMethods(ITestNGMethod[] methods,
      boolean forTests, RunInfo runInfo, IAnnotationFinder finder,
      boolean unique, List<ITestNGMethod> outExcludedMethods) 
  {
    List<ITestNGMethod> includedMethods = new ArrayList<ITestNGMethod>();
    collectMethodsByGroup(methods, forTests, 
        includedMethods, outExcludedMethods, 
        runInfo, finder, unique);
    List<ITestNGMethod> vResult = sortMethods(forTests, includedMethods, finder);

    ITestNGMethod[] result = vResult.toArray(new ITestNGMethod[vResult.size()]);

    // Method[] result = new Method[vResult.size()];
    // for (int i = 0; i < vResult.size(); i++) {
    // result[i] = vResult.get(i).getMethod();
    // }

    return result;
  }

  /**
   * @param methods
   * @return All the methods that match the filtered groups. If a method belongs
   *         to an excluded group, it is automatically excluded.
   */
  public static ITestNGMethod[] collectAndOrderConfigurationMethods(
      ITestNGMethod[] methods, RunInfo runInfo, IAnnotationFinder finder,
      boolean unique, List<ITestNGMethod> outExcludedMethods) 
  {
    return collectAndOrderMethods(methods, false /* forTests */, runInfo,
        finder, unique, outExcludedMethods);
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
    List<ITestNGMethod> vResult = new ArrayList<ITestNGMethod>();
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

  public static ITestNGMethod[] findMethodsNamed(String mainMethod,
      ITestNGMethod[] methods, String[] regexps) 
  {
    List<ITestNGMethod> vResult = new ArrayList<ITestNGMethod>();
    String currentRegexp = null;
    for (String fullyQualifiedRegexp : regexps) {
//      int ind = fullyQualifiedRegexp.lastIndexOf(".");
//      String regexp = ind >= 0 ? fullyQualifiedRegexp.substring(ind + 1) : fullyQualifiedRegexp;
      String regexp = fullyQualifiedRegexp;
      boolean foundAtLeastAMethod = false;
      if (regexp != null) {
        currentRegexp = regexp;
        boolean usePackage = regexp.indexOf(".") != -1;
        for (ITestNGMethod method : methods) {
          Method thisMethod = method.getMethod();
          String thisMethodName = thisMethod.getName();
          String methodName = usePackage ?
              calculateMethodCanonicalName(thisMethod)
              : thisMethodName;
//          ppp("COMPARING\n" + regexp + "\n" + methodName);
          if (Pattern.matches(regexp, methodName)) {
            vResult.add(method);
            foundAtLeastAMethod = true;
          }
        }
      }

      if (!foundAtLeastAMethod) {
        throw new TestNGException(mainMethod
            + "() is depending on nonexistent method " + currentRegexp);
      }
    }

    ITestNGMethod[] result = vResult.toArray(new ITestNGMethod[vResult.size()]);

    return result;
  }

  //
  // End of public methods
  // ///

  public static boolean isEnabled(Class objectClass, IAnnotationFinder finder) {
    ITest testClassAnnotation= AnnotationHelper.findTest(finder, objectClass);

    return isEnabled(testClassAnnotation);
  }
  
  public static boolean isEnabled(Method m, IAnnotationFinder finder) {
    ITest annotation = AnnotationHelper.findTest(finder, m);
    
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
    Map<ITestNGMethod, ITestNGMethod> runningMethods =
      new HashMap<ITestNGMethod, ITestNGMethod>();
    for (ITestNGMethod m : includedMethods) {
      runningMethods.put(m, m);
    }
    
    Map<String, String> runningGroups = new HashMap<String, String>();
    for (String thisGroup : includedGroups) {
      runningGroups.put(thisGroup, thisGroup);
    }
    
    boolean keepGoing = true;
    
    Map<ITestNGMethod, ITestNGMethod> newMethods = 
      new HashMap<ITestNGMethod, ITestNGMethod>();
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
      includedMethods = new ArrayList<ITestNGMethod>();
      includedMethods.addAll(newMethods.keySet());
      newMethods = new HashMap<ITestNGMethod, ITestNGMethod>();
    } // while keepGoing
    
    outMethods.addAll(runningMethods.keySet());
    outGroups.addAll(runningGroups.keySet());
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
      RunInfo runInfo, ITestNGMethod tm, 
      boolean forTests, boolean unique,
      List<ITestNGMethod> outIncludedMethods)
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
        IConfiguration annotation = AnnotationHelper.findConfiguration(finder, m);
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
        Class c1 = jm1.getDeclaringClass();
        Class c2 = jm2.getDeclaringClass();
        if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1)) {
          return true;
        }
      }
    }

    return false;
  }

  static public Graph topologicalSort(ITestNGMethod[] methods,
      List<ITestNGMethod> sequentialList, List<ITestNGMethod> parallelList) {
    Graph<ITestNGMethod> result = new Graph<ITestNGMethod>();

    //
    // Create the graph
    //
    for (ITestNGMethod m : methods) {
      result.addNode(m);

      Map<ITestNGMethod, ITestNGMethod> predecessors = new HashMap<ITestNGMethod, ITestNGMethod>();

      String[] methodsDependedUpon = m.getMethodsDependedUpon();
      String[] groupsDependedUpon = m.getGroupsDependedUpon();
      if (methodsDependedUpon.length > 0) {
        String methodName = calculateMethodCanonicalName(m);
        ITestNGMethod[] methodsNamed = 
          MethodHelper.findMethodsNamed(methodName, methods, methodsDependedUpon);
        for (ITestNGMethod pred : methodsNamed) {
          predecessors.put(pred, pred);
        }
      }
      if (groupsDependedUpon.length > 0) {
        for (String group : groupsDependedUpon) {
          ITestNGMethod[] methodsThatBelongToGroup = MethodHelper
              .findMethodsThatBelongToGroup(m, methods, group);
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
  
  public static String calculateMethodCanonicalName(Method m) {
    String packageName = m.getDeclaringClass().getName() + "." + m.getName(); 
    
    // Try to find the method on this class or parents
    Class cls = m.getDeclaringClass();
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
    List<ITestNGMethod> sl = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> pl = new ArrayList<ITestNGMethod>();
    ITestNGMethod[] allMethodsArray = allMethods
        .toArray(new ITestNGMethod[allMethods.size()]);

    // Fix the method inheritance if these are @Configuration methods to make
    // sure base classes are invoked before child classes if 'before' and the
    // other
    // way around if they are 'after'
    if (!forTests && allMethodsArray.length > 0) {
      ITestNGMethod m = allMethodsArray[0];
      boolean before = m.isBeforeClassConfiguration()
          || m.isBeforeMethodConfiguration() || m.isBeforeSuiteConfiguration()
          || m.isBeforeTestConfiguration();
      MethodInheritance.fixMethodInheritance(allMethodsArray, before);
    }

    topologicalSort(allMethodsArray, sl, pl);

    List<ITestNGMethod> result = new ArrayList<ITestNGMethod>();
    result.addAll(sl);
    result.addAll(pl);
    return result;
  }

  /**
   * Perform a topological sort on the methods based on their dependents.
   */
  // private static List<ITestNGMethod> sortMethods(boolean forTests,
  // List<ITestNGMethod> allMethods,
  // IAnnotationFinder finder)
  // {
  // Graph g = new Graph<ITestNGMethod>();
  // for(ITestNGMethod tm : allMethods) {
  //      
  // // Add this ITestNGMethod as a node
  // g.addNode(tm);
  //      
  // // If it has any dependents, add them as predecessors on the graph
  // String[] groups = {};
  // Method m = tm.getMethod();
  // if (forTests) {
  // groups = Utils.dependentGroupsForThisMethodForTest(m, finder);
  // }
  // else {
  // groups = Utils.dependentGroupsForThisMethodForConfiguration(m, finder);
  // }
  // for (String group : groups) {
  // ITestNGMethod[] dependentMethods = getMethodsThatBelongToGroup(allMethods,
  // group);
  // for (ITestNGMethod dependentMethod : dependentMethods) {
  // g.addPredecessor(tm, dependentMethod);
  // }
  // }
  // }
  //    
  // g.topologicalSort();
  // List<ITestNGMethod> result = new ArrayList<ITestNGMethod>();
  // result.addAll(g.getIndependentNodes());
  // result.addAll(g.getStrictlySortedNodes());
  // if (TestRunner.getVerbose() >= 10) {
  // ppp("BEFORE:"); Utils.dumpMethods(allMethods);
  // ppp("AFTER:"); Utils.dumpMethods(result);
  // }
  // return result;
  // }
  private static void log(int level, String s) {
    if (level <= TestRunner.getVerbose()) {
      ppp(s);
    }
  }

  public static void ppp(String s) {
    System.out.println("[MethodHelper] " + s);
  }

  /**
   * @param method
   * @param allTestMethods
   * @return A sorted array containing all the methods 'method' depends on
   */
  public static List<ITestNGMethod> getMethodsDependedUpon(
      ITestNGMethod method, ITestNGMethod[] methods) {
    List<ITestNGMethod> parallelList = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> sequentialList = new ArrayList<ITestNGMethod>();
    Graph g = topologicalSort(methods, sequentialList, parallelList);

    List<ITestNGMethod> result = g.findPredecessors(method);
    return result;
  }

  public static Object invokeMethod(Method thisMethod, Object instance,
      Object[] parameters) 
  throws InvocationTargetException, IllegalAccessException 
  {
    Object result = null;
    boolean isPublic = Modifier.isPublic(thisMethod.getModifiers());

    try {
      if (!isPublic) {
        thisMethod.setAccessible(true);
      }
      result = thisMethod.invoke(instance, parameters);
    }
//    catch(Exception ex) {
//      if (TestRunner.getVerbose() > 2) {
//        ex.printStackTrace();
//      }
//    }
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
      Method dataProvider, ITestNGMethod method)
   {
    Iterator<Object[]> result = null;
    Method testMethod = method.getMethod();

    // If it returns an Object[][], convert it to an Iterable<Object[]>
    try {
      Object[] parameters = null;
      
      // If the DataProvider accepts a Method as first parameter, pass the
      // current test method
      Class[] parameterTypes = dataProvider.getParameterTypes();
      if (parameterTypes.length > 0 && parameterTypes[0].equals(Method.class)) {
        parameters = new Object[] {
          testMethod
        };
      }
      else if (parameterTypes.length > 0) {
        throw new TestNGException("DataProvider " + dataProvider + " needs to have "
            + " either zero parameters or one parameter of type java.lang.reflect.Method");
      }
      
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
//    catch (InstantiationException e) {
//      throw new TestNGException(e);
//    }
    catch (InvocationTargetException e) {
      throw new TestNGException(e);
    }
    catch (IllegalAccessException e) {
      throw new TestNGException(e);
    }

    return result;
  }

  public static String calculateMethodCanonicalName(Class methodClass, String methodName) {
    Method[] methods = methodClass.getMethods();
    Method result = null;
    for (Method m : methods) {
      if (methodName.equals(m.getName())) {
        result = m;
        break;
      }
    }
    
    return result != null ? calculateMethodCanonicalName(result) : null;
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
