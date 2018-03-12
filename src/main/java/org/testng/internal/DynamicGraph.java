package org.testng.internal;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.union;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.collections.Lists;
import org.testng.collections.Sets;

/**
 * Representation of the graph of methods.
 */
public class DynamicGraph<T> {

  private final Set<T> m_nodesReady = Sets.newLinkedHashSet();
  private final Set<T> m_nodesRunning = Sets.newLinkedHashSet();
  private final Set<T> m_nodesFinished = Sets.newLinkedHashSet();

  // Maps of edges in the graph organized by incoming or outgoing edges. The integer is the weight of the edge.
  private final Map<T, Map<T, Integer>> m_incomingEdges = new HashMap<>();
  private final Map<T, Map<T, Integer>> m_outgoingEdges = new HashMap<>();

  public enum Status {
    READY, RUNNING, FINISHED
  }

  public DynamicGraph() {
  }

  /**
   * Add a node to the graph.
   */
  public boolean addNode(T node) {
    return m_nodesReady.add(node);
  }

  /**
   *
   * @param weight - Represents one of {@link org.testng.TestRunner.PriorityWeight} ordinals indicating
   *               the weightage of a particular node in the graph
   * @param from - Represents the edge that depends on another edge.
   * @param to - Represents the edge on which another edge depends upon.
   */
  public void addEdge(int weight, T from, T to) {
    Integer reversedEdgeWeight = findReversedEdge(from, to);
    if (reversedEdgeWeight != null && reversedEdgeWeight == weight) {
      throw new IllegalStateException("Circular dependency: " + from + " <-> " + to);
    }

    addEdgeToMap(m_incomingEdges, to, from, weight);
    addEdgeToMap(m_outgoingEdges, from, to, weight);
  }


  private void addEdgeToMap(Map<T, Map<T, Integer>> map, T n1, T n2, int weight) {
    Map<T, Integer> edges = map.get(n1);
    if (edges == null) {
      edges = new HashMap<>();
      map.put(n1, edges);
    }

    Integer existingWeight = edges.get(n2);
    edges.put(n2, Math.max(weight, existingWeight != null ? existingWeight : Integer.MIN_VALUE));
  }

  /**
   * Add an edge between two nodes.
   */
  @SafeVarargs
  public final void addEdges(int weight, T from, T... tos) {
    for (T to : tos) {
      addEdge(weight, from, to);
    }
  }

  /**
   * Add an edge between two nodes.
   */
  public void addEdges(int weight, T from, Iterable<T> tos) {
    for (T to : tos) {
      addEdge(weight, from, to);
    }
  }

  /**
   * Remove a node from the graph and all associated edges.
   *
   * @param node Node to remove.
   */
  private void removeNode(T node) {
    Map<T, Integer> incomingEdges = m_incomingEdges.get(node);
    Map<T, Integer> outgoingEdges = m_outgoingEdges.get(node);

    m_incomingEdges.remove(node);
    m_outgoingEdges.remove(node);

    if (incomingEdges != null) {
      for (T from : incomingEdges.keySet()) {
        Map<T, Integer> edges = m_outgoingEdges.get(from);
        edges.remove(node);
        if (edges.isEmpty()) {
          m_outgoingEdges.remove(from);
        }
      }
    }

    if (outgoingEdges != null) {
      for (T to : outgoingEdges.keySet()) {
        Map<T, Integer> edges = m_incomingEdges.get(to);
        edges.remove(node);
        if (edges.isEmpty()) {
          m_incomingEdges.remove(to);
        }
      }
    }
  }

  /**
   * Return the weight of the edge in the graph that is the reversed direction of edge. For example, if
   * edge a -> b exists, and edge b -> a is passed in, then return a -> b.
   *
   * @param from
   * @param to
   * @return
   */
  private Integer findReversedEdge(T from, T to) {
    Map<T, Integer> edges = m_outgoingEdges.get(to);
    return edges == null ? null : edges.get(from);
  }

