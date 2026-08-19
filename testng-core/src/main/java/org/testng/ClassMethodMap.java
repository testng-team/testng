package org.testng;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jspecify.annotations.Nullable;
import org.testng.internal.IInstanceIdentity;
import org.testng.internal.XmlMethodSelector;

/**
 * This class maintains a map of {@code <Class, List<ITestNGMethod>>}. It is used by TestWorkers to
 * determine if the method they just ran is the last of its class, in which case it's time to invoke
 * all the afterClass methods.
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class ClassMethodMap {
  private final Map<Object, Collection<ITestNGMethod>> classMap = new ConcurrentHashMap<>();
  // These two variables are used throughout the workers to keep track
  // of what beforeClass/afterClass methods have been invoked
  private final Map<ITestClass, Set<Object>> beforeClassMethods = new ConcurrentHashMap<>();
  private final Map<ITestClass, Set<Object>> afterClassMethods = new ConcurrentHashMap<>();

  public ClassMethodMap(List<ITestNGMethod> methods, XmlMethodSelector xmlMethodSelector) {
    for (ITestNGMethod m : methods) {
      // Only add to the class map methods that are included in the
      // method selector. We can pass a null context here since the selector
      // should already have been initialized
      if (xmlMethodSelector != null && !xmlMethodSelector.includeMethod(null, m, true)) {
        continue;
      }

      // Key by the per-instance id rather than the instantiated instance so that constructing this
      // map during collection never forces a lazy @Factory instance to be created.
      Object instanceId = IInstanceIdentity.getInstanceId(m);
      classMap.computeIfAbsent(instanceId, k -> new ConcurrentLinkedQueue<>()).add(m);
    }
  }

  /**
   * Remove the method from this map.
   *
   * @param m The test method
   * @param instance The test instance
   * @return true if it is the last of its class
   */
  public boolean removeAndCheckIfLast(ITestNGMethod m, @Nullable Object instance) {
    // Look up by the method's own per-instance id so this matches the id-keyed map above (and never
    // instantiates anything); the passed instance is retained only for the diagnostic message.
    Collection<ITestNGMethod> l = classMap.get(IInstanceIdentity.getInstanceId(m));
    if (l == null) {
      throw new IllegalStateException(
          "Could not find any methods associated with test class instance " + instance);
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
    for (Set<Object> instances : beforeClassMethods.values()) {
      instances.clear();
    }
    for (Set<Object> instances : afterClassMethods.values()) {
      instances.clear();
    }
    beforeClassMethods.clear();
    afterClassMethods.clear();
  }
}
