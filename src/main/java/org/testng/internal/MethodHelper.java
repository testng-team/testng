package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.annotations.IExpectedExceptionsAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.collections.Pair;

/**
 * Collection of helper methods to help sort and arrange methods.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class MethodHelper {
  private static final Map<ITestNGMethod[], Graph<ITestNGMethod>> GRAPH_CACHE =
          new ConcurrentHashMap<>();
  private static final Map<Method, String> CANONICAL_NAME_CACHE = new ConcurrentHashMap<>();
  private static final Map<Pair<String, String>, Boolean> MATCH_CACHE =
          new ConcurrentHashMap<>();

  /**
   * Collects and orders test or configuration methods
   * @param methods methods to be worked on
   * @param forTests true for test methods, false for configuration methods
   * @param runInfo
   * @param finder annotation finder
   * @param unique true for unique methods, false otherwise
   * @param outExcludedMethods
   * @return list of ordered methods
   */
  public static ITestNGMethod[] collectAndOrderMethods(List<ITestNGMethod> methods,
      boolean forTests, RunInfo runInfo, IAnnotationFinder finder,
      boolean unique, List<ITestNGMethod> outExcludedMethods)
  {
    List<ITestNGMethod> includedMethods = Lists.newArrayList();
    MethodGroupsHelper.collectMethodsByGroup(methods.toArray(new ITestNGMethod[methods.size()]),
        forTests,
        includedMethods,
        outExcludedMethods,
        runInfo,
        finder,
        unique);

    return sortMethods(forTests, includedMethods, finder).toArray(new ITestNGMethod[]{});
  }

  /**
   * Finds TestNG methods that the specified TestNG method depends upon
   * @param m TestNG method
   * @param methods list of methods to search for depended upon methods
   * @return list of methods that match the criteria
   */
  protected static ITestNGMethod[] findDependedUponMethods(ITestNGMethod m,
      ITestNGMethod[] methods)
  {
    String canonicalMethodName = calculateMethodCanonicalName(m);
    List<ITestNGMethod> vResult = Lists.newArrayList();
    String regexp = null;
    for (String fullyQualifiedRegexp : m.getMethodsDependedUpon()) {
      boolean foundAtLeastAMethod = false;

      if (null != fullyQualifiedRegexp) {
        // Escapes $ in regexps as it is not meant for end - line matching, but inner class matches.
        regexp = fullyQualifiedRegexp.replace("$", "\\$");
        boolean usePackage = regexp.indexOf('.') != -1;
        Pattern pattern = Pattern.compile(regexp);

        for (ITestNGMethod method : methods) {
          Method thisMethod = method.getMethod();
          String thisMethodName = thisMethod.getName();
          String methodName = usePackage ?
              calculateMethodCanonicalName(thisMethod)
              : thisMethodName;
          Pair<String, String> cacheKey = Pair.create(regexp, methodName);
          Boolean match = MATCH_CACHE.get(cacheKey);
          if (match == null) {
              match = pattern.matcher(methodName).matches();
              MATCH_CACHE.put(cacheKey, match);
          }
          if (match) {
            vResult.add(method);
            foundAtLeastAMethod = true;
          }
        }
      }

      if (!foundAtLeastAMethod) {
        if (m.ignoreMissingDependencies()) {
          continue;
        }
        if (m.isAlwaysRun()) {
          continue;
        }
        Method maybeReferringTo = findMethodByName(m, regexp);
        if (maybeReferringTo != null) {
          throw new TestNGException(canonicalMethodName + "() is depending on method "
              + maybeReferringTo + ", which is not annotated with @Test or not included.");
        }
        throw new TestNGException(canonicalMethodName
            + "() depends on nonexistent method " + regexp);
      }
    }//end for

    return vResult.toArray(new ITestNGMethod[vResult.size()]);
  }

  /**
   * Finds method based on regex and TestNGMethod. If regex doesn't represent the
   * class name, uses the TestNG method's class name.
   * @param testngMethod TestNG method
   * @param regExp regex representing a method and/or related class name
   */
  private static Method findMethodByName(ITestNGMethod testngMethod, String regExp) {
    if (regExp == null) {
      return null;
    }
    int lastDot = regExp.lastIndexOf('.');
    String className, methodName;
    if (lastDot == -1) {
      className = testngMethod.getMethod().getDeclaringClass().getCanonicalName();
      methodName = regExp;
    } else {
      methodName = regExp.substring(lastDot + 1);
      className = regExp.substring(0, lastDot);
    }

    try {
      Class<?> c = Class.forName(className);
      for (Method m : c.getDeclaredMethods()) {
        if (methodName.equals(m.getName())) {
          return m;
        }
      }
    }
    catch (Exception e) {
      //only logging
      Utils.log("MethodHelper", 3, "Caught exception while searching for methods using regex");
    }
    return null;
  }

  protected static boolean isEnabled(Class<?> objectClass, IAnnotationFinder finder) {
    ITestAnnotation testClassAnnotation = AnnotationHelper.findTest(finder, objectClass);
    return isEnabled(testClassAnnotation);
  }

  protected static boolean isEnabled(Method m, IAnnotationFinder finder) {
    ITestAnnotation annotation = AnnotationHelper.findTest(finder, m);

    // If no method annotation, look for one on the class
    if (null == annotation) {
      annotation = AnnotationHelper.findTest(finder, m.getDeclaringClass());
    }

    return isEnabled(annotation);
  }

  protected static boolean isEnabled(ITestOrConfiguration test) {
    return null == test || test.getEnabled();
  }

  /**
   * Extracts the unique list of <code>ITestNGMethod</code>s.
   */
  public static List<ITestNGMethod> uniqueMethodList(Collection<List<ITestNGMethod>> methods) {
    Set<ITestNGMethod> resultSet = Sets.newHashSet();

    for (List<ITestNGMethod> l : methods) {
      resultSet.addAll(l);
    }

    return Lists.newArrayList(resultSet);
  }

  private static Graph<ITestNGMethod> topologicalSort(ITestNGMethod[] methods,
      List<ITestNGMethod> sequentialList, List<ITestNGMethod> parallelList) {
    Graph<ITestNGMethod> result = new Graph<>();

    if (methods.length == 0) {
      return result;
    }

    //
    // Create the graph
    //
    for (ITestNGMethod m : methods) {
      result.addNode(m);

      List<ITestNGMethod> predecessors = Lists.newArrayList();

      String[] methodsDependedUpon = m.getMethodsDependedUpon();
      String[] groupsDependedUpon = m.getGroupsDependedUpon();
      if (methodsDependedUpon.length > 0) {
        ITestNGMethod[] methodsNamed =
          MethodHelper.findDependedUponMethods(m, methods);
        for (ITestNGMethod pred : methodsNamed) {
          predecessors.add(pred);
        }
      }
      if (groupsDependedUpon.length > 0) {
        for (String group : groupsDependedUpon) {
          ITestNGMethod[] methodsThatBelongToGroup =
            MethodGroupsHelper.findMethodsThatBelongToGroup(m, methods, group);
          for (ITestNGMethod pred : methodsThatBelongToGroup) {
            predecessors.add(pred);
          }
        }
      }

      for (ITestNGMethod predecessor : predecessors) {
        result.addPredecessor(m, predecessor);
      }
    }

    result.topologicalSort();
    sequentialList.addAll(result.getStrictlySortedNodes());
    parallelList.addAll(result.getIndependentNodes());

    return result;
  }

  protected static String calculateMethodCanonicalName(ITestNGMethod m) {
    return calculateMethodCanonicalName(m.getMethod());
  }

  private static String calculateMethodCanonicalName(Method m) {
    String result = CANONICAL_NAME_CACHE.get(m);
    if (result != null) {
      return result;
    }

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

    result = packageName + "." + m.getName();
    CANONICAL_NAME_CACHE.put(m, result);
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

  /**
   * @return A sorted array containing all the methods 'method' depends on
   */
  public static List<ITestNGMethod> getMethodsDependedUpon(ITestNGMethod method, ITestNGMethod[] methods) {
    Graph<ITestNGMethod> g = GRAPH_CACHE.get(methods);
    if (g == null) {
      List<ITestNGMethod> parallelList = Lists.newArrayList();
      List<ITestNGMethod> sequentialList = Lists.newArrayList();
      g = topologicalSort(methods, sequentialList, parallelList);
      GRAPH_CACHE.put(methods, g);
    }

    List<ITestNGMethod> result = g.findPredecessors(method);
    return result;
  }

  protected static Iterator<Object[]> createArrayIterator(final Object[][] objects) {
    ArrayIterator result = new ArrayIterator(objects);
    return result;
  }

  protected static String calculateMethodCanonicalName(Class<?> methodClass, String methodName) {
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

  protected static long calculateTimeOut(ITestNGMethod tm) {
    long result = tm.getTimeOut() > 0 ? tm.getTimeOut() : tm.getInvocationTimeOut();
    return result;
  }
}

/**
 * Custom iterator class over a 2D array
 *
 */
class ArrayIterator implements Iterator<Object[]> {
  private Object[][] m_objects;
  private int m_count;

  public ArrayIterator(Object[][] objects) {
    m_objects = objects;
    m_count = 0;
  }

  @Override
  public boolean hasNext() {
    return m_count < m_objects.length;
  }

  @Override
  public Object[] next() {
    if (m_count >= m_objects.length) {
      throw new NoSuchElementException();
    }
    return m_objects[m_count++];
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove operation is not supported on this iterator");
  }

}
