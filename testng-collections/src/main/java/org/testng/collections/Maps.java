package org.testng.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Maps {

  /** @deprecated Use {@code new HashMap<>()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K, V> Map<K, V> newHashMap() {
    return new HashMap<>();
  }

  /**
   * @deprecated Use {@code new Hashtable<>()} instead, or preferably a {@link ConcurrentHashMap}.
   */
  @Deprecated(forRemoval = true, since = "7.13.0")
  // The Hashtable is this factory's published contract, not a choice made inside it:
  // org.testng.collections is Export-Package'd and a caller may already depend on the type it
  // answers, synchronization included. It has no call site left in TestNG and the javadoc above
  // names the replacement.
  @SuppressWarnings("JdkObsolete")
  public static <K, V> Map<K, V> newHashtable() {
    return new Hashtable<>();
  }

  /** @deprecated Use {@code new ConcurrentHashMap<>()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K, V> Map<K, V> newConcurrentMap() {
    return new ConcurrentHashMap<>();
  }

  public static <K, V> ListMultiMap<K, V> newListMultiMap() {
    return new ListMultiMap<>(false);
  }

  public static <K, V> ListMultiMap<K, V> newSortedListMultiMap() {
    return new ListMultiMap<>(true);
  }

  public static <K, V> SetMultiMap<K, V> newSetMultiMap() {
    return new SetMultiMap<>(false);
  }

  /** @deprecated Use {@code new LinkedHashMap<>()} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K, V> Map<K, V> newLinkedHashMap() {
    return new LinkedHashMap<>();
  }

  /** @deprecated Use {@code Collections.synchronizedMap(new LinkedHashMap<>())} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K, V> Map<K, V> synchronizedLinkedHashMap() {
    return Collections.synchronizedMap(new LinkedHashMap<>());
  }

  /** @deprecated Use {@code new HashMap<>(parameters)} instead. */
  @Deprecated(forRemoval = true, since = "7.13.0")
  public static <K, V> Map<K, V> newHashMap(Map<K, V> parameters) {
    return new HashMap<>(parameters);
  }
}
