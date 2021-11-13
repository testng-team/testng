package org.testng.collections;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public final class Lists {

  private Lists() {}

  public static <K> List<K> newArrayList() {
    return new ArrayList<>();
  }

  public static <K> List<K> newLinkedList() {
    return new LinkedList<>();
  }

  public static <K> List<K> newLinkedList(Collection<K> c) {
    return new LinkedList<>(c);
  }

  public static <K> List<K> newArrayList(Collection<K> c) {
    return new ArrayList<>(c);
  }

  public static <K> List<K> newArrayList(Iterator<K> c) {
    List<K> result = new ArrayList<>();
    while (c.hasNext()) {
      result.add(c.next());
    }
    return result;
  }

  @SafeVarargs
  public static <K> List<K> newArrayList(K... elements) {
    List<K> result = new ArrayList<>();
    Collections.addAll(result, elements);
    return result;
  }

  public static <K> List<K> newArrayList(int size) {
    return new ArrayList<>(size);
  }

  public static <K> List<K> intersection(List<K> list1, List<K> list2) {
    return list1.stream().filter(list2::contains).collect(Collectors.toList());
  }

  public static <K> List<K> merge(Collection<K> l1, Collection<K> l2) {
    List<K> result = newArrayList(l1);
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
    List<T> result = newArrayList(l1);
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

  public static <K> List<K> newReversedArrayList(Collection<K> c) {
    List<K> list = newArrayList(c);
    Collections.reverse(list);
    return list;
  }
}
