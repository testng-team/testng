package org.testng.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MultiMap<K, V, C extends Collection<V>> {
  protected final Map<K, C> objects;

  protected MultiMap(boolean isSorted) {
    if (isSorted) {
      objects = Maps.newLinkedHashMap();
    } else {
      objects = Maps.newHashMap();
    }
  }

  protected abstract C createValue();

  public boolean put(K key, V method) {
    boolean setExists = true;
    C l = objects.get(key);
    if (l == null) {
      setExists = false;
      l = createValue();
      objects.put(key, l);
    }
    return l.add(method) && setExists;
  }

  public C get(K key) {
    C list = objects.get(key);
    if (list == null) {
      list = createValue();
      objects.put(key, list);
    }
    return list;
  }

  @Deprecated
  public List<K> getKeys() {
    return new ArrayList<>(keySet());
  }

  public Set<K> keySet() {
    return new HashSet(objects.keySet());
  }

  public boolean containsKey(K k) {
    return objects.containsKey(k);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    Set<K> indices = keySet();
    for (K i : indices) {
      result.append("\n    ").append(i).append(" <-- ");
      for (Object o : objects.get(i)) {
        result.append(o).append(" ");
      }
    }
    return result.toString();
  }

  public boolean isEmpty() {
    return objects.size() == 0;
  }

  @Deprecated
  public int getSize() {
    return size();
  }

  public int size() {
    return objects.size();
  }

  @Deprecated
  public C remove(K key) {
    return removeAll(key);
  }

  public boolean remove(K key, V value) {
    return get(key).remove(value);
  }

  public C removeAll(K key) {
    return objects.remove(key);
  }

  @Deprecated
  public Set<Map.Entry<K, C>> getEntrySet() {
    return entrySet();
  }

  public Set<Map.Entry<K, C>> entrySet() {
    return objects.entrySet();
  }

  @Deprecated
  public Collection<C> getValues() {
    return values();
  }

  public Collection<C> values() {
    return objects.values();
  }

  public boolean putAll(K k, Collection<? extends V> values) {
    boolean result = false;
    for (V v : values) {
      result = put(k, v) || result;
    }
    return result;
  }
}
