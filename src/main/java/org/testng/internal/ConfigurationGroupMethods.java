package org.testng.internal;


import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class wraps access to beforeGroups and afterGroups methods,
 * since they are passed around the various invokers and potentially
 * modified in different threads.
 *
 * @since 5.3 (Mar 2, 2006)
 */
public class ConfigurationGroupMethods {

  /** The list of beforeGroups methods keyed by the name of the group */
  private final Map<String, List<ITestNGMethod>> m_beforeGroupsMethods;

  /** The list of afterGroups methods keyed by the name of the group */
  private final Map<String, List<ITestNGMethod>> m_afterGroupsMethods;

  /** The list of all test methods */
  private final ITestNGMethod[] m_allMethods;

  /**A map that returns the last method belonging to the given group */
  private volatile Map<String, List<ITestNGMethod>> m_afterGroupsMap = null;

  public ConfigurationGroupMethods(ITestNGMethod[] allMethods,
                                   Map<String, List<ITestNGMethod>> beforeGroupsMethods,
                                   Map<String, List<ITestNGMethod>> afterGroupsMethods)
  {
    m_allMethods = allMethods;
    m_beforeGroupsMethods = new ConcurrentHashMap<>(beforeGroupsMethods);
    m_afterGroupsMethods = new ConcurrentHashMap<>(afterGroupsMethods);
  }

  public ITestNGMethod[] getAllTestMethods() {
    return this.m_allMethods;
  }

  public Map<String, List<ITestNGMethod>> getBeforeGroupsMethods() {
    return m_beforeGroupsMethods;
  }

  public Map<String, List<ITestNGMethod>> getAfterGroupsMethods() {
    return m_afterGroupsMethods;
  }

  /**
   * @return true if the passed method is the last to run for the group.
   * This method is used to figure out when is the right time to invoke
   * afterGroups methods.
   */
  public boolean isLastMethodForGroup(String group, ITestNGMethod method) {

    // If we have more invocation to do, this is not the last one yet
    if(method.hasMoreInvocation()) {
      return false;
    }

    // Lazy initialization since we might never be called
    if(m_afterGroupsMap == null) {
      synchronized (this) {
        if (m_afterGroupsMap == null) {
          m_afterGroupsMap = initializeAfterGroupsMap();
        }
      }
    }

    List<ITestNGMethod> methodsInGroup= m_afterGroupsMap.get(group);

    if(null == methodsInGroup || methodsInGroup.isEmpty()) {
      return false;
    }

    methodsInGroup.remove(method);

    // Note:  == is not good enough here as we may work with ITestNGMethod clones
    return methodsInGroup.isEmpty();

  }

  private Map<String, List<ITestNGMethod>> initializeAfterGroupsMap() {
    Map<String, List<ITestNGMethod>> result= Maps.newConcurrentMap();
    for(ITestNGMethod m : m_allMethods) {
      String[] groups= m.getGroups();
      for(String g : groups) {
        List<ITestNGMethod> methodsInGroup= result.get(g);
        if(null == methodsInGroup) {
          methodsInGroup= Lists.newArrayList();
          result.put(g, methodsInGroup);
        }
        methodsInGroup.add(m);
      }
    }

    return result;
  }

  public void removeBeforeMethod(String group, ITestNGMethod method) {
    List<ITestNGMethod> methods= m_beforeGroupsMethods.get(group);
    if(methods != null) {
      Object success= methods.remove(method);
      if(success == null) {
        log("Couldn't remove beforeGroups method " + method + " for group " + group);
      }
    }
    else {
      log("Couldn't find any beforeGroups method for group " + group);
    }
  }

  private void log(String string) {
    Utils.log("ConfigurationGroupMethods", 2, string);
  }

  public Map<String, List<ITestNGMethod>> getBeforeGroupsMap() {
    return m_beforeGroupsMethods;
  }

  public Map<String, List<ITestNGMethod>> getAfterGroupsMap() {
    return m_afterGroupsMethods;
  }

  public void removeBeforeGroups(String[] groups) {
    for(String group : groups) {
//      log("Removing before group " + group);
      m_beforeGroupsMethods.remove(group);
    }
  }

  public void removeAfterGroups(Collection<String> groups) {
    for(String group : groups) {
//      log("Removing before group " + group);
      m_afterGroupsMethods.remove(group);
    }

  }

}
