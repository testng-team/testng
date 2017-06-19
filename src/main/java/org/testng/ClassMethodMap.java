package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.XmlMethodSelector;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class maintains a map of {@code <Class, List<ITestNGMethod>>}.
 * It is used by TestWorkers to determine if the method they just ran
 * is the last of its class, in which case it's time to invoke all the
 * afterClass methods.
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class ClassMethodMap {
  private Map<Object, List<ITestNGMethod>> classMap = Maps.newHashMap();
  // These two variables are used throughout the workers to keep track
  // of what beforeClass/afterClass methods have been invoked
  private Map<ITestClass, Set<Object>> beforeClassMethods = Maps.newHashMap();
  private Map<ITestClass, Set<Object>> afterClassMethods = Maps.newHashMap();

  public ClassMethodMap(List<ITestNGMethod> methods, XmlMethodSelector xmlMethodSelector) {
    for (ITestNGMethod m : methods) {
      // Only add to the class map methods that are included in the
      // method selector. We can pass a null context here since the selector
      // should already have been initialized
      if (xmlMethodSelector != null && ! xmlMethodSelector.includeMethod(null, m, true)) {
        continue;
      }

      Object instance = m.getInstance();
      List<ITestNGMethod> l = classMap.get(instance);
      if (l == null) {
        l = Lists.newArrayList();
        classMap.put(instance, l);
      }
      l.add(m);
    }
  }

  /**
   * Remove the method from this map and returns true if it is the last
   * of its class.
   */
  public synchronized boolean removeAndCheckIfLast(ITestNGMethod m, Object instance) {
    List<ITestNGMethod> l = classMap.get(instance);
    if (l == null) {
      throw new AssertionError("l should not be null");
    }
    l.remove(m);
    // It's the last method of this class if all the methods remaining in the list belong to a
    // different class
    for (ITestNGMethod tm : l) {
      if (tm.getEnabled() && tm.getTestClass().equals(m.getTestClass())) {
        return false;
      }
    }
    return true;
  }

  public Map<ITestClass, Set<Object>> getInvokedBeforeClassMethods() {
    return beforeClassMethods;
  }

  public Map<ITestClass, Set<Object>> getInvokedAfterClassMethods() {
    return afterClassMethods;
  }

  public void clear() {
    for(Set<Object> instances: beforeClassMethods.values()) {
      instances.clear();
    }
    for(Set<Object> instances: afterClassMethods.values()) {
      instances.clear();
    }
    beforeClassMethods.clear();
    afterClassMethods.clear();
  }
}
