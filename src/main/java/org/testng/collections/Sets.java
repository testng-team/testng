package org.testng.collections;

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

  public static <V> Set<V> newLinkedHashSet() {
    return new LinkedHashSet<>();
  }
}
