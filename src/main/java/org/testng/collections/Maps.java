package org.testng.collections;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Maps {

  private Maps() {}

  @Deprecated
  public static <K, V> Map<K,V> newHashMap() {
    return new HashMap<>();
  }

  @Deprecated
  public static <K, V> Map<K,V> newHashtable() {
    return new Hashtable<>();
  }

  @Deprecated
  public static <K, V> ListMultiMap<K, V> newListMultiMap() {
    return new ListMultiMap<>();
  }

  @Deprecated
  public static <K, V> SetMultiMap<K, V> newSetMultiMap() {
    return new SetMultiMap<>();
  }

  @Deprecated
  public static <K, V> Map<K, V> newLinkedHashMap() {
    return new LinkedHashMap<>();
  }

  @Deprecated
  public static <K, V> Map<K, V> newHashMap(Map<K, V> parameters) {
    return new HashMap<>(parameters);
  }
}
