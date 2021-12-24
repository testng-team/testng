package org.testng.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

public class MethodInheritance {

  /**
   * A Custom comparator that helps in {@link ITestNGMethod} ordering keeping in mind the class
   * hierarchy. Here's how the comparator works: <br>
   * Lets say we have two method objects o1 and o2. <br>
   * o1 is associated with MyClass and o2 is associated with AnotherClass.
   *
   * <ul>
   *   <li>-1 is returned if MyClass is the parent of AnotherClass
   *   <li>1 is returned if AnotherClass is the parent of MyClass
   *   <li>0 is returned otherwise if MyClass and AnotherClass are the same i.e., both methods
   *       belong to the same class.
   * </ul>
   *
   * Working of isAssignableFrom <br>
   * Lets say we have : <br>
   *
   * <ol>
   *   <li>interface Oven
   *   <li>Microwave implements Oven
   * </ol>
   *
   * <ol>
   *   <li>microwave instanceof Oven : <b>returns true</b>
   *   <li>Oven.class.isAssignableFrom(microwave.getClass()) : <b>returns true</b>
   * </ol>
   */
  private static final Comparator<ITestNGMethod> COMPARATOR =
      (o1, o2) -> {
        int result = -2;
        Class<?> thisClass = o1.getRealClass();
        Class<?> otherClass = o2.getRealClass();
        if (thisClass.isAssignableFrom(otherClass)) {
          result = -1;
        } else if (otherClass.isAssignableFrom(thisClass)) {
          result = 1;
        } else if (o1.equals(o2)) {
          result = 0;
        }
        return result;
      };

  /** Look in map for a class that is a superclass of methodClass */
  private static List<ITestNGMethod> findMethodListSuperClass(
      Map<Class<?>, List<ITestNGMethod>> map, Class<? extends ITestNGMethod> methodClass) {
    return map.entrySet().stream()
        .parallel()
        .filter(each -> each.getKey().isAssignableFrom(methodClass))
        .map(Entry::getValue)
        .findFirst()
        .orElse(null);
  }

  /** Look in map for a class that is a subclass of methodClass */
  private static Class<?> findSubClass(
      Map<Class<?>, List<ITestNGMethod>> map, Class<? extends ITestNGMethod> methodClass) {
    return map.keySet().stream()
        .parallel()
        .filter(methodClass::isAssignableFrom)
        .findFirst()
        .orElse(null);
  }

  /**
   * Fix the methodsDependedUpon to make sure that @Configuration methods respect inheritance
   * (before methods are invoked in the order Base first and after methods are invoked in the order
   * Child first)
   *
   * @param methods the list of methods
   * @param before true if we are handling a before method (meaning, the methods need to be sorted
   *     base class first and subclass last). false otherwise (subclass methods first, base classes
   *     last).
   */
  public static void fixMethodInheritance(ITestNGMethod[] methods, boolean before) {
    // Map of classes -> List of methods that belong to this class or same hierarchy
    Map<Class<?>, List<ITestNGMethod>> map = Maps.newHashMap();

    //
    // Put the list of methods in their hierarchy buckets
    //
    for (ITestNGMethod method : methods) {
      Class<? extends ITestNGMethod> methodClass = method.getRealClass();
      List<ITestNGMethod> l = findMethodListSuperClass(map, methodClass);
      if (null != l) {
        l.add(method);
      } else {
        Class<?> subClass = findSubClass(map, methodClass);
        if (null != subClass) {
          l = map.get(subClass);
          l.add(method);
          map.remove(subClass);
          map.put(methodClass, l);
        } else {
          l = Lists.newArrayList();
          l.add(method);
          map.put(methodClass, l);
        }
      }
    }

    //
    // Each bucket that has a list bigger than one element gets sorted
    //
    map.values()
        .parallelStream()
        .filter(l -> l.size() > 1)
        .forEach(
            l -> {
              // Sort them
              sortMethodsByInheritance(l, before);

              /*
               *  Set methodDependedUpon accordingly
               *  E.g. Base class can have multiple @BeforeClass methods. Need to ensure
               *  that @BeforeClass methods in derived class depend on all @BeforeClass methods
               *  of base class. Vice versa for @AfterXXX methods
               */

              for (int i = 0; i < l.size() - 1; i++) {
                ITestNGMethod m1 = l.get(i);
                for (int j = i + 1; j < l.size(); j++) {
                  ITestNGMethod m2 = l.get(j);
                  boolean notEffectivelyEqual = !equalsEffectiveClass(m1, m2);
                  boolean upstreamHierarchy = hasUpstreamHierarchy(m1, m2);
                  boolean shouldConsider =
                      before ? notEffectivelyEqual && upstreamHierarchy : notEffectivelyEqual;
                  boolean hasGroupDependencies =
                      m2.getGroupsDependedUpon().length == 0
                          && m1.getGroupsDependedUpon().length == 0;

                  if (shouldConsider
                      && !dependencyExists(m1, m2, methods)
                      && hasGroupDependencies) {
                    Utils.log("MethodInheritance", 4, m2 + " DEPENDS ON " + m1);
                    m2.addMethodDependedUpon(MethodHelper.calculateMethodCanonicalName(m1));
                  }
                }
              }
            });
  }

  private static boolean hasUpstreamHierarchy(ITestNGMethod m1, ITestNGMethod m2) {
    Class<?> c1 = m1.getRealClass();
    Class<?> c2 = m2.getRealClass();
    if (c1.equals(c2)) {
      return false;
    }
    return c1.isAssignableFrom(c2);
  }

  private static boolean dependencyExists(
      ITestNGMethod m1, ITestNGMethod m2, ITestNGMethod[] methods) {
    return internalDependencyExists(m1, m2, methods) || internalDependencyExists(m2, m1, methods);
  }

  private static boolean internalDependencyExists(
      ITestNGMethod m1, ITestNGMethod m2, ITestNGMethod[] methods) {
    ITestNGMethod[] methodsNamed = MethodHelper.findDependedUponMethods(m1, methods);

    boolean match = Arrays.stream(methodsNamed).parallel().anyMatch(method -> method.equals(m2));
    if (match) {
      return true;
    }

    return Arrays.stream(m1.getGroupsDependedUpon())
        .parallel()
        .anyMatch(
            group -> {
              ITestNGMethod[] methodsThatBelongToGroup =
                  MethodGroupsHelper.findMethodsThatBelongToGroup(m1, methods, group);
              return Arrays.stream(methodsThatBelongToGroup)
                  .parallel()
                  .anyMatch(method -> method.equals(m2));
            });
  }

  private static boolean equalsEffectiveClass(ITestNGMethod m1, ITestNGMethod m2) {
    try {
      Class<?> c1 = m1.getRealClass();
      Class<?> c2 = m2.getRealClass();
      return Objects.equals(c1, c2);
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * Given a list of methods belonging to the same class hierarchy, orders them from the base class
   * to the child (if true) or from child to base class (if false)
   */
  private static void sortMethodsByInheritance(
      List<ITestNGMethod> methods, boolean baseClassToChild) {
    methods.sort(COMPARATOR);
    if (!baseClassToChild) {
      Collections.reverse(methods);
    }
  }
}
