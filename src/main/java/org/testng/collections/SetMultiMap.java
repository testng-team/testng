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
  private final Map<K, Set<V>> m_objects = Maps.newHashMap();

  public boolean put(K key, V method) {
    boolean setExists = true;
    Set<V> l = m_objects.get(key);
    if (l == null) {
      setExists = false;
      l = Sets.newHashSet();
      m_objects.put(key, l);
    }
    return l.add(method) && setExists;
  }

  public Set<V> get(K key) {
    return m_objects.get(key);
  }

  @Deprecated
  public Set<K> getKeys() {
    return keySet();
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
  public Set<V> remove(K key) {
    return removeAll(key);
  }

  public boolean remove(K key, V value) {
    Set<V> values = get(key);
    if (values == null) {
      return false;
    }
    return values.remove(value);
  }

  public Set<V> removeAll(K key) {
    return m_objects.remove(key);
  }

  @Deprecated
  public Set<Entry<K, Set<V>>> getEntrySet() {
    return entrySet();
  }

  public Set<Entry<K, Set<V>>> entrySet() {
    return m_objects.entrySet();
  }

  @Deprecated
  public Collection<Set<V>> getValues() {
    return values();
  }

  public Collection<Set<V>> values() {
    return m_objects.values();
  }

  public boolean putAll(K k, Collection<? extends V> values) {
    boolean result = false;
    for (V v : values) {
      result = put(k, v) || result;
    }
    return result;
  }
}
