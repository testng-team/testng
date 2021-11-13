package org.testng.collections;

import java.util.Set;

/** A container to hold sets indexed by a key. */
public class SetMultiMap<K, V> extends MultiMap<K, V, Set<V>> {

  public SetMultiMap(boolean isSorted) {
    super(isSorted);
  }

  @Override
  protected Set<V> createValue() {
    return Sets.newHashSet();
  }
}
