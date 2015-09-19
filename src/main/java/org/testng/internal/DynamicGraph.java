package org.testng.internal;

import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of the graph of methods.
 */
public class DynamicGraph<T> {
  private static final boolean DEBUG = false;

  private List<T> m_nodesReady = Lists.newArrayList();
  private List<T> m_nodesRunning = Lists.newArrayList();
  private List<T> m_nodesFinished = Lists.newArrayList();

  private Comparator<? super T> m_nodeComparator = null;

  private ListMultiMap<T, T> m_dependedUpon = Maps.newListMultiMap();
  private ListMultiMap<T, T> m_dependingOn = Maps.newListMultiMap();

  public static enum Status {
    READY, RUNNING, FINISHED
  }

  /**
   * Define a comparator for the nodes of this graph, which will be used
   * to order the free nodes when they are asked.
   */
  public void setComparator(Comparator<? super T> c) {
    m_nodeComparator = c;
  }

  /**
   * Add a node to the graph.
   */
  public void addNode(T node) {
    m_nodesReady.add(node);
  }

  /**
   * Add an edge between two nodes.
   */
  public void addEdge(T from, T to) {
    m_dependingOn.put(to, from);
    m_dependedUpon.put(from, to);
  }

  /**
   * @return a set of all the nodes that don't depend on any other nodes.
   */
  public List<T> getFreeNodes() {
    List<T> result = Lists.newArrayList();
    for (T m : m_nodesReady) {
      // A node is free if...

      List<T> du = m_dependedUpon.get(m);
      // - no other nodes depend on it
      if (!m_dependedUpon.containsKey(m)) {
        result.add(m);
      } else if (getUnfinishedNodes(du).size() == 0) {
        result.add(m);
      }
    }

    // Sort the free nodes if requested (e.g. priorities)
    if (result != null && ! result.isEmpty()) {
      if (m_nodeComparator != null) {
        Collections.sort(result, m_nodeComparator);
        ppp("Nodes after sorting:" + result.get(0));
      }
    }

    return result;
  }

  /**
   * @return a list of all the nodes that have a status other than FINISHED.
   */
  private Collection<? extends T> getUnfinishedNodes(List<T> nodes) {
    Set<T> result = Sets.newHashSet();
    for (T node : nodes) {
      if (m_nodesReady.contains(node) || m_nodesRunning.contains(node)) {
        result.add(node);
      }
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
    result.append("\n  Edges:\n");
    for (Map.Entry<T, List<T>> es : m_dependingOn.entrySet()) {
      result.append("     " + es.getKey() + "\n");
      for (T t : es.getValue()) {
        result.append("        " + t + "\n");
      }
    }
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
    List<T> freeNodes = getFreeNodes();
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

    for (T k : m_dependingOn.keySet()) {
      List<T> nodes = m_dependingOn.get(k);
      for (T n : nodes) {
        String dotted = m_nodesFinished.contains(k) ? "style=dotted" : "";
        result.append("  " + getName(k) + " -> " + getName(n) + " [dir=back " + dotted + "]\n");
      }
    }
    result.append("}\n");

    return result.toString();
  }

  public ListMultiMap<T, T> getEdges() {
    return m_dependingOn;
  }

}
