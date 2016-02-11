package org.testng.internal.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class ReflectionHelper {
  /**
   * @return An array of all locally declared methods or equivalent thereof
   * (such as default methods on Java 8 based interfaces that the given class
   * implements).
   */
  public static Method[] getLocalMethods(Class<?> clazz) {
    Method[] result;
    Method[] declaredMethods = clazz.getDeclaredMethods();
    List<Method> defaultMethods = getDefaultMethods(clazz);
    if (defaultMethods != null) {
      result = new Method[declaredMethods.length + defaultMethods.size()];
      System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
      int index = declaredMethods.length;
      for (Method defaultMethod : defaultMethods) {
        result[index] = defaultMethod;
        index++;
      }
    }
    else {
      result = declaredMethods;
    }
    return result;
  }

  private static List<Method> getDefaultMethods(Class<?> clazz) {
    List<Method> result = null;
    for (Class<?> ifc : clazz.getInterfaces()) {
      for (Method ifcMethod : ifc.getMethods()) {
        if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
          if (result == null) {
            result = new LinkedList<Method>();
          }
          result.add(ifcMethod);
        }
      }
    }
    return result;
  }

}
