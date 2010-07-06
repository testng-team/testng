package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MethodInheritance {
  /**
   * Look in map for a class that is a superclass of methodClass
   * @param map
   * @param methodClass
   * @return
   */
  private static List<ITestNGMethod> findMethodListSuperClass(Map<Class, List<ITestNGMethod>> map, 
      Class< ? extends ITestNGMethod> methodClass)
  {
    for (Class cls : map.keySet()) {
      if (cls.isAssignableFrom(methodClass)) {
        return map.get(cls);
      }
    }
    
    return null;
  }
  
  /**
   * Look in map for a class that is a subclass of methodClass
   * @param map
   * @param methodClass
   * @return
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
   * @param methods
   * @param baseClassToChild
   */
  public static void fixMethodInheritance(ITestNGMethod[] methods, boolean baseClassToChild) {
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
        sortMethodsByInheritance(l, baseClassToChild);
        
        // Set methodDependedUpon accordingly
        if (baseClassToChild) {
          for (int i = 1; i < l.size(); i++) {
            ITestNGMethod m1 = l.get(i - 1);
            ITestNGMethod m2 = l.get(i);
            if (!equalsEffectiveClass(m1, m2) && !dependencyExists(m1, m2, methods)) {
              Utils.log("MethodInheritance", 4, m2 + " DEPENDS ON " + m1);
              m2.addMethodDependedUpon(MethodHelper.calculateMethodCanonicalName(m1));
            }
          }
        }
        else {
          for (int i = 0; i < l.size() - 1; i++) {
            ITestNGMethod m1 = l.get(i);
            ITestNGMethod m2 = l.get(i + 1);
            if (!equalsEffectiveClass(m1, m2) && !dependencyExists(m1, m2, methods)) {
              m2.addMethodDependedUpon(MethodHelper.calculateMethodCanonicalName(m1));
              Utils.log("MethodInheritance", 4, m2 + " DEPENDS ON " + m1);
            }
          }          
        }
      }
    }
  }
  
  private static boolean dependencyExists(ITestNGMethod m1, ITestNGMethod m2, ITestNGMethod[] methods) {
    return true == internalDependencyExists(m1, m2, methods) 
       ? true : internalDependencyExists(m2, m1, methods);
  }
  
  private static boolean internalDependencyExists(ITestNGMethod m1, ITestNGMethod m2, ITestNGMethod[] methods) {
    ITestNGMethod[] methodsNamed = 
      MethodHelper.findMethodsNamed(m1, methods, m1.getMethodsDependedUpon());

    for (ITestNGMethod method : methodsNamed) {
      if (method.equals(m2)) {
        return true;
      }
    }

    for (String group : m1.getGroupsDependedUpon()) {
      ITestNGMethod[] methodsThatBelongToGroup = 
        MethodHelper.findMethodsThatBelongToGroup(m1, methods, group);
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
      
      boolean isEqual = c1 == null ? c2 == null : c1.equals(c2);
      
      return isEqual; // && m1.getMethod().equals(m2.getMethod());
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
  
  private static void ppp(String s) {
    System.out.println("[MethodInheritance] " + s);
  }
}
