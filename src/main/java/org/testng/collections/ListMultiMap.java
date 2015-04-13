package org.testng.collections;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A container to hold lists indexed by a key.
 */
public class ListMultiMap<K, V> {
  private final Map<K, List<V>> m_objects = Maps.newHashMap();

  public boolean put(K key, V method) {
    boolean setExists = true;
    List<V> l = m_objects.get(key);
    if (l == null) {
      setExists = false;
      l = Lists.newArrayList();
      m_objects.put(key, l);
    }
    return l.add(method) && setExists;
  }

  public List<V> get(K key) {
    return m_objects.get(key);
  }

  @Deprecated
  public List<K> getKeys() {
    return new ArrayList<>(m_objects.keySet());
  }

  public Set<K> keySet() {
    return new HashSet(m_objects.keySet());
  }

  public boolean containsKey(K k) {
    return m_objects.containsKey(k);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    Set<K> indices = keySet();
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

  @Deprecated
  public int getSize() {
    return size();
  }

  public int size() {
    return m_objects.size();
  }

  @Deprecated
  public List<V> remove(K key) {
    return removeAll(key);
  }

  public boolean remove(K key, V value) {
    List<V> values = get(key);
    if (values == null) {
      return false;
    }
    return values.remove(value);
  }

  public List<V> removeAll(K key) {
    return m_objects.remove(key);
  }

  @Deprecated
  public Set<Entry<K, List<V>>> getEntrySet() {
    return entrySet();
  }

  public Set<Entry<K, List<V>>> entrySet() {
    return m_objects.entrySet();
  }

  @Deprecated
  public Collection<List<V>> getValues() {
    return values();
  }

  public Collection<List<V>> values() {
    return m_objects.values();
  }

  public boolean putAll(K k, Collection<? extends V> values) {
    boolean result = false;
    for (V v : values) {
      result = put(k, v) || result;
    }
    return result;
  }

  public static <K, V> ListMultiMap<K, V> create() {
    return new ListMultiMap<>();
  }
}
