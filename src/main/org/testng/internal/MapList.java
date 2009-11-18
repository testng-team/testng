package org.testng.internal;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A container to hold lists indexed by an integer.
 */
public class MapList<T> implements Iterable<List<T>> {
  private Map<Integer, List<T>> m_objects = Maps.newHashMap();

  public void addObjectAtIndex(T method, int index) {
    List<T> l = m_objects.get(index);
    if (l == null) {
      l = Lists.newArrayList();
      m_objects.put(index, l);
    }
    l.add(method);
  }

  public List<T> get(int index) {
    return m_objects.get(index);
  }

  public Integer[] getIndices() {
    Integer[] result = m_objects.keySet().toArray(new Integer[m_objects.size()]);
    Arrays.sort(result);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    Integer[] indices = m_objects.keySet().toArray(new Integer[m_objects.size()]);
    Arrays.sort(indices);
    for (Integer i : indices) {
      result.append("\n  ").append(i).append(" ");
      for (Object o : m_objects.get(i)) {
        result.append(o).append(" ");
      }
    }
    return result.toString();
  }

  public boolean isEmpty() {
    return m_objects.size() == 0;
  }

  public Iterator<List<T>> iterator() {
    return null;
  }

  public int getSize() {
    return m_objects.size();
  }
}
