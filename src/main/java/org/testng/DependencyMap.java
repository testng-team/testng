package org.testng;

import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;

import java.util.List;

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
      m_dependencies.put(/* m.getTestClass().getName() + "." + */ m.getMethodName(), m);
      for (String g : m.getGroups()) {
        m_groups.put(g, m);
      }
    }
  }

  public List<ITestNGMethod> getMethodsThatBelongTo(String group, ITestNGMethod fromMethod) {
    List<ITestNGMethod> result = m_groups.get(group);
    if (result == null) {
      throw new TestNGException("Method \"" + fromMethod
          + "\" depends on nonexistent group \"" + group + "\"");
    } else {
      return result;
    }
  }

  public ITestNGMethod getMethodDependingOn(String methodName, ITestNGMethod fromMethod) {
    List<ITestNGMethod> l = m_dependencies.get(methodName);
    for (ITestNGMethod m : l) {
      // If they are in the same class hierarchy, they must belong to the same instance,
      // otherwise, it's a method depending on a method in a different class so we
      // don't bother checking the instance
      if (fromMethod.getRealClass().isAssignableFrom(m.getRealClass())) {
        if (m.getInstance() == fromMethod.getInstance()) return m;
      } else {
        return m;
      }
    }
    throw new TestNGException("Method \"" + fromMethod
        + "\" depends on nonexistent method \"" + methodName + "\"");
  }
}
