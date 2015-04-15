package org.testng.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Lists {

  private Lists() {}

  public static <K> List<K> newArrayList() {
    return new ArrayList<>();
  }

  public static <K> List<K> newArrayList(Collection<K> c) {
    return new ArrayList<>(c);
  }

  public static <K> List<K> newArrayList(K... elements) {
    List<K> result = new ArrayList<>();
    Collections.addAll(result, elements);
    return result;
  }

  public static <K> List<K> newArrayList(int size) {
    return new ArrayList<>(size);
  }
}