  /**
   * @return a set of all the nodes that don't depend on any other nodes.
   */
  public List<T> getFreeNodes() {
    // Get a list of nodes that are ready and have no outgoing edges.
    List<T> result = Lists.newArrayList();
    result.addAll(difference(m_nodesReady, m_outgoingEdges.keySet()));

    // if all nodes have dependencies, then we can ignore the lowest one if nothing else is running
    if (result.isEmpty() && m_nodesRunning.isEmpty()) {
      int lowestPriority = getLowestEdgeWeight(m_nodesReady);
      for (T node : m_nodesReady) {
        if (hasAllEdgesWithWeight(m_outgoingEdges, node, lowestPriority)) {
          result.add(node);
        }
      }
    }

    // Filter result: remove node if the result contains all nodes from an edge
    List<T> finalResult = Lists.newArrayList();
    for (T node : result) {
      Map<T, Integer> edges = m_outgoingEdges.get(node);
      // disjoint returns true if the two collections have no common items.
      if (edges == null || Collections.disjoint(edges.keySet(), result)) {
        finalResult.add(node);
      }
    }

    return finalResult;
  }

  private int getLowestEdgeWeight(Set<T> nodes) {
    if (nodes.isEmpty()) {
      return 0;
    }

    Set<T> union = union(nodes, m_outgoingEdges.keySet());
    if (union.isEmpty()) {
      return 0;
    }

    int lowestWeight = Integer.MAX_VALUE;
    for (T node : union) {
      Map<T, Integer> weightMap = m_outgoingEdges.get(node);

      // Not catching NoSuchElementException, because that would indicate our graph is corrupt.
      lowestWeight = Math.min(lowestWeight, Collections.min(weightMap.values()));
    }
    return lowestWeight;
  }

  private static <T> boolean hasAllEdgesWithWeight(Map<T, Map<T, Integer>> map, T node, int level) {
    Map<T, Integer> weights = map.get(node);
    if (weights == null) {
      return true;
    }

    for (int weight : weights.values()) {
      if (weight != level) {
        return false;
      }
    }
    return true;
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

        Map<T, Integer> outgoingEdges = m_outgoingEdges.get(node);
        Map<T, Integer> incomingEdges = m_incomingEdges.get(node);
        if (outgoingEdges != null && incomingEdges != null) {
          // Add virtual edge before removing intermediate node. E.g.:
          //   Given graph c -> b -> a, then add c -> a before removing b.
          for (Map.Entry<T, Integer> out : outgoingEdges.entrySet()) {
            for (Map.Entry<T, Integer> in : incomingEdges.entrySet()) {
              if (in.getKey() == out.getKey()) {
                // Don't create a one node cycle.
                continue;
              } else if (in.getValue() > out.getValue()) {
                // Don't add edges if we're patching up a lower weighted cycle.
                continue;
              }

              int weight = Math.max(in.getValue(), out.getValue());

              // Check for cycles to avoid circle IllegalStateException
              Integer reversedEdgeWeight = findReversedEdge(in.getKey(), out.getKey());
              if (reversedEdgeWeight == null || reversedEdgeWeight != weight) {
                addEdge(weight, in.getKey(), out.getKey());
              }
            }
          }

        }

        removeNode(node);
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
    result.append("\n  Ready:").append(m_nodesReady);
    result.append("\n  Running:").append(m_nodesRunning);
    result.append("\n  Finished:").append(m_nodesFinished);
    result.append("\n  Edges:\n");
    for (Map.Entry<T, Map<T, Integer>> es : m_outgoingEdges.entrySet()) {
      result.append("     ").append(es.getKey()).append("\n");
      for (Map.Entry<T, Integer> to : es.getValue().entrySet()) {
        result.append("        (").append(to.getValue()).append(") ").append(to.getKey()).append("\n");
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
      result.append("  ").append(getName(n)).append(color).append("\n");
    }
    for (T n : m_nodesRunning) {
      color = freeNodes.contains(n) ? FREE : RUNNING;
      result.append("  ").append(getName(n)).append(color).append("\n");
    }
    for (T n : m_nodesFinished) {
      result.append("  ").append(getName(n)).append(FINISHED).append("\n");
    }
    result.append("\n");

    for (Map.Entry<T, Map<T, Integer>> es : m_outgoingEdges.entrySet()) {
      T from = es.getKey();
      for (T to : es.getValue().keySet()) {
        String dotted = m_nodesFinished.contains(from) ? "style=dotted" : "";
        result.append("  ").append(getName(from)).append(" -> ").append(getName(to)).append(" [dir=back ").append(dotted).append("]\n");
      }
    }
    result.append("}\n");

    return result.toString();
  }

  /**
   * For tests only
   */
  Map<T, Map<T, Integer>> getEdges() {
    return m_outgoingEdges;
  }
}
