package org.testng;

import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper class to keep track of dependencies.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class DependencyMap {
  private ListMultiMap<String, ITestNGMethod> m_dependencies = Maps.newListMultiMap();
  private ListMultiMap<String, ITestNGMethod> m_groups = Maps.newListMultiMap();

  public DependencyMap(ITestNGMethod[] methods) {
    for (ITestNGMethod m : methods) {
    	m_dependencies.put( m.getQualifiedName(), m);
      for (String g : m.getGroups()) {
        m_groups.put(g, m);
      }
    }
  }

  public List<ITestNGMethod> getMethodsThatBelongTo(String group, ITestNGMethod fromMethod) {
    Set<String> uniqueKeys = m_groups.keySet();

    List<ITestNGMethod> result = Lists.newArrayList();

    for (String k : uniqueKeys) {
      if (Pattern.matches(group, k)) {
        result.addAll(m_groups.get(k));
      }
    }

    if (result.isEmpty() && !fromMethod.ignoreMissingDependencies()) {
      throw new TestNGException("DependencyMap::Method \"" + fromMethod
          + "\" depends on nonexistent group \"" + group + "\"");
    } else {
      return result;
    }
  }

  public ITestNGMethod getMethodDependingOn(String methodName, ITestNGMethod fromMethod) {
    List<ITestNGMethod> l = m_dependencies.get(methodName);
    if (l.isEmpty()) {
      // Try to fetch dependencies by using the test class in the method name.
      // This is usually needed in scenarios wherein a child class overrides a base class method
      // So the dependency name needs to be adjusted to use the test class name instead of using the
      //declared class.
      l = m_dependencies.get(constructMethodNameUsingTestClass(methodName, fromMethod));
    }
    if (l.isEmpty() && fromMethod.ignoreMissingDependencies()){
    	return fromMethod;
    }
    for (ITestNGMethod m : l) {
      // If they are in the same class hierarchy, they must belong to the same instance,
      // otherwise, it's a method depending on a method in a different class so we
      // don't bother checking the instance
      if (isSameInstance(fromMethod, m) || belongToDifferentClassHierarchy(fromMethod, m) || hasInstance(fromMethod, m)) {
        return m;
      }
    }
    throw new TestNGException("Method \"" + fromMethod
        + "\" depends on nonexistent method \"" + methodName + "\"");
  }

  private static boolean belongToDifferentClassHierarchy(ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    return ! baseClassMethod.getRealClass().isAssignableFrom(derivedClassMethod.getRealClass());
  }

  private static boolean hasInstance(ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    Object baseInstance = baseClassMethod.getInstance();
    Object derivedInstance = derivedClassMethod.getInstance();
    return derivedInstance != null || baseInstance != null;
  }

  private static boolean isSameInstance(ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    Object baseInstance = baseClassMethod.getInstance();
    Object derivedInstance = derivedClassMethod.getInstance();
    return derivedInstance != null &&
            baseInstance != null &&
            baseInstance.getClass().isAssignableFrom(derivedInstance.getClass());
  }

  private static String constructMethodNameUsingTestClass(String currentMethodName, ITestNGMethod m) {
    int lastIndex = currentMethodName.lastIndexOf('.');
    if (lastIndex != -1) {
      return m.getTestClass().getRealClass().getName() + currentMethodName.substring(lastIndex);
    }
    return currentMethodName;
  }

}
