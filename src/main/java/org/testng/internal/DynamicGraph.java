package org.testng.internal;

import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.collections.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation of the graph of methods.
 */
public class DynamicGraph<T> {

  private final List<T> m_nodesReady = Lists.newArrayList();
  private final List<T> m_nodesRunning = Lists.newArrayList();
  private final List<T> m_nodesFinished = Lists.newArrayList();

  private final ListMultiMap<T, Edge<T>> m_edges = Maps.newListMultiMap();

  public enum Status {
    READY, RUNNING, FINISHED
  }

  private static class Edge<T> {
    private final T from;
    private final T to;
    private final int weight;
    private final int order;

    private Edge(int weight, T from, T to) {
      this(weight, 0, from, to);
    }

    private Edge(int weight, int order, T from, T to) {
      this.from = from;
      this.to = to;
      this.weight = weight;
      this.order = order;
    }
  }

  /**
   * Add a node to the graph.
   */
  public void addNode(T node) {
    m_nodesReady.add(node);
  }

  /**
   *
   * @param weight - Represents one of {@link org.testng.TestRunner.PriorityWeight} ordinals indicating
   *               the weightage of a particular node in the graph
   * @param order - Represents an ordering of nodes that have the same weight.
   * @param from - Represents the edge that depends on another edge.
   * @param to - Represents the edge on which another edge depends upon.
   */
  public void addEdge(int weight, int order, T from, T to) {
    addEdges(Collections.singletonList(new Edge<>(weight, order, from, to)));
  }

  /**
   * Add an edge between two nodes.
   */
  public void addEdge(int weight, T from, T... tos) {
    addEdge(weight, from, Arrays.asList(tos));
  }

  /**
   * Add an edge between two nodes.
   */
  public void addEdge(int weight, T from, Iterable<T> tos) {
    List<Edge<T>> edges = Lists.newArrayList();
    for (T to : tos) {
      edges.add(new Edge<>(weight, from, to));
    }
    addEdges(edges);
  }

  private void addEdges(List<Edge<T>> edges) {
    for (Edge<T> edge : edges) {
      Edge<T> existingEdge = getNode(m_edges, edge);
      if (existingEdge != null && existingEdge.weight == edge.weight) {
        throw new IllegalStateException("Circular dependency: " + edge.from + " <-> " + edge.to);
      }
      if (existingEdge == null) {
        m_edges.put(edge.from, edge);
      } else if (existingEdge.weight < edge.weight) {
        m_edges.put(edge.from, edge);
      }
      // else: existingEdge.weight > edge.weight and ignore
    }
  }

  private static <T> Edge<T> getNode(ListMultiMap<T, Edge<T>> edges, Edge<T> edge) {
    for (Edge<T> e : edges.get(edge.to)) {
      if (e.to.equals(edge.from)) {
        return e;
      }
    }
    return null;
  }

  /**
   * @return a set of all the nodes that don't depend on any other nodes.
   */
  public List<T> getFreeNodes() {
    List<T> result = Lists.newArrayList();
    for (T node : m_nodesReady) {
      // A node is free if...
      // - no other nodes depend on it
      if (m_edges.get(node).isEmpty() || getUnfinishedNodes(node).isEmpty()) {
        result.add(node);
      }
    }
    // if all nodes have dependencies, then we can ignore the lowest one
    if (result.isEmpty()) {
      int lowestPriority = getLowestEdgePriority(m_nodesReady);
      int lowestOrder = getLowestOrder(m_nodesReady);
      for (T node : m_nodesReady) {
        if (hasAllEdgesWithLevel(m_edges.get(node), lowestPriority) &&
            hasAllEdgesWithSameOrder(m_edges.get(node), lowestOrder)) {
          result.add(node);
        }
      }
    }
    return  result;
  }

  private int getLowestEdgePriority(List<T> nodes) {
    if (nodes.isEmpty()) {
      return 0;
    }
    Integer lowerPriority = null;
    for (T node : nodes) {
      for (Edge<T> edge : m_edges.get(node)) {
        if (lowerPriority == null) {
          lowerPriority = edge.weight;
        } else {
          lowerPriority = lowerPriority < edge.weight ? lowerPriority : edge.weight;
        }
      }
    }
    return lowerPriority == null ? 0 : lowerPriority;
  }

  private int getLowestOrder(List<T> nodes) {
    if (nodes.isEmpty()) {
      return 0;
    }
    Integer lowestOrder = null;
    for (T node : nodes) {
      for (Edge<T> edge : m_edges.get(node)) {
        if (lowestOrder == null) {
          lowestOrder = edge.order;
        } else {
          lowestOrder = lowestOrder < edge.order ? lowestOrder : edge.order;
        }
      }
    }
    return lowestOrder == null ? 0 : lowestOrder;
  }

  private static <T> boolean hasAllEdgesWithLevel(List<Edge<T>> edges, int level) {
    for (Edge<?> edge : edges) {
      if (edge.weight != level) {
        return false;
      }
    }
    return true;
  }

  private static <T> boolean hasAllEdgesWithSameOrder(List<Edge<T>> edges, int order) {
    for (Edge<?> edge : edges) {
      if (edge.order != order) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return a list of all the nodes that have a status other than FINISHED.
   */
  private Collection<? extends T> getUnfinishedNodes(T node) {
    Set<T> result = Sets.newHashSet();
    for (Edge<T> edge : m_edges.get(node)) {
      if (m_nodesReady.contains(edge.to) || m_nodesRunning.contains(edge.to)) {
        result.add(edge.to);
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
    switch(status) {
      case RUNNING:
        m_nodesReady.remove(node);
        m_nodesRunning.add(node);
        break;
      case FINISHED:
        m_nodesReady.remove(node);
        m_nodesRunning.remove(node);
        m_nodesFinished.add(node);

        m_edges.removeAll(node);
        List<Pair<T, Edge<T>>> edgesToRemove = Lists.newArrayList();
        for (Map.Entry<T, List<Edge<T>>> entry : m_edges.entrySet()) {
          for (Edge<T> edge : entry.getValue()) {
            if (edge.to.equals(node)) {
              edgesToRemove.add(new Pair<>(entry.getKey(), edge));
            }
          }
        }
        for (Pair<T, Edge<T>> pair : edgesToRemove) {
          m_edges.remove(pair.first(), pair.second());
        }
        break;
      default:
        throw new IllegalArgumentException("Unsupported status: " + status);
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

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[DynamicGraph ");
    result.append("\n  Ready:" + m_nodesReady);
    result.append("\n  Running:" + m_nodesRunning);
    result.append("\n  Finished:" + m_nodesFinished);
    result.append("\n  Edges:\n");
    for (Map.Entry<T, List<Edge<T>>> es : m_edges.entrySet()) {
      result.append("     " + es.getKey() + "\n");
      for (Edge<T> t : es.getValue()) {
        result.append("        " + t.to + "\n");
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

    for (List<Edge<T>> edges : m_edges.values()) {
      for (Edge<T> edge : edges) {
        String dotted = m_nodesFinished.contains(edge.from) ? "style=dotted" : "";
        result.append("  " + getName(edge.from) + " -> " + getName(edge.to) + " [dir=back " + dotted + "]\n");
      }
    }
    result.append("}\n");

    return result.toString();
  }

  public ListMultiMap<T, Edge<T>> getEdges() {
    return m_edges;
  }
}
