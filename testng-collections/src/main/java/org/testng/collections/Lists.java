package org.testng.collections;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public final class Lists {

  private Lists() {}

  /** @deprecated Use {@code new ArrayList<>()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K> List<K> newArrayList() {
    return new ArrayList<>();
  }

  /** @deprecated Use {@code new LinkedList<>()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  // The LinkedList is this factory's published contract, not a choice made inside it:
  // org.testng.collections is Export-Package'd and a caller may already depend on the type it
  // answers. It has no call site left in TestNG, so the answer is its removal.
  @SuppressWarnings("JdkObsolete")
  public static <K> List<K> newLinkedList() {
    return new LinkedList<>();
  }

  /** @deprecated Use {@code new LinkedList<>(c)} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  // See newLinkedList(): the type is the contract.
  @SuppressWarnings("JdkObsolete")
  public static <K> List<K> newLinkedList(Collection<K> c) {
    return new LinkedList<>(c);
  }

  /** @deprecated Use {@code new ArrayList<>(c)} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K> List<K> newArrayList(Collection<K> c) {
    return new ArrayList<>(c);
  }

  /**
   * @deprecated Drain the iterator into a list of your own, or wrap it: {@code Iterable<K> it = ()
   *     -> c;}
   */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K> List<K> newArrayList(Iterator<K> c) {
    List<K> result = new ArrayList<>();
    while (c.hasNext()) {
      result.add(c.next());
    }
    return result;
  }

  /** @deprecated Use {@code new ArrayList<>(Arrays.asList(elements))} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  @SafeVarargs
  public static <K> List<K> newArrayList(K... elements) {
    List<K> result = new ArrayList<>();
    Collections.addAll(result, elements);
    return result;
  }

  /**
   * @deprecated Use {@code Arrays.stream(elements).flatMap(Arrays::stream)} and collect it
   *     yourself.
   */
  @Deprecated(forRemoval = true, since = "7.13.0")
  @SafeVarargs
  public static <K> List<K> newArrayList(K[]... elements) {
    return Arrays.stream(elements)
        .map(Arrays::asList)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  /** @deprecated Use {@code new ArrayList<>(size)} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K> List<K> newArrayList(int size) {
    return new ArrayList<>(size);
  }

  public static <K> List<K> intersection(List<K> list1, List<K> list2) {
    return list1.stream().filter(list2::contains).collect(Collectors.toList());
  }

  public static <K> List<K> merge(Collection<K> l1, Collection<K> l2) {
    List<K> result = new ArrayList<>(l1);
    result.addAll(l2);
    return result;
  }

  /**
   * Utility method that merges two lists by applying the provided condition.
   *
   * @param <T> - The generic type
   * @param l1 - The first list
   * @param condition - The condition that is used to determine if an element is to be added or not.
   * @param lists - The lists which are to be merged into the first list
   * @return - The merged list.
   */
  @SafeVarargs
  public static <T> List<T> merge(List<T> l1, BiPredicate<T, T> condition, List<T>... lists) {
    List<T> result = new ArrayList<>(l1);
    Arrays.stream(lists)
        .flatMap(Collection::stream)
        .forEach(
            eachItem -> {
              boolean exists = result.stream().anyMatch(e -> condition.test(e, eachItem));
              if (!exists) {
                result.add(eachItem);
              }
            });
    return result;
  }
}
