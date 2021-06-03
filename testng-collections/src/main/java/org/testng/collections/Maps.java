package org.testng.collections;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Maps {

  public static <K, V> Map<K, V> newHashMap() {
    return new HashMap<>();
  }

  public static <K, V> Map<K, V> newHashtable() {
    return new Hashtable<>();
  }

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

  public static <K, V> Map<K, V> newLinkedHashMap() {
    return new LinkedHashMap<>();
  }

  public static <K, V> Map<K, V> newHashMap(Map<K, V> parameters) {
    return new HashMap<>(parameters);
  }
}
