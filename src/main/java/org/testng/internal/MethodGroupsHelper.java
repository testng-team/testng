package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.collections.Pair;

/**
 * Collections of helper methods to help deal with test methods
 *
 * @author Cedric Beust <cedric@beust.com>
 * @author nullin <nalin.makar * gmail.com>
 *
 */
public class MethodGroupsHelper {

  private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
  private static final Map<Pair<String, String>, Boolean> MATCH_CACHE =
          new ConcurrentHashMap<>();

    /**
   * Collect all the methods that belong to the included groups and exclude all
   * the methods that belong to an excluded group.
   */
  static void collectMethodsByGroup(ITestNGMethod[] methods,
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
        in = MethodGroupsHelper.includeMethod(AnnotationHelper.findTest(finder, m),
            runInfo, tm, forTests, unique, outIncludedMethods);
      }

      //
      // @Configuration method
      //
      else {
        IConfigurationAnnotation annotation = AnnotationHelper.findConfiguration(finder, m);
        if (annotation.getAlwaysRun()) {
        	if (!unique || !MethodGroupsHelper.isMethodAlreadyPresent(outIncludedMethods, tm)) {
        		in = true;
        	}
        }
        else {
          in = MethodGroupsHelper.includeMethod(AnnotationHelper.findTest(finder, tm),
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

  private static boolean includeMethod(ITestOrConfiguration annotation,
      RunInfo runInfo, ITestNGMethod tm, boolean forTests, boolean unique, List<ITestNGMethod> outIncludedMethods)
  {
    boolean result = false;

    if (MethodHelper.isEnabled(annotation)) {
      if (runInfo.includeMethod(tm, forTests)) {
        if (unique) {
          if (!MethodGroupsHelper.isMethodAlreadyPresent(outIncludedMethods, tm)) {
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

  protected static void findGroupTransitiveClosure(XmlMethodSelector xms,
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
              MethodGroupsHelper.findMethodsThatBelongToGroup(m,
                    allMethods.toArray(new ITestNGMethod[allMethods.size()]), g);
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
          ITestNGMethod thisMethod = MethodGroupsHelper.findMethodNamed(tm, allMethods);
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

  private static ITestNGMethod findMethodNamed(String tm, List<ITestNGMethod> allMethods) {
    for (ITestNGMethod m : allMethods) {
      // TODO(cbeust):  account for package
      String methodName =
        m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName();
      if (methodName.equals(tm)) {
        return m;
      }
    }

    return null;
  }

  /**
   * Only used if a group is missing to flag an error on that method
   *
   * @param method if no group is found, group regex is set as this method's missing group
   * @param methods list of methods to search
   * @param groupRegexp regex representing the group
   *
   * @return all the methods that belong to the group specified by the regular
   * expression groupRegExp.  methods[] is the list of all the methods we
   * are choosing from and method is the method that owns the dependsOnGroups
   * statement (only used if a group is missing to flag an error on that method).
   */
  protected static ITestNGMethod[] findMethodsThatBelongToGroup(
      ITestNGMethod method,
      ITestNGMethod[] methods, String groupRegexp)
  {
    ITestNGMethod[] found = findMethodsThatBelongToGroup(methods, groupRegexp);

    if (found.length == 0) {
      method.setMissingGroup(groupRegexp);
    }

    return found;
  }

  /**
   * @param methods list of methods to search
   * @param groupRegexp regex representing the group
   *
   * @return all the methods that belong to the group specified by the regular
   * expression groupRegExp.  methods[] is the list of all the methods we
   * are choosing from.
   */
  protected static ITestNGMethod[] findMethodsThatBelongToGroup(ITestNGMethod[] methods, String groupRegexp)
  {
    List<ITestNGMethod> vResult = Lists.newArrayList();
    final Pattern pattern = getPattern(groupRegexp);
    for (ITestNGMethod tm : methods) {
      String[] groups = tm.getGroups();
      for (String group : groups) {
        Boolean match = isMatch(pattern, group);
        if (match) {
          vResult.add(tm);
        }
      }
    }

    return vResult.toArray(new ITestNGMethod[vResult.size()]);
  }

  private static Boolean isMatch(Pattern pattern, String group) {
    Pair<String, String> cacheKey = Pair.create(pattern.pattern(), group);
    Boolean match = MATCH_CACHE.get(cacheKey);
    if (match == null) {
      match = pattern.matcher(group).matches();
      MATCH_CACHE.put(cacheKey, match);
    }
    return match;
  }

  private static Pattern getPattern(String groupRegexp) {
    Pattern groupPattern = PATTERN_CACHE.get(groupRegexp);
    if (groupPattern == null) {
      groupPattern = Pattern.compile(groupRegexp);
      PATTERN_CACHE.put(groupRegexp, groupPattern);
    }
    return groupPattern;
  }


}
