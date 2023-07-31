package org.testng.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class MultiMap<K, V, C extends Collection<V>> {
  protected final @NonNull Map<@NonNull K, @NonNull C> m_objects;

  protected MultiMap(boolean isSorted) {
    if (isSorted) {
      m_objects = Maps.newLinkedHashMap();
    } else {
      m_objects = Maps.newHashMap();
    }
  }

  protected abstract C createValue();

  public boolean put(@NonNull K key, @NonNull V method) {
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

  public C get(@NonNull K key) {
    return m_objects.computeIfAbsent(key, k -> createValue());
  }

  public @NonNull Set<K> keySet() {
    return new HashSet<>(m_objects.keySet());
  }

  public boolean containsKey(@NonNull K k) {
    return m_objects.containsKey(k);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (Map.Entry<K, C> entry : m_objects.entrySet()) {
      result.append("\n    ").append(entry.getKey()).append(" <-- ");
      for (V v : entry.getValue()) {
        result.append(v).append(" ");
      }
    }
    return result.toString();
  }

  public boolean isEmpty() {
    return m_objects.isEmpty();
  }

  public int size() {
    return m_objects.size();
  }

  public boolean remove(@NonNull K key, @NonNull V value) {
    return get(key).remove(value);
  }

  public @Nullable C removeAll(@NonNull K key) {
    return m_objects.remove(key);
  }

  public Set<Map.Entry<@NonNull @KeyFor("this.m_objects") K, @NonNull C>> entrySet() {
    return m_objects.entrySet();
  }

  public Collection<C> values() {
    return m_objects.values();
  }

  public boolean putAll(@NonNull K k, @NonNull Collection<? extends @NonNull V> values) {
    boolean result = false;
    for (@NonNull V v : values) {
      result = put(k, v) || result;
    }
    return result;
  }
}
