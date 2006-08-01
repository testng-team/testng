package org.testng.internal;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.IMethodSelector;
import org.testng.TestNGException;
import org.testng.internal.annotations.IAnnotation;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IFactory;

/**
 * Utility class for different class manipulations.
 *
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alex Popescu</a>
 */
public final class ClassHelper {

  /** Hide constructor. */
  private ClassHelper() {
    // Hide Constructor
  }
  
  /**
   * Tries to load the specified class using the context ClassLoader or if none,
   * than from the default ClassLoader. This method differs from the standard 
   * class loading methods in that it does not throw an exception if the class 
   * is not found but returns null instead.
   *  
   * @param className the class name to be loaded.
   *
   * @return the class or null if the class is not found.
   */
  public static Class forName(final String className) {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader != null) {
      try {
        return classLoader.loadClass(className);
      }
      catch(Exception ex) {
        // TODO document why we catch all Exceptions instead of just 
        // ClassNotFoundException.
      }
    }
    
    try {
      return Class.forName(className);
    }
    catch(ClassNotFoundException cnfe) {
      return null;
    }
  }

  /**
   * For the given class, returns the method annotated with &#64;Factory or null 
   * if none is found. This method does not search up the superclass hierarchy.
   * If more than one method is @Factory annotated, a TestNGException is thrown. 
   * @param cls The class to search for the @Factory annotation.
   * @param finder The finder (JDK 1.4 or JDK 5.0+) use to search for the annotation. 
   *
   * @return the @Factory <CODE>method</CODE> or null
   *  
   * FIXME: @Factory method must be public!
   * TODO rename this method to findDeclaredFactoryMethod
   */
  public static Method findFactoryMethod(Class cls, IAnnotationFinder finder) {
    Method result = null;

    for (Method method : cls.getDeclaredMethods()) {
      IAnnotation f = finder.findAnnotation(method, IFactory.class);

      if (null != f) {
        if (null != result) {
          throw new TestNGException(cls.getName() + ":  only one @Factory method allowed");
        }
        result = method;
      }
    }

    // If we didn't find anything, look for nested classes
//    if (null == result) {
//      Class[] subClasses = cls.getClasses();
//      for (Class subClass : subClasses) {
//        result = findFactoryMethod(subClass, finder);
//        if (null != result) {
//          break;
//        }
//      }
//    }

    // Found the method, verify that it returns an array of objects
    // TBD

    return result;
  }

//  private static void ppp(String s) {
//    System.out.println("[ClassHelper] " + s);
//  }

  /**
   * Extract all callable methods of a class and all its super (keeping in mind
   * the Java access rules).
   *
   * @param clazz
   * @return
   */
  public static Set<Method> getAvailableMethods(Class clazz) {
    Set<Method> methods = new HashSet<Method>(Arrays.asList(clazz.getDeclaredMethods()));

    String fqn= clazz.getName();

    Class parent= clazz.getSuperclass();

    while (null != parent) {
      methods.addAll(extractMethods(clazz, parent, methods));
      parent= parent.getSuperclass();
    }

    return methods;
  }

  private static Set<Method> extractMethods(final Class childClass, final Class clazz, final Set<Method> collected) {
    Set<Method> methods = new HashSet<Method>();

    Method[] declaredMethods = clazz.getDeclaredMethods();

    Package childPackage = childClass.getPackage();
    Package classPackage = clazz.getPackage();
    boolean isSamePackage = false;

    if ((null == childPackage) && (null == classPackage)) {
      isSamePackage = true;
    }
    if ((null != childPackage) && (null != classPackage)) {
      isSamePackage = childPackage.getName().equals(classPackage.getName());
    }

    for (Method method : declaredMethods) {
      int methodModifiers = method.getModifiers();
      if (Modifier.isPublic(methodModifiers)
        || Modifier.isProtected(methodModifiers)) {
        if (!isOverridden(method, collected)) {
          methods.add(method);
        }
      }
      else if (isSamePackage && !Modifier.isPrivate(methodModifiers)) {
        if (!isOverridden(method, collected)) {
          methods.add(method);
        }
      }
    }

    return methods;
  }

  private static boolean isOverridden(Method method, Set<Method> collectedMethods) {
    Class methodClass = method.getDeclaringClass();
    Class[] methodParams = method.getParameterTypes();
    
    for (Method m: collectedMethods) {
      Class[] paramTypes = m.getParameterTypes();
      if (method.getName().equals(m.getName())
         && methodClass.isAssignableFrom(m.getDeclaringClass())
         && methodParams.length == paramTypes.length) {
        
        boolean sameParameters = true;
        for (int i= 0; i < methodParams.length; i++) {
          if (!methodParams[i].equals(paramTypes[i])) {
            sameParameters = false;
            break;
          }
        }
        
        if (sameParameters) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  public static IMethodSelector createSelector(org.testng.xml.XmlMethodSelector selector) {
    try {
      Class cls = Class.forName(selector.getClassName());
      return (IMethodSelector) cls.newInstance();
    }
    catch(Exception ex) {
      throw new TestNGException("Couldn't find method selector : " + selector.getClassName(), ex);
    }
  }
}
