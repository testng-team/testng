package org.testng.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

public class MethodInheritance {
  /**
   * Look in map for a class that is a superclass of methodClass
   */
  private static List<ITestNGMethod> findMethodListSuperClass(Map<Class, List<ITestNGMethod>> map,
      Class< ? extends ITestNGMethod> methodClass)
  {
    for (Map.Entry<Class, List<ITestNGMethod>> entry : map.entrySet()) {
      if (entry.getKey().isAssignableFrom(methodClass)) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * Look in map for a class that is a subclass of methodClass
   */
  private static Class findSubClass(Map<Class, List<ITestNGMethod>> map,
      Class< ? extends ITestNGMethod> methodClass)
  {
    for (Class cls : map.keySet()) {
      if (methodClass.isAssignableFrom(cls)) {
        return cls;
      }
    }

    return null;
  }

  /**
   * Fix the methodsDependedUpon to make sure that @Configuration methods
   * respect inheritance (before methods are invoked in the order Base first
   * and after methods are invoked in the order Child first)
   *
   * @param methods the list of methods
   * @param before true if we are handling a before method (meaning, the methods
   * need to be sorted base class first and subclass last). false otherwise (subclass
   * methods first, base classes last).
   */
  public static void fixMethodInheritance(ITestNGMethod[] methods, boolean before) {
    // Map of classes -> List of methods that belong to this class or same hierarchy
    Map<Class, List<ITestNGMethod>> map = Maps.newHashMap();

    //
    // Put the list of methods in their hierarchy buckets
    //
    for (ITestNGMethod method : methods) {
      Class< ? extends ITestNGMethod> methodClass = method.getRealClass();
      List<ITestNGMethod> l = findMethodListSuperClass(map, methodClass);
      if (null != l) {
        l.add(method);
      }
      else {
        Class subClass = findSubClass(map, methodClass);
        if (null != subClass) {
          l = map.get(subClass);
          l.add(method);
          map.remove(subClass);
          map.put(methodClass, l);
        }
        else {
          l = Lists.newArrayList();
          l.add(method);
          map.put(methodClass, l);
        }
      }
    }

    //
    // Each bucket that has a list bigger than one element gets sorted
    //
    for (List<ITestNGMethod> l : map.values()) {
      if (l.size() > 1) {
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
            if (!equalsEffectiveClass(m1, m2) && !dependencyExists(m1, m2, methods)) {
              Utils.log("MethodInheritance", 4, m2 + " DEPENDS ON " + m1);
              m2.addMethodDependedUpon(MethodHelper.calculateMethodCanonicalName(m1));
            }
          }
        }
      }
    }
  }

  private static boolean dependencyExists(ITestNGMethod m1, ITestNGMethod m2, ITestNGMethod[] methods) {
    return internalDependencyExists(m1, m2, methods) || internalDependencyExists(m2, m1, methods);
  }

  private static boolean internalDependencyExists(ITestNGMethod m1, ITestNGMethod m2, ITestNGMethod[] methods) {
    ITestNGMethod[] methodsNamed =
      MethodHelper.findDependedUponMethods(m1, methods);

    for (ITestNGMethod method : methodsNamed) {
      if (method.equals(m2)) {
        return true;
      }
    }

    for (String group : m1.getGroupsDependedUpon()) {
      ITestNGMethod[] methodsThatBelongToGroup =
        MethodGroupsHelper.findMethodsThatBelongToGroup(m1, methods, group);
      for (ITestNGMethod method : methodsThatBelongToGroup) {
         if (method.equals(m2)) {
           return true;
         }
       }
    }

    return false;
  }

  private static boolean equalsEffectiveClass(ITestNGMethod m1, ITestNGMethod m2) {
    try {
      Class c1 = m1.getRealClass();
      Class c2 = m2.getRealClass();

      return c1 == null ? c2 == null : c1.equals(c2);
    }
    catch(Exception ex) {
      return false;
    }
  }


  /**
   * Given a list of methods belonging to the same class hierarchy, orders them
   * from the base class to the child (if true) or from child to base class (if false)
   * @param methods
   */
  private static void sortMethodsByInheritance(List<ITestNGMethod> methods,
      boolean baseClassToChild)
  {
    Collections.sort(methods);
    if (! baseClassToChild) {
      Collections.reverse(methods);
    }
  }
}
