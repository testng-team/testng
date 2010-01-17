package org.testng.internal;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A container to hold lists indexed by a key.
 */
public class MapList<K, V> {
  private Map<K, List<V>> m_objects = Maps.newHashMap();

  public void put(K key, V method) {
    List<V> l = m_objects.get(key);
    if (l == null) {
      l = Lists.newArrayList();
      m_objects.put(key, l);
    }
    l.add(method);
  }

  public List<V> get(K key) {
    return m_objects.get(key);
  }

  public List<K> getKeys() {
    return new ArrayList(m_objects.keySet());
//    List<K> result = new ArrayList<K>();
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
    List<K> indices = getKeys();
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

  public List<V> remove(K key) {
    return m_objects.remove(key);
  }
}
