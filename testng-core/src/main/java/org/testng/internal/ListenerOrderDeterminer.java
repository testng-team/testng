package org.testng.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.testng.collections.Lists;
import org.testng.internal.collections.Pair;

/**
 * A Utility that helps us differentiate between a user's listener and an IDE Listener.
 *
 * <p>When dealing with TestNG listeners we would need to ensure that the user created listeners are
 * invoked first followed by IDE listeners. This is required so that we always honour any state
 * changes that a user's listener may have done to the internal state of objects that can affect the
 * outcome of the execution (for e.g.,{@link org.testng.ITestResult})
 *
 * <p>The ordering must be done such that, when dealing with "beforeXXX|afterXXX" we group all the
 * IDE listeners at the end. That way, we can always ensure that the IDE listeners always honour the
 * changes that were done to the TestNG internal states and give a consistent experience for users
 * in the IDE.
 */
public final class ListenerOrderDeterminer {

  private ListenerOrderDeterminer() {
    // Defeat instantiation
  }

  private static final List<String> IDE_PACKAGES =
      RuntimeBehavior.getIdePackageNames().stream()
          .map(each -> each.replaceAll("\\Q.*\\E", ""))
          .collect(Collectors.toList());

  private static final Predicate<Class<?>> IDE_LISTENER =
      clazz -> IDE_PACKAGES.stream().anyMatch(each -> clazz.getPackage().getName().contains(each));

  /**
   * @param original - The original collection of listeners
   * @return - A re-ordered collection wherein TestNG's known listeners are added at the end
   */
  public static <T> List<T> order(Collection<T> original) {
    Pair<List<T>, List<T>> ordered = arrange(original);
    List<T> ideListeners = ordered.first();
    List<T> regularListeners = ordered.second();
    return Lists.merge(regularListeners, ideListeners);
  }

  /**
   * @param original - The original collection of listeners
   * @return - A reversed ordered list wherein the user listeners are found in reverse order
   *     followed by TestNG known listeners also in reverse order.
   */
  public static <T> List<T> reversedOrder(Collection<T> original) {
    Pair<List<T>, List<T>> ordered = arrange(original);
    List<T> ideListeners = ordered.first();
    List<T> regularListeners = ordered.second();
    Collections.reverse(regularListeners);
    Collections.reverse(ideListeners);
    return Lists.merge(regularListeners, ideListeners);
  }

  private static boolean isIDEListener(Class<?> clazz) {
    return IDE_LISTENER.test(clazz);
  }

  private static <T> Pair<List<T>, List<T>> arrange(Collection<T> original) {
    List<T> ideListeners = new ArrayList<>();
    List<T> regularListeners = new ArrayList<>();
    original.stream()
        .filter(Objects::nonNull)
        .forEach(
            each -> {
              if (isIDEListener(each.getClass())) {
                ideListeners.add(each);
              } else {
                regularListeners.add(each);
              }
            });
    return new Pair<>(ideListeners, regularListeners);
  }
}
