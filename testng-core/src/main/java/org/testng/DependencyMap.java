package org.testng;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.IInstanceIdentity;
import org.testng.internal.MethodHelper;
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
      ITestNGMethod[] array =
          m_dependencies.values().stream()
              .flatMap(Collection::stream)
              .toArray(ITestNGMethod[]::new);
      l = Arrays.asList(MethodHelper.findDependedUponMethods(fromMethod, array));
    }
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
        "Method \""
            + fromMethod.getQualifiedName()
            + "()\" depends on nonexistent method \""
            + methodName
            + "\"");
  }

  private static boolean belongToDifferentClassHierarchy(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    Class<?> clazz = baseClassMethod.getRealClass();
    return !clazz.isAssignableFrom(derivedClassMethod.getRealClass());
  }

  private static boolean hasInstance(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    // Check for the presence of an instance via the per-instance id so a lazy @Factory instance is
    // not created just to resolve dependencies during collection.
    boolean result =
        IInstanceIdentity.getInstanceId(derivedClassMethod) != null
            || IInstanceIdentity.getInstanceId(baseClassMethod) != null;
    boolean params = baseClassMethod.getFactoryInstance().isPresent();

    if (result && params && RuntimeBehavior.enforceThreadAffinity()) {
      return hasSameParameters(baseClassMethod, derivedClassMethod);
    }
    return result;
  }

  private static boolean hasSameParameters(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    Optional<IFactoryInstance> first = baseClassMethod.getFactoryInstance();
    Optional<IFactoryInstance> second = derivedClassMethod.getFactoryInstance();
    if (first.isEmpty() || second.isEmpty()) {
      return false;
    }
    Object[] firstParams = first.get().getParameters();
    Object[] secondParams = second.get().getParameters();
    if (firstParams.length == 0 || secondParams.length == 0) {
      return false;
    }
    // A data provider row may legitimately hold null, and this used to be a plain equals() call on
    // it -- comparing thread affinity of two instances is no reason to throw.
    return Objects.equals(firstParams[0], secondParams[0]);
  }

  private static boolean isSameInstance(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    boolean nonNullInstances =
        IInstanceIdentity.getInstanceId(derivedClassMethod) != null
            && IInstanceIdentity.getInstanceId(baseClassMethod) != null;
    if (!nonNullInstances) {
      return false;
    }
    Class<?> baseClass = instanceClassOf(baseClassMethod);
    Class<?> derivedClass = instanceClassOf(derivedClassMethod);
    boolean assignable = baseClass.isAssignableFrom(derivedClass);
    if (baseClassMethod.getFactoryInstance().isPresent()
        && RuntimeBehavior.enforceThreadAffinity()) {
      return assignable && hasSameParameters(baseClassMethod, derivedClassMethod);
    }
    return assignable;
  }

  /**
   * @return - The class of the method's instance, resolved without instantiating a lazy @Factory
   *     instance (a constructor factory produces exactly the method's real class).
   */
  private static Class<?> instanceClassOf(ITestNGMethod method) {
    if (method instanceof BaseTestMethod && !((BaseTestMethod) method).isInstanceInstantiated()) {
      return method.getRealClass();
    }
    Object instance = method.getInstance();
    return instance == null ? method.getRealClass() : instance.getClass();
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
