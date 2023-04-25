package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.IMethodInstance;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.collections.Pair;
import org.testng.internal.invokers.IInvocationStatus;
import org.testng.util.TimeUtils;
import org.testng.xml.XmlTest;

/** Collection of helper methods to help sort and arrange methods. */
public class MethodHelper {
  private static final Map<ITestNGMethod[], Graph<ITestNGMethod>> GRAPH_CACHE =
      new ConcurrentHashMap<>();
  private static final Map<Method, String> CANONICAL_NAME_CACHE = new ConcurrentHashMap<>();
  private static final Map<Pair<String, String>, Boolean> MATCH_CACHE = new ConcurrentHashMap<>();

  /**
   * Collects and orders test or configuration methods
   *
   * @param methods methods to be worked on
   * @param forTests true for test methods, false for configuration methods
   * @param runInfo - {@link RunInfo} object.
   * @param finder annotation finder
   * @param unique true for unique methods, false otherwise
   * @param outExcludedMethods - A List of excluded {@link ITestNGMethod} methods.
   * @return list of ordered methods
   */
  public static ITestNGMethod[] collectAndOrderMethods(
      List<ITestNGMethod> methods,
      boolean forTests,
      RunInfo runInfo,
      IAnnotationFinder finder,
      boolean unique,
      List<ITestNGMethod> outExcludedMethods,
      Comparator<ITestNGMethod> comparator) {
    AtomicReference<ITestNGMethod[]> results = new AtomicReference<>();
    List<ITestNGMethod> includedMethods = Lists.newArrayList();
    TimeUtils.computeAndShowTime(
        "MethodGroupsHelper.collectMethodsByGroup()",
        () ->
            MethodGroupsHelper.collectMethodsByGroup(
                methods.toArray(new ITestNGMethod[0]),
                forTests,
                includedMethods,
                outExcludedMethods,
                runInfo,
                finder,
                unique));
    TimeUtils.computeAndShowTime(
        "MethodGroupsHelper.sortMethods()",
        () ->
            results.set(
                sortMethods(forTests, includedMethods, comparator)
                    .toArray(new ITestNGMethod[] {})));
    return results.get();
  }

  /**
   * Finds TestNG methods that the specified TestNG method depends upon
   *
   * @param m TestNG method
   * @param methods list of methods to search for depended upon methods
   * @return list of methods that match the criteria
   */
  protected static ITestNGMethod[] findDependedUponMethods(
      ITestNGMethod m, List<ITestNGMethod> methods) {
    ITestNGMethod[] methodsArray = methods.toArray(new ITestNGMethod[0]);
    return findDependedUponMethods(m, methodsArray);
  }

