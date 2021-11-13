package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.collections.Pair;

/** Collections of helper methods to help deal with test methods */
public class MethodGroupsHelper {

  private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
  private static final Map<Pair<String, String>, Boolean> MATCH_CACHE = new ConcurrentHashMap<>();

  /**
   * Collect all the methods that belong to the included groups and exclude all the methods that
   * belong to an excluded group.
   */
  static void collectMethodsByGroup(
      ITestNGMethod[] methods,
      boolean forTests,
      List<ITestNGMethod> outIncludedMethods,
      List<ITestNGMethod> outExcludedMethods,
      RunInfo runInfo,
      IAnnotationFinder finder,
      boolean unique) {
    for (ITestNGMethod tm : methods) {
      boolean in = false;
      Method m = tm.getConstructorOrMethod().getMethod();
      //
      // @Test method
      //
      if (forTests) {
        in =
            MethodGroupsHelper.includeMethod(
                AnnotationHelper.findTest(finder, m),
                runInfo,
                tm,
                forTests,
                unique,
                outIncludedMethods);
      }

      //
      // @Configuration method
      //
      else {
        IConfigurationAnnotation annotation = AnnotationHelper.findConfiguration(finder, m);
        if (annotation.getAlwaysRun()) {
          if (!unique || MethodGroupsHelper.isMethodAlreadyNotPresent(outIncludedMethods, tm)) {
            in = true;
          }
        } else {
          in =
              MethodGroupsHelper.includeMethod(
                  AnnotationHelper.findTest(finder, tm),
                  runInfo,
                  tm,
                  forTests,
                  unique,
                  outIncludedMethods);
        }
      }
      if (in) {
        outIncludedMethods.add(tm);
      } else {
        outExcludedMethods.add(tm);
      }
    }
  }

  private static boolean includeMethod(
      ITestOrConfiguration annotation,
      RunInfo runInfo,
      ITestNGMethod tm,
      boolean forTests,
      boolean unique,
      List<ITestNGMethod> outIncludedMethods) {
    boolean result = false;

    if (MethodHelper.isEnabled(annotation)) {
      if (runInfo.includeMethod(tm, forTests)) {
        if (unique) {
          if (MethodGroupsHelper.isMethodAlreadyNotPresent(outIncludedMethods, tm)) {
            result = true;
          }
        } else {
          result = true;
        }
      }
    }

    return result;
  }

  private static boolean isMethodAlreadyNotPresent(List<ITestNGMethod> result, ITestNGMethod tm) {
    Class<?> cls = tm.getConstructorOrMethod().getDeclaringClass();
    return result
        .parallelStream()
        .map(ITestNGMethod::getConstructorOrMethod)
        .filter(m -> m.getName().equals(tm.getConstructorOrMethod().getName()))
        .map(ConstructorOrMethod::getDeclaringClass)
        .noneMatch(eachCls -> eachCls.isAssignableFrom(cls) || cls.isAssignableFrom(eachCls));
  }

  /**
   * @return the map of groups and their corresponding methods from the extraction of <code>classes
   *     </code>.
   */
  public static Map<String, List<ITestNGMethod>> findGroupsMethods(
      Collection<ITestClass> classes, boolean before) {
    Map<String, List<ITestNGMethod>> result = Maps.newHashMap();
    for (ITestClass cls : classes) {
      ITestNGMethod[] methods = before ? cls.getBeforeGroupsMethods() : cls.getAfterGroupsMethods();
      for (ITestNGMethod method : methods) {
        String[] grp = before ? method.getBeforeGroups() : method.getAfterGroups();
        List<String> groups =
            Stream.concat(Arrays.stream(grp), Arrays.stream(method.getGroups()))
                .collect(Collectors.toList());
        for (String group : groups) {
          List<ITestNGMethod> methodList = result.computeIfAbsent(group, k -> Lists.newArrayList());
          // NOTE(cbeust, 2007/01/23)
          // BeforeGroups/AfterGroups methods should only be invoked once.
          // I should probably use a map instead of a list for a contains(), but
          // this list should usually be fairly short
          if (!methodList.contains(method)) {
            methodList.add(method);
          }
        }
      }
    }

    return result;
  }

  protected static void findGroupTransitiveClosure(
      List<ITestNGMethod> includedMethods,
      List<ITestNGMethod> allMethods,
      String[] includedGroups,
      Set<String> outGroups,
      Set<ITestNGMethod> outMethods) {
    Map<ITestNGMethod, ITestNGMethod> runningMethods =
        includedMethods.stream().collect(Collectors.toMap(m -> m, m -> m));

    Map<String, String> runningGroups =
        Arrays.stream(includedGroups).collect(Collectors.toMap(g -> g, g -> g));

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
          if (!runningGroups.containsKey(g)) {
            // Found a new included group, add all the methods it contains to
            // our outMethod closure
            runningGroups.put(g, g);
            ITestNGMethod[] im =
                MethodGroupsHelper.findMethodsThatBelongToGroup(
                    m, allMethods.toArray(new ITestNGMethod[0]), g);
            for (ITestNGMethod thisMethod : im) {
              if (!runningMethods.containsKey(thisMethod)) {
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
          ITestNGMethod thisMethod = MethodGroupsHelper.findMethodNamed(tm, allMethods);
          if (thisMethod != null && !runningMethods.containsKey(thisMethod)) {
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

  private static ITestNGMethod findMethodNamed(String tm, List<ITestNGMethod> allMethods) {
    return allMethods.stream()
        .filter(m -> m.getQualifiedName().equals(tm))
        .findFirst()
        .orElse(null);
  }

  /**
   * Only used if a group is missing to flag an error on that method
   *
   * @param method if no group is found, group regex is set as this method's missing group
   * @param methods list of methods to search
   * @param groupRegexp regex representing the group
   * @return all the methods that belong to the group specified by the regular expression
   *     groupRegExp. methods[] is the list of all the methods we are choosing from and method is
   *     the method that owns the dependsOnGroups statement (only used if a group is missing to flag
   *     an error on that method).
   */
  public static ITestNGMethod[] findMethodsThatBelongToGroup(
      ITestNGMethod method, ITestNGMethod[] methods, String groupRegexp) {
    ITestNGMethod[] found = findMethodsThatBelongToGroup(methods, groupRegexp);

    if (found.length == 0) {
      method.setMissingGroup(groupRegexp);
    }

    return found;
  }

  /**
   * @param methods list of methods to search
   * @param groupRegexp regex representing the group
   * @return all the methods that belong to the group specified by the regular expression
   *     groupRegExp. methods[] is the list of all the methods we are choosing from.
   */
  protected static ITestNGMethod[] findMethodsThatBelongToGroup(
      ITestNGMethod[] methods, String groupRegexp) {
    final Pattern pattern = getPattern(groupRegexp);
    Predicate<ITestNGMethod> matchingGroups =
        tm -> Arrays.stream(tm.getGroups()).anyMatch(group -> isMatch(pattern, group));
    return Arrays.stream(methods).filter(matchingGroups).toArray(ITestNGMethod[]::new);
  }

  private static Boolean isMatch(Pattern pattern, String group) {
    Pair<String, String> cacheKey = Pair.create(pattern.pattern(), group);
    return MATCH_CACHE.computeIfAbsent(cacheKey, k -> pattern.matcher(group).matches());
  }

  private static Pattern getPattern(String groupRegexp) {
    return PATTERN_CACHE.computeIfAbsent(groupRegexp, Pattern::compile);
  }
}
