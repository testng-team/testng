package org.testng;

/**
 * Listener interface that can be used to determine listener execution order. This interface will
 * NOT be used to determine execution order for {@link IReporter} implementations.
 *
 * <p>An implementation can be plugged into TestNG either via:
 *
 * <ol>
 *   <li>{@link TestNG#setListenerComparator(ListenerComparator)} if you are using the {@link
 *       TestNG} APIs.
 *   <li>Via the configuration parameter <code>-listenercomparator</code> if you are using a build
 *       tool
 * </ol>
 */
@FunctionalInterface
public interface ListenerComparator {

  /**
   * @param l1 - First {@link ITestNGListener} object to be compared.
   * @param l2 - Second {@link ITestNGListener} object to be compared.
   * @return - a negative integer, zero, or a positive integer as the first argument is less than,
   *     equal to, or greater than the second.
   */
  int compare(ITestNGListener l1, ITestNGListener l2);
}
