package org.testng;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jspecify.annotations.Nullable;
import org.testng.internal.IInstanceIdentity;
import org.testng.internal.XmlMethodSelector;

/**
 * Keeps the test methods of one {@code <test>} in lists, one list for each test instance.
 *
 * <p>The worker that runs test methods ({@code TestMethodWorker}) uses this class to decide when to
 * run the class-level configuration methods of a test instance:
 *
 * <ul>
 *   <li>It runs the {@code @BeforeClass} methods before the first test method of the instance.
 *   <li>It runs the {@code @AfterClass} methods after the last test method of the instance.
 * </ul>
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class ClassMethodMap {
  private final Map<Object, Collection<ITestNGMethod>> classMap = new ConcurrentHashMap<>();
  // The worker reads these two maps to decide if the @BeforeClass or @AfterClass methods of a test
  // instance must run.
  private final Map<ITestClass, Set<Object>> beforeClassMethods = new ConcurrentHashMap<>();
  private final Map<ITestClass, Set<Object>> afterClassMethods = new ConcurrentHashMap<>();

  /**
   * Builds the lists from the test methods of a {@code <test>}.
   *
   * @param methods The test methods to put in the lists.
   * @param xmlMethodSelector The selector that decides which test methods the run includes. When it
   *     is {@code null}, this map keeps every test method.
   */
  public ClassMethodMap(
      List<ITestNGMethod> methods, @Nullable XmlMethodSelector xmlMethodSelector) {
    for (ITestNGMethod m : methods) {
      // Skip a test method that the selector excludes. The selector never reads the context, so
      // null is safe here.
      if (xmlMethodSelector != null && !xmlMethodSelector.includeMethod(null, m, true)) {
        continue;
      }

      // Key the lists by the instance id, not by the instance. A lazy @Factory creates a test
      // instance when something first reads it. Reading it here would create every instance too
      // early.
      Object instanceId = IInstanceIdentity.getInstanceId(m);
      classMap.computeIfAbsent(instanceId, k -> new ConcurrentLinkedQueue<>()).add(m);
    }
  }

  /**
   * Removes a test method and tells you if it was the last one of its test class.
   *
   * <p>The worker calls this after a test method runs. When the answer is {@code true}, the worker
   * runs the {@code @AfterClass} methods of the test instance.
   *
   * @param m The test method that finished.
   * @param instance The test instance of the method. This method uses it only in the error message.
   * @return {@code true} when no enabled test method of the same test class is left in the list.
   * @throws IllegalStateException when this map has no list for the instance id of the method.
   */
  public boolean removeAndCheckIfLast(ITestNGMethod m, @Nullable Object instance) {
    // Find the list by the instance id of the method, as the constructor does. This does not
    // create a lazy instance.
    Collection<ITestNGMethod> l = classMap.get(IInstanceIdentity.getInstanceId(m));
    if (l == null) {
      throw new IllegalStateException(
          "Could not find any methods associated with test class instance " + instance);
    }
    l.remove(m);
    // All test methods without an instance share one list, so a list can hold methods of other
    // test classes. Count only the methods of this test class. A disabled method never runs, so
    // it does not count.
    for (ITestNGMethod tm : l) {
      if (tm.getEnabled() && Objects.equals(tm.getTestClass(), m.getTestClass())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the test instances whose {@code @BeforeClass} methods the worker has started.
   *
   * @return For each test class, the set of those test instances.
   */
  public Map<ITestClass, Set<Object>> getInvokedBeforeClassMethods() {
    return beforeClassMethods;
  }

  /**
   * Returns the sets that the worker reads before it runs the {@code @AfterClass} methods.
   *
   * @return For each test class, a set of test instances.
   */
  public Map<ITestClass, Set<Object>> getInvokedAfterClassMethods() {
    return afterClassMethods;
  }

  /** Empties both maps of test instances and every set in them. */
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
