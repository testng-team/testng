package org.testng.collections;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Maps {

  public static <K, V> Map<K,V> newHashMap() {
    return new HashMap<K, V>();
  }

  public static <K, V> Map<K,V> newHashtable() {
    return new Hashtable<K, V>();
  }
}
