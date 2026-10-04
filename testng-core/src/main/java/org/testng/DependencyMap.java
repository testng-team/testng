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
import org.testng.internal.Utils;

/**
 * Finds the test methods that a test method depends on.
 *
 * <p>It looks them up by method name for {@code dependsOnMethods}, and by group name for {@code
 * dependsOnGroups}.
 */
public class DependencyMap {
  private final ListMultiMap<String, ITestNGMethod> m_dependencies = Maps.newListMultiMap();
  private final ListMultiMap<String, ITestNGMethod> m_groups = Maps.newListMultiMap();

  /**
   * Records each test method under its full name and under each of its groups.
   *
   * @param methods the test methods to look in.
   */
  public DependencyMap(ITestNGMethod[] methods) {
    for (ITestNGMethod m : methods) {
      m_dependencies.put(m.getQualifiedName(), m);
      for (String g : m.getGroups()) {
        m_groups.put(g, m);
      }
    }
  }

  /**
   * Returns the test methods in the groups whose names match a regular expression.
   *
   * <p>Note: the list holds each matching method twice (GITHUB-3564). The callers in TestNG put the
   * list into a set or a map, so the second copy does not change a run.
   *
   * @param group a regular expression for the group names.
   * @param fromMethod the test method that depends on the groups.
   * @return the test methods in the matching groups. The list is empty when no group matches and
   *     {@code fromMethod} ignores missing dependencies.
   * @throws TestNGException when no group matches and {@code fromMethod} does not ignore missing
   *     dependencies.
   */
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

  /**
   * Finds the test method that {@code fromMethod} depends on through {@code methodName}.
   *
   * <p>When no test method has the name {@code methodName}, and {@code fromMethod} ignores missing
   * dependencies, this method returns {@code fromMethod} itself.
   *
   * @param methodName the name of the method that {@code fromMethod} depends on.
   * @param fromMethod the test method that has the dependency.
   * @return the test method that {@code fromMethod} depends on, or {@code fromMethod} itself.
   * @throws TestNGException when this method finds no matching test method, and cannot return
   *     {@code fromMethod}.
   */
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
      // Try again with the test class name in place of the class name in methodName. This helps
      // when a child class overrides a method of its base class. The dependency names the base
      // class, but TestNG lists the method under the child class.
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
    // Check for an instance through the instance id. Reading the instance itself would make a lazy
    // @Factory create it while TestNG collects the methods.
    boolean result =
        IInstanceIdentity.carriesInstance(derivedClassMethod)
            || IInstanceIdentity.carriesInstance(baseClassMethod);
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
    // A factory parameter can be null. Objects.equals accepts null, so this check does not throw.
    return Objects.equals(firstParams[0], secondParams[0]);
  }

  private static boolean isSameInstance(
      ITestNGMethod baseClassMethod, ITestNGMethod derivedClassMethod) {
    boolean bothCarryAnInstance =
        IInstanceIdentity.carriesInstance(derivedClassMethod)
            && IInstanceIdentity.carriesInstance(baseClassMethod);
    if (!bothCarryAnInstance) {
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
   * Returns the class of the instance of a test method, without creating a lazy instance.
   *
   * <p>An instance that does not exist yet comes from a lazy constructor {@code @Factory}. That
   * factory creates objects of the real class of the method, so this method returns that class.
   *
   * @return the class of the instance, or the real class of the method when there is no instance.
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
      return Utils.requireTestClassOf(m).getRealClass().getName()
          + currentMethodName.substring(lastIndex);
    }
    return currentMethodName;
  }
}
