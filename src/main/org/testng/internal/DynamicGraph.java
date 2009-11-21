package org.testng.internal;

import org.testng.collections.Maps;
import org.testng.internal.annotations.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of the graph of methods.
 */
public class DynamicGraph<T> {
  private Set<T> m_nodes = Sets.newHashSet();
  private MapList<T, T> m_dependedUpon = new MapList<T, T>();
  private MapList<T, T> m_dependingOn = new MapList<T, T>();
  private Map<T, Status> m_statuses = Maps.newHashMap();

  public static enum Status {
    READY, RUNNING, FINISHED
  };

  public void addNode(T node) {
    m_nodes.add(node);
    m_statuses.put(node, Status.READY);
  }

  public void addEdge(T from, T to) {
    addNode(from);
    addNode(to);
    m_dependingOn.put(to, from);
    m_dependedUpon.put(from, to);
  }

  public Set<T> getFreeNodes() {
    Set<T> result = Sets.newHashSet();
    for (T m : m_nodes) {
      if (m_statuses.get(m) != Status.READY) continue;
      if (!m_dependedUpon.containsKey(m)) result.add(m);
      else if (getUnfinishedNodes(m_dependedUpon.get(m)).size() == 0) result.add(m);
    }
    return result;
  }

  private Collection<? extends T> getUnfinishedNodes(List<T> nodes) {
    Set<T> result = Sets.newHashSet();
    if (nodes != null) {
      for (T n : nodes) {
        if (m_statuses.get(n) != Status.FINISHED) result.add(n);
      }
    }
    return result;
  }

  public void remove(T node) {
//    System.out.println("Removing:" + node + " now:"+ m_dependedUpon);
    m_nodes.remove(node);
    m_dependingOn.remove(node);
    for (T k : m_dependedUpon.getKeys()) {
      List<T> l = m_dependedUpon.get(k);
      boolean f = l.remove(node);
      if (l.size() == 0) m_dependedUpon.remove(k);
    }
//    System.out.println(m_dependedUpon);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[DynamicGraph ");
    for (T o : m_nodes) {
      result.append(o + ":" + m_statuses.get(o)).append("\n");
    }
    result.append("\n Edges:" + m_dependingOn);
    result.append("]");
    return result.toString();
  }

  public void setStatus(Collection<T> nodes, Status status) {
    for (T n : nodes) {
      setStatus(n, status);
    }
  }

  public void setStatus(T node, Status status) {
    m_statuses.put(node, status);
  }
}
