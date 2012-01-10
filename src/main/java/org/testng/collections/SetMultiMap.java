package org.testng.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A container to hold sets indexed by a key.
 */
public class SetMultiMap<K, V> {
  private Map<K, Set<V>> m_objects = Maps.newHashMap();

  public void put(K key, V method) {
    Set<V> l = m_objects.get(key);
    if (l == null) {
      l = Sets.newHashSet();
      m_objects.put(key, l);
    }
    l.add(method);
  }

  public Set<V> get(K key) {
    return m_objects.get(key);
  }

  public Set<K> getKeys() {
    return new HashSet(m_objects.keySet());
//    Set<K> result = new ArraySet<K>();
//    for (K k : m_objects.keySet()) {
//      result.add(k);
//    }
//    Collections.sort(result);
//    return result;
  }

  public boolean containsKey(K k) {
    return m_objects.containsKey(k);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    Set<K> indices = getKeys();
//    Collections.sort(indices);
    for (K i : indices) {
      result.append("\n    ").append(i).append(" <-- ");
      for (Object o : m_objects.get(i)) {
        result.append(o).append(" ");
      }
    }
    return result.toString();
  }

  public boolean isEmpty() {
    return m_objects.size() == 0;
  }

  public int getSize() {
    return m_objects.size();
  }

  public Set<V> remove(K key) {
    return m_objects.remove(key);
  }

  public Set<Entry<K, Set<V>>> getEntrySet() {
    return m_objects.entrySet();
  }

  public Collection<Set<V>> getValues() {
    return m_objects.values();
  }

  public void putAll(K k, Collection<V> values) {
    for (V v : values) {
      put(k, v);
    }
  }
}
