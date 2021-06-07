package org.testng.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

/**
 * This class wraps access to beforeGroups and afterGroups methods, since they are passed around the
 * various invokers and potentially modified in different threads.
 *
 * @since 5.3 (Mar 2, 2006)
 */
public class ConfigurationGroupMethods {

  /** The list of beforeGroups methods keyed by the name of the group */
  private final Map<String, List<ITestNGMethod>> m_beforeGroupsMethods;

  private final Set<String> beforeGroupsThatHaveAlreadyRun =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
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

  /**
   * @param group The group name
   * @param method The test method
   * @return true if the passed method is the last to run for the group. This method is used to
   *     figure out when is the right time to invoke afterGroups methods.
   */
  public boolean isLastMethodForGroup(String group, ITestNGMethod method) {

    // If we have more invocation to do, this is not the last one yet
    if (method.hasMoreInvocation()) {
      return false;
    }

    // This Mutex ensures that this edit check runs sequentially for one ITestNGMethod
    // method at a time because this object is being shared between all the ITestNGMethod objects.
    synchronized (this) {
      if (m_afterGroupsMap == null) {
        m_afterGroupsMap = initializeAfterGroupsMap();
      }

      List<ITestNGMethod> methodsInGroup = m_afterGroupsMap.get(group);

      if (null == methodsInGroup || methodsInGroup.isEmpty()) {
        return false;
      }

      methodsInGroup.remove(method);

      // Note:  == is not good enough here as we may work with ITestNGMethod clones
      return methodsInGroup.isEmpty();
    }
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

  public List<ITestNGMethod> getBeforeGroupMethodsForGroup(String group) {
    synchronized (beforeGroupsThatHaveAlreadyRun) {
      return retrieve(beforeGroupsThatHaveAlreadyRun, m_beforeGroupsMethods, group);
    }
  }

  public List<ITestNGMethod> getAfterGroupMethodsForGroup(String group) {
    synchronized (afterGroupsThatHaveAlreadyRun) {
      return retrieve(afterGroupsThatHaveAlreadyRun, m_afterGroupsMethods, group);
    }
  }

  public void removeBeforeGroups(String[] groups) {
    for (String group : groups) {
      m_beforeGroupsMethods.remove(group);
    }
  }

  public void removeAfterGroups(Collection<String> groups) {
    for (String group : groups) {
      m_afterGroupsMethods.remove(group);
    }
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
