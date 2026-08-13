package org.testng.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Sets {

  private Sets() {}

  /** @deprecated Use {@code new HashSet<>()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <V> Set<V> newHashSet() {
    return new HashSet<>();
  }

  /** @deprecated Use {@code ConcurrentHashMap.newKeySet()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <V> Set<V> newConcurrentHashSet() {
    return ConcurrentHashMap.newKeySet();
  }

  /** @deprecated Use {@code new HashSet<>(c)} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <V> Set<V> newHashSet(Collection<V> c) {
    return new HashSet<>(c);
  }

  /** @deprecated Use {@code new HashSet<>(Arrays.asList(a))} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  @SafeVarargs
  public static <V> Set<V> newHashSet(V... a) {
    return new HashSet<>(Arrays.asList(a));
  }

  /** @deprecated Use {@code new LinkedHashSet<>()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <V> Set<V> newLinkedHashSet() {
    return new LinkedHashSet<>();
  }

  /** @deprecated Use {@code new LinkedHashSet<>(c)} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <V> Set<V> newLinkedHashSet(Collection<V> c) {
    return new LinkedHashSet<>(c);
  }
}
