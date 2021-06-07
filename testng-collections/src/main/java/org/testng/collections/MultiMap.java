package org.testng.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MultiMap<K, V, C extends Collection<V>> {
  protected final Map<K, C> m_objects;

  protected MultiMap(boolean isSorted) {
    if (isSorted) {
      m_objects = Maps.newLinkedHashMap();
    } else {
      m_objects = Maps.newHashMap();
    }
  }

  protected abstract C createValue();

  public boolean put(K key, V method) {
    AtomicBoolean exists = new AtomicBoolean(true);
    return m_objects
            .computeIfAbsent(
                key,
                k -> {
                  exists.set(false);
                  return createValue();
                })
            .add(method)
        && exists.get();
  }

  public C get(K key) {
    return m_objects.computeIfAbsent(key, k -> createValue());
  }

  public Set<K> keySet() {
    return new HashSet<>(m_objects.keySet());
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

  public int size() {
    return m_objects.size();
  }

  public boolean remove(K key, V value) {
    return get(key).remove(value);
  }

  public C removeAll(K key) {
    return m_objects.remove(key);
  }

  public Set<Map.Entry<K, C>> entrySet() {
    return m_objects.entrySet();
  }

  public Collection<C> values() {
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