  /**
   * Finds TestNG methods that the specified TestNG method depends upon
   *
   * @param m TestNG method
   * @param methods list of methods to search for depended upon methods
   * @return list of methods that match the criteria
   */
  public static ITestNGMethod[] findDependedUponMethods(ITestNGMethod m, ITestNGMethod[] methods) {

    String canonicalMethodName = calculateMethodCanonicalName(m);

    List<ITestNGMethod> vResult = Lists.newArrayList();
    String regexp = null;
    for (String fullyQualifiedRegexp : m.getMethodsDependedUpon()) {
      boolean foundAtLeastAMethod = false;

      if (null != fullyQualifiedRegexp) {
        // Escapes $ in regexps as it is not meant for end - line matching, but inner class matches.
        regexp = fullyQualifiedRegexp.replace("$", "\\$");
        MatchResults results = matchMethod(methods, regexp);
        foundAtLeastAMethod = results.foundAtLeastAMethod;
        vResult.addAll(results.matchedMethods);
        if (!foundAtLeastAMethod) {
          // Replace the declaring class name in the dependsOnMethods value with
          // the fully qualified test class name and retry the method matching.
          int lastIndex = regexp.lastIndexOf('.');
          String newMethodName;
          if (lastIndex != -1) {
            String clazzName =
                (m.getTestClass() != null ? m.getTestClass().getRealClass() : m.getRealClass())
                    .getName();
            newMethodName = clazzName + regexp.substring(lastIndex);
            results = matchMethod(methods, newMethodName);
            foundAtLeastAMethod = results.foundAtLeastAMethod;
            vResult.addAll(results.matchedMethods);
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
          throw new TestNGException(
              canonicalMethodName
                  + "() is depending on method "
                  + maybeReferringTo
                  + ", which is not annotated with @Test or not included.");
        }
        throw new TestNGException(
            canonicalMethodName + "() depends on nonexistent method " + regexp);
      }
    } // end for

    return vResult.toArray(new ITestNGMethod[0]);
  }

  /**
   * Finds method based on regex and TestNGMethod. If regex doesn't represent the class name, uses
   * the TestNG method's class name.
   *
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
      className = testngMethod.getConstructorOrMethod().getDeclaringClass().getCanonicalName();
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
    } catch (Exception e) {
      // only logging
      Utils.log("MethodHelper", 3, "Caught exception while searching for methods using regex");
    }
    return null;
  }

  public static boolean isEnabled(Class<?> objectClass, IAnnotationFinder finder) {
    ITestAnnotation testClassAnnotation = AnnotationHelper.findTest(finder, objectClass);
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
    return null == test || test.getEnabled();
  }

  public static boolean isAlwaysRun(IConfigurationAnnotation configurationAnnotation) {
    if (null == configurationAnnotation) {
      return false;
    }

    boolean alwaysRun = false;
    if ((configurationAnnotation.getAfterSuite()
            || configurationAnnotation.getAfterTest()
            || configurationAnnotation.getAfterTestClass()
            || configurationAnnotation.getAfterTestMethod()
            || configurationAnnotation.getBeforeTestMethod()
            || configurationAnnotation.getBeforeTestClass()
            || configurationAnnotation.getBeforeTest()
            || configurationAnnotation.getBeforeSuite()
            || configurationAnnotation.getBeforeGroups().length != 0
            || configurationAnnotation.getAfterGroups().length != 0)
        && configurationAnnotation.getAlwaysRun()) {
      alwaysRun = true;
    }

    return alwaysRun;
  }

  /** Extracts the unique list of <code>ITestNGMethod</code>s. */
  public static List<ITestNGMethod> uniqueMethodList(Collection<List<ITestNGMethod>> methods) {
    Set<ITestNGMethod> resultSet = Sets.newHashSet();

    for (List<ITestNGMethod> l : methods) {
      resultSet.addAll(l);
    }

    return Lists.newArrayList(resultSet);
  }

  private static Graph<ITestNGMethod> topologicalSort(
      ITestNGMethod[] methods,
      List<ITestNGMethod> sequentialList,
      List<ITestNGMethod> parallelList,
      final Comparator<ITestNGMethod> comparator) {
    Graph<ITestNGMethod> result =
        new Graph<>((o1, o2) -> comparator.compare(o1.getObject(), o2.getObject()));

    if (methods.length == 0) {
      return result;
    }

    //
    // Create the graph
    //

    Map<Object, List<ITestNGMethod>> testInstances = sortMethodsByInstance(methods);

    XmlTest xmlTest = null;
    for (ITestNGMethod m : methods) {
      if (xmlTest == null) {
        xmlTest = m.getXmlTest();
      }
      result.addNode(m);

      List<ITestNGMethod> predecessors = Lists.newArrayList();

      String[] methodsDependedUpon = m.getMethodsDependedUpon();
      if (methodsDependedUpon.length > 0) {
        ITestNGMethod[] methodsNamed;
        // Method has instance
        if (m.getInstance() != null) {
          // Get other methods with the same instance
          List<ITestNGMethod> instanceMethods = testInstances.get(m.getInstance());
          try {
            // Search for other methods that depends upon with the same instance
            methodsNamed = MethodHelper.findDependedUponMethods(m, instanceMethods);
          } catch (TestNGException e) {
            // Maybe this method has a dependency on a method that resides in a different instance.
            // Lets try searching for all methods now
            methodsNamed = MethodHelper.findDependedUponMethods(m, methods);
          }
        } else {
          // Search all methods
          methodsNamed = MethodHelper.findDependedUponMethods(m, methods);
        }
        predecessors.addAll(Arrays.asList(methodsNamed));
      }
      boolean anyConfigExceptGroupConfigs =
          !(m.isBeforeGroupsConfiguration() || m.isAfterGroupsConfiguration());
      boolean isGroupAgnosticConfigMethod = !m.isTest() && anyConfigExceptGroupConfigs;
      if (isGroupAgnosticConfigMethod) {
        String[] groupsDependedUpon = m.getGroupsDependedUpon();
        if (groupsDependedUpon.length > 0) {
          for (String group : groupsDependedUpon) {
            ITestNGMethod[] methodsThatBelongToGroup =
                MethodGroupsHelper.findMethodsThatBelongToGroup(m, methods, group);
            predecessors.addAll(Arrays.asList(methodsThatBelongToGroup));
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

  /**
   * This method is used to create a map of test instances and their associated method(s) . Used to
   * decrease the scope to only a methods instance when trying to find method dependencies.
   *
   * @param methods Methods to be sorted
   * @return Map of Instances as the keys and the methods associated with the instance as the values
   */
  private static Map<Object, List<ITestNGMethod>> sortMethodsByInstance(ITestNGMethod[] methods) {
    return Arrays.stream(methods)
        .parallel()
        .filter(m -> Objects.nonNull(m.getInstance()))
        .collect(Collectors.groupingBy(ITestNGMethod::getInstance, Collectors.toList()));
  }

  protected static String calculateMethodCanonicalName(ITestNGMethod m) {
    return calculateMethodCanonicalName(m.getConstructorOrMethod().getMethod());
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
      } catch (Exception e) {
        // ignore
      }
      cls = cls.getSuperclass();
    }

    result = packageName + "." + m.getName();
    CANONICAL_NAME_CACHE.put(m, result);
    return result;
  }

  private static List<ITestNGMethod> sortMethods(
      boolean forTests, List<ITestNGMethod> allMethods, Comparator<ITestNGMethod> comparator) {
    List<ITestNGMethod> sl = Lists.newArrayList();
    List<ITestNGMethod> pl = Lists.newArrayList();
    ITestNGMethod[] allMethodsArray = allMethods.toArray(new ITestNGMethod[0]);

    // Fix the method inheritance if these are @Configuration methods to make
    // sure base classes are invoked before child classes if 'before' and the
    // other way around if they are 'after'
    if (!forTests && allMethodsArray.length > 0) {
      ITestNGMethod m = allMethodsArray[0];
      boolean before =
          m.isBeforeClassConfiguration()
              || m.isBeforeMethodConfiguration()
              || m.isBeforeSuiteConfiguration()
              || m.isBeforeTestConfiguration();
      MethodInheritance.fixMethodInheritance(allMethodsArray, before);
    }

    topologicalSort(allMethodsArray, sl, pl, comparator);

    List<ITestNGMethod> result = Lists.newArrayList();
    result.addAll(sl);
    result.addAll(pl);
    return result;
  }

  /** @return A sorted array containing all the methods 'method' depends on */
  public static List<ITestNGMethod> getMethodsDependedUpon(
      ITestNGMethod method, ITestNGMethod[] methods, Comparator<ITestNGMethod> comparator) {
    Graph<ITestNGMethod> g = GRAPH_CACHE.get(methods);
    if (g == null) {
      List<ITestNGMethod> parallelList = Lists.newArrayList();
      List<ITestNGMethod> sequentialList = Lists.newArrayList();
      g = topologicalSort(methods, sequentialList, parallelList, comparator);
      GRAPH_CACHE.put(methods, g);
    }

    return g.findPredecessors(method);
  }

  // TODO: This needs to be revisited so that, we dont update the parameter list "methodList"
  // but we are returning the values.
  public static void fixMethodsWithClass(
      ITestNGMethod[] methods, ITestClass testCls, List<ITestNGMethod> methodList) {
    for (ITestNGMethod itm : methods) {
      itm.setTestClass(testCls);

      if (methodList != null) {
        methodList.add(itm);
      }
    }
  }

  public static List<IMethodInstance> methodsToMethodInstances(List<ITestNGMethod> sl) {
    return sl.stream().map(MethodInstance::new).collect(Collectors.toList());
  }

  public static List<ITestNGMethod> methodInstancesToMethods(
      List<IMethodInstance> methodInstances) {
    return methodInstances.stream().map(IMethodInstance::getMethod).collect(Collectors.toList());
  }

  public static void dumpInvokedMethodInfoToConsole(ITestNGMethod[] methods, int currentVerbosity) {
    if (currentVerbosity < 3) {
      return;
    }
    System.out.println("===== Invoked methods");
    Arrays.stream(methods)
        .filter(m -> m instanceof IInvocationStatus)
        .filter(m -> ((IInvocationStatus) m).getInvocationTime() > 0)
        .forEach(
            im -> {
              if (im.isTest()) {
                System.out.print("    ");
              } else if (isConfigurationMethod(im)) {
                System.out.print("  ");
              } else {
                return;
              }
              System.out.println("" + im);
            });
    System.out.println("=====");
  }

  private static boolean isConfigurationMethod(ITestNGMethod tm) {
    return tm.isBeforeSuiteConfiguration()
        || tm.isAfterSuiteConfiguration()
        || tm.isBeforeTestConfiguration()
        || tm.isAfterTestConfiguration()
        || tm.isBeforeClassConfiguration()
        || tm.isAfterClassConfiguration()
        || tm.isBeforeGroupsConfiguration()
        || tm.isAfterGroupsConfiguration()
        || tm.isBeforeMethodConfiguration()
        || tm.isAfterMethodConfiguration();
  }

  protected static String calculateMethodCanonicalName(Class<?> methodClass, String methodName) {
    Set<Method> methods = ClassHelper.getAvailableMethods(methodClass); // TESTNG-139
    return methods.stream()
        .filter(method -> methodName.equals(method.getName()))
        .findFirst()
        .map(MethodHelper::calculateMethodCanonicalName)
        .orElse(null);
  }

  public static void clear(Stream<Method> methods) {
    methods.filter(Objects::nonNull).forEach(CANONICAL_NAME_CACHE::remove);
  }

  public static long calculateTimeOut(ITestNGMethod tm) {
    return tm.getTimeOut() > 0 ? tm.getTimeOut() : tm.getInvocationTimeOut();
  }

  private static MatchResults matchMethod(ITestNGMethod[] methods, String regexp) {
    MatchResults results = new MatchResults();
    boolean usePackage = regexp.indexOf('.') != -1;
    Pattern pattern = Pattern.compile(regexp);
    for (ITestNGMethod method : methods) {
      ConstructorOrMethod thisMethod = method.getConstructorOrMethod();
      String thisMethodName = thisMethod.getName();
      String methodName = usePackage ? calculateMethodCanonicalName(method) : thisMethodName;
      Pair<String, String> cacheKey = Pair.create(regexp, methodName);
      Boolean match = MATCH_CACHE.get(cacheKey);
      if (match == null) {
        match = pattern.matcher(methodName).matches();
        MATCH_CACHE.put(cacheKey, match);
      }
      if (match) {
        results.matchedMethods.add(method);
        results.foundAtLeastAMethod = true;
      }
    }
    return results;
  }

  private static class MatchResults {
    private final List<ITestNGMethod> matchedMethods = Lists.newArrayList();
    private boolean foundAtLeastAMethod = false;
  }
}
