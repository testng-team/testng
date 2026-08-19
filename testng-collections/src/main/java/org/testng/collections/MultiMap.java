package org.testng.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;

public abstract class MultiMap<K, V, C extends Collection<V>> {
  protected final Map<K, C> m_objects;

  protected MultiMap(boolean isSorted) {
    if (isSorted) {
      m_objects = new LinkedHashMap<>();
    } else {
      m_objects = new HashMap<>();
    }
  }

  protected abstract C createValue();

  public boolean put(@Nullable K key, V method) {
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

  public C get(@Nullable K key) {
    return m_objects.computeIfAbsent(key, k -> createValue());
  }

  public Set<K> keySet() {
    return new HashSet<>(m_objects.keySet());
  }

  public boolean containsKey(@Nullable K k) {
    return m_objects.containsKey(k);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    Set<K> indices = keySet();
    for (K i : indices) {
      result.append("\n    ").append(i).append(" <-- ");
      for (Object o : get(i)) {
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

  public boolean remove(@Nullable K key, V value) {
    return get(key).remove(value);
  }

  /**
   * Drops a key and every value held for it.
   *
   * @param key the key to drop.
   * @return the values that were held, or {@code null} when the key was not present.
   */
  public @Nullable C removeAll(@Nullable K key) {
    return m_objects.remove(key);
  }

  public Set<Map.Entry<K, C>> entrySet() {
    return m_objects.entrySet();
  }

  public Collection<C> values() {
    return m_objects.values();
  }

  public boolean putAll(@Nullable K k, Collection<? extends V> values) {
    boolean result = false;
    for (V v : values) {
      result = put(k, v) || result;
    }
    return result;
  }
}
