package org.testng.collections;


import java.util.List;

/**
 * A container to hold lists indexed by a key.
 */
public class ListMultiMap<K, V> extends MultiMap<K, V, List<V>> {

  @Override
  protected List<V> createValue() {
    return Lists.newArrayList();
  }

  @Deprecated
  public static <K, V> ListMultiMap<K, V> create() {
    return Maps.newListMultiMap();
  }
}
