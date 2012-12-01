package org.testng.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Sets {

  public static <V> Set<V> newHashSet() {
    return new HashSet<V>();
  }

  public static <V> Set<V> newHashSet(Collection<V> c) {
    return new HashSet<V>(c);
  }
}
