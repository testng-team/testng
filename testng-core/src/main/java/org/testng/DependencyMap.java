package org.testng;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;
import org.testng.internal.RuntimeBehavior;

/** Helper class to keep track of dependencies. */
public class DependencyMap {
  private final ListMultiMap<String, ITestNGMethod> m_dependencies = Maps.newListMultiMap();
  private final ListMultiMap<String, ITestNGMethod> m_groups = Maps.newListMultiMap();

  public DependencyMap(ITestNGMethod[] methods) {
    for (ITestNGMethod m : methods) {
      m_dependencies.put(m.getQualifiedName(), m);
      for (String g : m.getGroups()) {
        m_groups.put(g, m);
      }
    }
  }

  public List<ITestNGMethod> getMethodsThatBelongTo(String group, ITestNGMethod fromMethod) {
    Set<String> uniqueKeys = m_groups.keySet();
    Pattern pattern = Pattern.compile(group);

    List<ITestNGMethod> result =
        m_groups.keySet().stream()
            .parallel()
            .filter(k -> pattern.matcher(k).matches())
            .flatMap(k -> m_groups.get(k).stream())
            .collect(Collectors.toList());

    for (String k : uniqueKeys) {
      if (Pattern.matches(group, k)) {
        result.addAll(m_groups.get(k));
      }
    }

    if (result.isEmpty() && !fromMethod.ignoreMissingDependencies()) {
      throw new TestNGException(
          "DependencyMap::Method \""
              + fromMethod
              + "\" depends on nonexistent group \""
              + group
              + "\"");
    } else {
      return result;
    }
  }

  public ITestNGMethod getMethodDependingOn(String methodName, ITestNGMethod fromMethod) {
    List<ITestNGMethod> l = m_dependencies.get(methodName);
    if (l.isEmpty()) {
      // Try to fetch dependencies by using the test class in the method name.
      // This is usually needed in scenarios wherein a child class overrides a base class method.
      // So the dependency name needs to be adjusted to use the test class name instead of using the
      // declared class.
      l = m_dependencies.get(constructMethodNameUsingTestClass(methodName, fromMethod));
    }
    if (l.isEmpty() && fromMethod.ignoreMissingDependencies()) {
      return fromMethod;
    }
    Optional<ITestNGMethod> found =
        l.stream()
            .parallel()
            .filter(
                m ->
                    isSameInstance(fromMethod, m)
                        || belongToDifferentClassHierarchy(fromMethod, m)
                        || hasInstance(fromMethod, m))
            .findFirst();
    if (found.isPresent()) {
      return found.get();
    }

    throw new TestNGException(
        "Method \"" + fromMethod + "\" depends on nonexistent method \"" + methodName + "\"");
  }

  private static boolean belongToDifferentClassHierarchy(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    return !baseClassMethod.getRealClass().isAssignableFrom(derivedClassMethod.getRealClass());
  }

  private static boolean hasInstance(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    Object baseInstance = baseClassMethod.getInstance();
    Object derivedInstance = derivedClassMethod.getInstance();
    boolean result = derivedInstance != null || baseInstance != null;
    boolean params =
        null != baseClassMethod.getFactoryMethodParamsInfo()
            && null != derivedClassMethod.getFactoryMethodParamsInfo().getParameters();

    if (result && params && RuntimeBehavior.enforceThreadAffinity()) {
      return hasSameParameters(baseClassMethod, derivedClassMethod);
    }
    return result;
  }

  private static boolean hasSameParameters(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    return baseClassMethod
        .getFactoryMethodParamsInfo()
        .getParameters()[0]
        .equals(derivedClassMethod.getFactoryMethodParamsInfo().getParameters()[0]);
  }

  private static boolean isSameInstance(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    Object baseInstance = baseClassMethod.getInstance();
    Object derivedInstance = derivedClassMethod.getInstance();
    boolean nonNullInstances = derivedInstance != null && baseInstance != null;
    if (!nonNullInstances) {
      return false;
    }
    if (null != baseClassMethod.getFactoryMethodParamsInfo()
        && RuntimeBehavior.enforceThreadAffinity()) {
      return baseInstance.getClass().isAssignableFrom(derivedInstance.getClass())
          && hasSameParameters(baseClassMethod, derivedClassMethod);
    }
    return baseInstance.getClass().isAssignableFrom(derivedInstance.getClass());
  }

  private static String constructMethodNameUsingTestClass(
      String currentMethodName, ITestNGMethod m) {
    int lastIndex = currentMethodName.lastIndexOf('.');
    if (lastIndex != -1) {
      return m.getTestClass().getRealClass().getName() + currentMethodName.substring(lastIndex);
    }
    return currentMethodName;
  }
}
