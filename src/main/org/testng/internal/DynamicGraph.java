package org.testng.internal;

import org.testng.internal.annotations.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Representation of the graph of methods.
 */
public class DynamicGraph<T> {
  private static final boolean DEBUG = false;

  private Set<T> m_nodesReady = Sets.newHashSet();
  private Set<T> m_nodesRunning = Sets.newHashSet();
  private Set<T> m_nodesFinished = Sets.newHashSet();

  private MapList<T, T> m_dependedUpon = new MapList<T, T>();
  private MapList<T, T> m_dependingOn = new MapList<T, T>();

  public static enum Status {
    READY, RUNNING, FINISHED
  }

  /**
   * Add a node to the graph.
   */
  public void addNode(T node) {
    m_nodesReady.add(node);
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
    for (T m : m_nodesReady) {
      // A node is free if...

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
    for (T node : nodes) {
      if (m_nodesReady.contains(node) || m_nodesRunning.contains(node)) result.add(node);
    }
    return result;
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
    removeNode(node);
    switch(status) {
      case READY: m_nodesReady.add(node); break;
      case RUNNING: m_nodesRunning.add(node); break;
      case FINISHED: m_nodesFinished.add(node); break;
      default: throw new IllegalArgumentException();
    }
  }

  private void removeNode(T node) {
    if (!m_nodesReady.remove(node)) {
      if (!m_nodesRunning.remove(node)) {
        m_nodesFinished.remove(node);
      }
    }
  }

  /**
   * @return the number of nodes in this graph.
   */
  public int getNodeCount() {
    int result = m_nodesReady.size() + m_nodesRunning.size() + m_nodesFinished.size();
    return result;
  }

  public int getNodeCountWithStatus(Status status) {
    switch(status) {
      case READY: return m_nodesReady.size();
      case RUNNING: return m_nodesRunning.size();
      case FINISHED: return m_nodesFinished.size();
      default: throw new IllegalArgumentException();
    }
  }

  private static void ppp(String string) {
    if (DEBUG) {
      System.out.println("   [GroupThreadPoolExecutor] " + Thread.currentThread().getId() + " "
          + string);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[DynamicGraph ");
    result.append("\n  Ready:" + m_nodesReady);
    result.append("\n  Running:" + m_nodesRunning);
    result.append("\n  Finished:" + m_nodesFinished);
    result.append("\n  Edges:" + m_dependingOn);
    result.append("]");
    return result.toString();
  }

  private String getName(T t) {
    String s = t.toString();
    int n1 = s.lastIndexOf('.') + 1;
    int n2 = s.indexOf('(');
    return s.substring(n1, n2);
  }

  /**
   * @return a .dot file (GraphViz) version of this graph.
   */
  public String toDot() {
    String FREE = "[style=filled color=yellow]";
    String RUNNING = "[style=filled color=green]";
    String FINISHED = "[style=filled color=grey]";
    StringBuilder result = new StringBuilder("digraph g {\n");
    Set<T> freeNodes = getFreeNodes();
    String color;
    for (T n : m_nodesReady) {
      color = freeNodes.contains(n) ? FREE : "";
      result.append("  " + getName(n) + color + "\n");
    }
    for (T n : m_nodesRunning) {
      color = freeNodes.contains(n) ? FREE : RUNNING; 
      result.append("  " + getName(n) + color + "\n");
    }
    for (T n : m_nodesFinished) {
      result.append("  " + getName(n) + FINISHED+ "\n");
    }
    result.append("\n");

    for (T k : m_dependingOn.getKeys()) {
      List<T> nodes = m_dependingOn.get(k);
      for (T n : nodes) {
        String dotted = m_nodesFinished.contains(k) ? "style=dotted" : "";
        result.append("  " + getName(k) + " -> " + getName(n) + " [dir=back " + dotted + "]\n");
      }
    }
    result.append("}\n");

    return result.toString();
  }
}
