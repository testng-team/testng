package org.testng;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.testng.collections.Lists;

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
public interface ListenerComparator extends Comparator<ITestNGListener> {
  static <T extends ITestNGListener> List<T> sort(List<T> list, ListenerComparator comparator) {
    if (comparator == null) {
      return Collections.unmodifiableList(list);
    }
    List<T> original = Lists.newArrayList(list);
    original.sort(comparator);
    return Collections.unmodifiableList(original);
  }

  static <T extends ITestNGListener> Collection<T> sort(
      Collection<T> list, ListenerComparator comparator) {
    if (comparator == null) {
      return Collections.unmodifiableCollection(list);
    }
    List<T> original = Lists.newArrayList(list);
    original.sort(comparator);
    return Collections.unmodifiableCollection(original);
  }
}
