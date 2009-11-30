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
  private static final boolean DEBUG = false;

  private Set<T> m_nodes = Sets.newHashSet();
  private MapList<T, T> m_dependedUpon = new MapList<T, T>();
  private MapList<T, T> m_dependingOn = new MapList<T, T>();
  private Map<T, Status> m_statuses = Maps.newHashMap();

  public static enum Status {
    READY, RUNNING, FINISHED
  };

  /**
   * Add a node to the graph.
   */
  public void addNode(T node) {
    m_nodes.add(node);
    m_statuses.put(node, Status.READY);
  }

  /**
   * Add an edge between two nodes, which don't have to already be in the graph
   * (they will be added by this method).
   */
  public void addEdge(T from, T to) {
    addNode(from);
    addNode(to);
    m_dependingOn.put(to, from);
    m_dependedUpon.put(from, to);
  }

  /**
   * @return a set of all the nodes that don't depend on any other nodes.
   */
  public Set<T> getFreeNodes() {
    Set<T> result = Sets.newHashSet();
    for (T m : m_nodes) {
      // A node is free if...
      // - it's ready to run
      if (m_statuses.get(m) != Status.READY) continue;

      // - no other nodes depend on it
      if (!m_dependedUpon.containsKey(m)) result.add(m);

      // - or all the nodes that it depends on have already run 
      else if (getUnfinishedNodes(m_dependedUpon.get(m)).size() == 0) result.add(m);
    }
    return result;
  }

  /**
   * @return a list of all the nodes that have a status other than FINISHED.
   */
  private Collection<? extends T> getUnfinishedNodes(List<T> nodes) {
    Set<T> result = Sets.newHashSet();
    if (nodes != null) {
      for (T n : nodes) {
        if (m_statuses.get(n) != Status.FINISHED) result.add(n);
      }
    }
    return result;
  }

  /**
   * Remove a node from the graph.
   */
  public void remove(T node) {
    m_nodes.remove(node);
    m_dependingOn.remove(node);
    for (T k : m_dependedUpon.getKeys()) {
      List<T> l = m_dependedUpon.get(k);
      l.remove(node);
      if (l.size() == 0) m_dependedUpon.remove(k);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[DynamicGraph ");
    MapList<Status, T> ml = new MapList<Status, T>();
    for (T o : m_nodes) {
      ml.put(m_statuses.get(o), o);
    }
    for (Status s : ml.getKeys()) {
      result.append("\n  ").append(s).append(":").append(ml.get(s));
    }
    result.append("\n  Edges:" + m_dependingOn);
    result.append("]");
    return result.toString();
  }

  /**
   * Set the status for a set of nodes.
   */
  public void setStatus(Collection<T> nodes, Status status) {
    for (T n : nodes) {
      setStatus(n, status);
    }
  }

  /**
   * Set the status for a node.
   */
  public void setStatus(T node, Status status) {
    m_statuses.put(node, status);
  }

  /**
   * @return the number of nodes in this graph.
   */
  public int getNodeCount() {
    return m_nodes.size();
  }

  public int getNodeCountWithStatus(Status s) {
    int result = 0;
    for (T n : m_nodes) {
      if (m_statuses.get(n) == s) result++;
    }
    return result;
  }

  private void ppp(String string) {
    if (DEBUG) {
      System.out.println("   [GroupThreadPoolExecutor] " + Thread.currentThread().getId() + " "
          + string);
    }
  }

}
