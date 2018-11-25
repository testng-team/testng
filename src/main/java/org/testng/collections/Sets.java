package org.testng.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class Sets {

  private Sets() {}

  public static <V> Set<V> newHashSet() {
    return new HashSet<>();
  }

  public static <V> Set<V> newHashSet(Collection<V> c) {
    return new HashSet<>(c);
  }

  @SafeVarargs
  public static <V> Set<V> newHashSet(V... a) {
    return newHashSet(Arrays.asList(a));
  }

  public static <V> Set<V> newLinkedHashSet() {
    return new LinkedHashSet<>();
  }

  public static <V> Set<V> newLinkedHashSet(Collection<V> c) {
    return new LinkedHashSet<>(c);
  }
}
