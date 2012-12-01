package org.testng.collections;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A container to hold lists indexed by a key.
 */
public class ListMultiMap<K, V> {
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

  public Set<Entry<K, List<V>>> getEntrySet() {
    return m_objects.entrySet();
  }

  public Collection<List<V>> getValues() {
    return m_objects.values();
  }

  public void putAll(K k, Collection<V> values) {
    for (V v : values) {
      put(k, v);
    }
  }

  public static <K, V> ListMultiMap<K, V> create() {
    return new ListMultiMap<K, V>();
  }
}
