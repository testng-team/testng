package org.testng.xml.dom;

import org.testng.collections.Lists;
import org.testng.internal.collections.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class Reflect {
  public static List<Pair<Method, Wrapper>> findMethodsWithAnnotation(
      Class<?> c, Class<? extends Annotation> ac, Object bean) {
    List<Pair<Method, Wrapper>> result = Lists.newArrayList();
    for (Method m : c.getMethods()) {
      Annotation a = m.getAnnotation(ac);
      if (a != null) {
        result.add(Pair.of(m, new Wrapper(a, bean)));
      }
    }
    return result;
  }

  public static Pair<Method, Wrapper> findSetterForTag(
      Class<?> c, String tagName, Object bean) {

    // Try to find an annotation
    List<Class<? extends Annotation>> annotations =
        Arrays.asList(OnElement.class, OnElementList.class, Tag.class);
    for (Class<? extends Annotation> annotationClass : annotations) {
      List<Pair<Method, Wrapper>> methods
          = findMethodsWithAnnotation(c, annotationClass, bean);
  
      for (Pair<Method, Wrapper> pair : methods) {
        if (pair.second().getTagName().equals(tagName)) {
          return pair;
        }
      }
    }

    // try fo find an adder or a setter
    for (String prefix : new String[] { "add", "set" }) {
      for (Method m : c.getDeclaredMethods()) {
        String methodName = toCamelCase(tagName, prefix);
        if (m.getName().equals(methodName)) {
          return Pair.of(m, null);
        }
      }
    }

    return null;
  }

  private static String toCamelCase(String name, String prefix) {
    return prefix + toCapitalizedCamelCase(name);
  }

  public static String toCapitalizedCamelCase(String name) {
    StringBuilder result = new StringBuilder(name.substring(0, 1).toUpperCase());
    for (int i = 1; i < name.length(); i++) {
      if (name.charAt(i) == '-') {
        result.append(Character.toUpperCase(name.charAt(i + 1)));
        i++;
      } else {
        result.append(name.charAt(i));
      }
    }
    return result.toString();
  }

}
