package org.testng.internal;


import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;

/**
 * This class wraps access to beforeGroups and afterGroups methods,
 * since they are passed around the various invokers and potentially
 * modified in different threads.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 * @since 5.3 (Mar 2, 2006)
 */
public class ConfigurationGroupMethods implements Serializable {
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID= 1660798519864898480L;

  /** The list of beforeGroups methods keyed by the name of the group */
  private final Map<String, List<ITestNGMethod>> m_beforeGroupsMethods;

  /** The list of afterGroups methods keyed by the name of the group */
  private final Map<String, List<ITestNGMethod>> m_afterGroupsMethods;

  /** The list of all test methods */
  private final ITestNGMethod[] m_allMethods;

  /**A map that returns the last method belonging to the given group */
  private Map<String, ITestNGMethod> m_afterGroupsMap= null;

  public ConfigurationGroupMethods(ITestNGMethod[] allMethods,
                                   Map<String, List<ITestNGMethod>> beforeGroupsMethods,
                                   Map<String, List<ITestNGMethod>> afterGroupsMethods) {
    m_allMethods= allMethods;
    m_beforeGroupsMethods= beforeGroupsMethods;
    m_afterGroupsMethods= afterGroupsMethods;
  }

  /**
   * @return true if the passed method is the last to run for the group.
   * This method is used to figure out when is the right time to invoke
   * afterGroups methods.
   */
  public synchronized boolean isLastMethodForGroup(String group, ITestNGMethod method) {

    // If we have more invocation to do, this is not the last
    // one yet
    int invocationCount= method.getCurrentInvocationCount();
    if(invocationCount < (method.getInvocationCount() * method.getParameterInvocationCount())) {
      return false;
    }

    // Lazy initialization since we might never be called
    if(m_afterGroupsMap == null) {
      m_afterGroupsMap= new HashMap<String, ITestNGMethod>();
      for(ITestNGMethod m : m_allMethods) {
        String[] groups= m.getGroups();
        for(String g : groups) {
          m_afterGroupsMap.put(g, m);
        }
      }
    }

    // Note:  == is good enough here
    return m_afterGroupsMap.get(group) == method;

  }

  public synchronized void removeBeforeMethod(String group, ITestNGMethod method) {
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

  synchronized public Map<String, List<ITestNGMethod>> getBeforeGroupsMap() {
    return m_beforeGroupsMethods;
  }

  synchronized public Map<String, List<ITestNGMethod>> getAfterGroupsMap() {
    return m_afterGroupsMethods;
  }

  synchronized public void removeBeforeGroups(String[] groups) {
    for(String group : groups) {
//      log("Removing before group " + group);
      m_beforeGroupsMethods.remove(group);
    }
  }

  synchronized public void removeAfterGroups(Collection<String> groups) {
    for(String group : groups) {
//      log("Removing before group " + group);
      m_afterGroupsMethods.remove(group);
    }
  }

}
