package org.testng.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
}
