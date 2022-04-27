package org.testng.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import org.testng.ITestNGMethod;
import org.testng.collections.CollectionUtils;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.log4testng.Logger;

/**
 * This class wraps access to beforeGroups and afterGroups methods, since they are passed around the
 * various invokers and potentially modified in different threads.
 *
 * @since 5.3 (Mar 2, 2006)
 */
public class ConfigurationGroupMethods {

  /** The list of beforeGroups methods keyed by the name of the group */
  private final Map<String, List<ITestNGMethod>> m_beforeGroupsMethods;

  private final Map<String, CountDownLatch> beforeGroupsThatHaveAlreadyRun =
      new ConcurrentHashMap<>();
  private final Set<String> afterGroupsThatHaveAlreadyRun =
      Collections.newSetFromMap(new ConcurrentHashMap<>());

  /** The list of afterGroups methods keyed by the name of the group */
  private final Map<String, List<ITestNGMethod>> m_afterGroupsMethods;

  /** The list of all test methods */
  private final ITestNGMethod[] m_allMethods;

  /** A map that returns the last method belonging to the given group */
  private volatile Map<String, List<ITestNGMethod>> m_afterGroupsMap = null;

  public ConfigurationGroupMethods(
      IContainer<ITestNGMethod> container,
      Map<String, List<ITestNGMethod>> beforeGroupsMethods,
      Map<String, List<ITestNGMethod>> afterGroupsMethods) {
    m_allMethods = container.getItems();
    m_beforeGroupsMethods = new ConcurrentHashMap<>(beforeGroupsMethods);
    m_afterGroupsMethods = new ConcurrentHashMap<>(afterGroupsMethods);
  }

  public Map<String, List<ITestNGMethod>> getBeforeGroupsMethods() {
    return m_beforeGroupsMethods;
  }

  public Map<String, List<ITestNGMethod>> getAfterGroupsMethods() {
    return m_afterGroupsMethods;
  }

  public List<ITestNGMethod> getBeforeGroupMethodsForGroup(String[] groups) {
    if (groups.length == 0) {
      return Collections.emptyList();
    }

    synchronized (beforeGroupsThatHaveAlreadyRun) {
      return Arrays.stream(groups)
          .map(t -> retrieve(beforeGroupsThatHaveAlreadyRun, m_beforeGroupsMethods, t))
          .filter(Objects::nonNull)
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    }
  }

  public List<ITestNGMethod> getAfterGroupMethods(ITestNGMethod testMethod) {
    if (testMethod.hasMoreInvocation() || testMethod.getGroups().length == 0) {
      return Collections.emptyList();
    }

    Set<String> methodGroups = new HashSet<>(Arrays.asList(testMethod.getGroups()));

    synchronized (afterGroupsThatHaveAlreadyRun) {
      if (m_afterGroupsMap == null) {
        m_afterGroupsMap = initializeAfterGroupsMap();
      }

      return methodGroups.stream()
          .filter(t -> isLastMethodForGroup(t, testMethod))
          .map(t -> retrieve(afterGroupsThatHaveAlreadyRun, m_afterGroupsMethods, t))
          .filter(Objects::nonNull)
          .flatMap(Collection::stream)
          .filter(t -> isAfterGroupAllowedToRunAfterTestMethod(t, methodGroups))
          .collect(Collectors.toList());
    }
  }

  private boolean isAfterGroupAllowedToRunAfterTestMethod(
      ITestNGMethod afterGroupMethod, Set<String> testMethodGroups) {
    String[] afterGroupMethodGroups = afterGroupMethod.getAfterGroups();
    if (afterGroupMethodGroups.length == 1
        || testMethodGroups.containsAll(Arrays.asList(afterGroupMethodGroups))) {
      return true;
    }
    return Arrays.stream(afterGroupMethodGroups)
        .allMatch(
            t ->
                testMethodGroups.contains(t)
                    || !CollectionUtils.hasElements(m_afterGroupsMap.get(t)));
  }

  public void removeBeforeGroups(String[] groups) {
    for (String group : groups) {
      m_beforeGroupsMethods.remove(group);
      beforeGroupsThatHaveAlreadyRun.get(group).countDown();
    }
  }

  public void removeAfterGroups(Collection<String> groups) {
    for (String group : groups) {
      m_afterGroupsMethods.remove(group);
    }
  }

  /**
   * @param group The group name
   * @param method The test method
   * @return true if the passed method is the last to run for the group. This method is used to
   *     figure out when is the right time to invoke afterGroups methods.
   */
  private boolean isLastMethodForGroup(String group, ITestNGMethod method) {
    List<ITestNGMethod> methodsInGroup = m_afterGroupsMap.get(group);

    if (null == methodsInGroup || methodsInGroup.isEmpty()) {
      return true;
    }

    methodsInGroup.remove(method);

    // Note:  == is not good enough here as we may work with ITestNGMethod clones
    return methodsInGroup.isEmpty();
  }

  private Map<String, List<ITestNGMethod>> initializeAfterGroupsMap() {
    Map<String, List<ITestNGMethod>> result = Maps.newConcurrentMap();
    for (ITestNGMethod m : m_allMethods) {
      String[] groups = m.getGroups();
      for (String g : groups) {
        List<ITestNGMethod> methodsInGroup = result.computeIfAbsent(g, key -> Lists.newArrayList());
        methodsInGroup.add(m);
      }
    }

    synchronized (afterGroupsThatHaveAlreadyRun) {
      afterGroupsThatHaveAlreadyRun.clear();
    }

    return result;
  }

  private static List<ITestNGMethod> retrieve(
      Map<String, CountDownLatch> tracker, Map<String, List<ITestNGMethod>> map, String group) {
    if (tracker.containsKey(group)) {
      try {
        tracker.get(group).await();
      } catch (InterruptedException handled) {
        Logger.getLogger(ConfigurationGroupMethods.class).error(handled.getMessage(), handled);
        Thread.currentThread().interrupt();
      }
      return Collections.emptyList();
    }
    tracker.put(group, new CountDownLatch(1));
    return map.get(group);
  }

  private static List<ITestNGMethod> retrieve(
      Set<String> tracker, Map<String, List<ITestNGMethod>> map, String group) {
    if (tracker.contains(group)) {
      return Collections.emptyList();
    }
    tracker.add(group);
    return map.get(group);
  }
}
