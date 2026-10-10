package org.testng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Sets the order in which TestNG runs its listeners.
 *
 * <p>TestNG does <em>not</em> use it to order the {@link IReporter} implementations.
 *
 * <p>You can give TestNG an implementation in two ways:
 *
 * <ol>
 *   <li>Call {@link TestNG#setListenerComparator(ListenerComparator)} when you run TestNG from
 *       code.
 *   <li>Use the {@code -listenercomparator} option when you run TestNG from the command line or
 *       from a build tool.
 * </ol>
 */
@FunctionalInterface
public interface ListenerComparator extends Comparator<ITestNGListener> {
  /**
   * Returns the listeners of a list in the order of a comparator.
   *
   * @param list the listeners to sort.
   * @param comparator the comparator, or {@code null} to keep the order of {@code list}.
   * @return a read-only list. It is a sorted copy of {@code list}. When {@code comparator} is
   *     {@code null}, it is a view of {@code list}, not a copy.
   */
  static <T extends ITestNGListener> List<T> sort(
      List<T> list, @Nullable ListenerComparator comparator) {
    if (comparator == null) {
      return Collections.unmodifiableList(list);
    }
    List<T> original = new ArrayList<>(list);
    original.sort(comparator);
    return Collections.unmodifiableList(original);
  }

  /**
   * Returns the listeners of a collection in the order of a comparator.
   *
   * @param list the listeners to sort.
   * @param comparator the comparator, or {@code null} to keep the order of {@code list}.
   * @return a read-only collection. It is a sorted copy of {@code list}. When {@code comparator} is
   *     {@code null}, it is a view of {@code list}, not a copy.
   */
  static <T extends ITestNGListener> Collection<T> sort(
      Collection<T> list, @Nullable ListenerComparator comparator) {
    if (comparator == null) {
      return Collections.unmodifiableCollection(list);
    }
    List<T> original = new ArrayList<>(list);
    original.sort(comparator);
    return Collections.unmodifiableCollection(original);
  }
}
