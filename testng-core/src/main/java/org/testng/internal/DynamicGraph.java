package org.testng.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.IDynamicGraph;
import org.testng.IExecutionVisualiser;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;

/** Representation of the graph of methods. */
public class DynamicGraph<T> implements IDynamicGraph<T> {

  private final Set<T> m_nodesReady = Sets.newLinkedHashSet();
  private final Set<T> m_nodesRunning = Sets.newLinkedHashSet();
  private final Set<T> m_nodesFinished = Sets.newLinkedHashSet();
  private final Edges<T> m_edges = new Edges<>();
  private Set<IExecutionVisualiser> visualisers = Sets.newHashSet();

  /** Add a node to the graph. */
  public boolean addNode(T node) {
    return m_nodesReady.add(node);
  }

  /**
   * @param weight - Represents one of {@link org.testng.TestRunner.PriorityWeight} ordinals
   *     indicating the weightage of a particular node in the graph
   * @param from - Represents the edge that depends on another edge.
   * @param to - Represents the edge on which another edge depends upon.
   */
  public void addEdge(int weight, T from, T to) {
    m_edges.addEdge(weight, from, to, false);
  }

  public void setVisualisers(Set<IExecutionVisualiser> listener) {
    visualisers = listener;
  }

  /** Add an edge between two nodes. */
  public void addEdges(int weight, T from, Iterable<T> tos) {
    for (T to : tos) {
      addEdge(weight, from, to);
    }
  }

  /** @return a set of all the nodes that don't depend on any other nodes. */
  public List<T> getFreeNodes() {
    // Get a list of nodes that are ready and have no outgoing edges.
    Set<T> free = Sets.newLinkedHashSet(m_nodesReady);
    free.removeAll(m_edges.fromNodes());

    // if all nodes have dependencies, then we can ignore the lowest one if nothing else is running
    if (free.isEmpty() && m_nodesRunning.isEmpty()) {
      int lowestWeight = m_edges.getLowestEdgeWeight(m_nodesReady);
      for (T node : m_nodesReady) {
        if (m_edges.hasAllEdgesWithWeight(node, lowestWeight)) {
          free.add(node);
        }
      }
    }

    // Filter result: remove node if the result contains all nodes from an edge
    List<T> finalResult = Lists.newArrayList();
    for (T node : free) {
      Map<T, Integer> edges = m_edges.from(node);
      // disjoint returns true if the two collections have no common items.
      if (edges == null || Collections.disjoint(edges.keySet(), free)) {
        finalResult.add(node);
      }
    }

    return finalResult;
  }

  public List<T> getDependenciesFor(T node) {
    Map<T, Integer> data = m_edges.to(node);
    if (data == null) {
      return Lists.newArrayList();
    }
    return Lists.newArrayList(data.keySet());
  }

  /** Set the status for a set of nodes. */
  public void setStatus(Collection<T> nodes, Status status) {
    for (T n : nodes) {
      setStatus(n, status);
    }
  }

  /** Set the status for a node. */
  public void setStatus(T node, Status status) {
    switch (status) {
      case RUNNING:
        m_nodesReady.remove(node);
        m_nodesRunning.add(node);
        break;
      case FINISHED:
        m_nodesReady.remove(node);
        m_nodesRunning.remove(node);
        m_nodesFinished.add(node);

        Map<T, Integer> outgoingEdges = m_edges.from(node);
        Map<T, Integer> incomingEdges = m_edges.to(node);
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
              m_edges.addEdge(weight, in.getKey(), out.getKey(), true);
            }
          }
        }

        m_edges.removeNode(node);
        break;
      case READY:
        m_nodesReady.add(node);
        m_nodesRunning.remove(node);
        break;
      default:
        throw new IllegalArgumentException("Unsupported status: " + status);
    }

    this.visualisers.forEach(visualiser -> visualiser.consumeDotDefinition(toDot()));
  }

  /** @return the number of nodes in this graph. */
  public int getNodeCount() {
    return m_nodesReady.size() + m_nodesRunning.size() + m_nodesFinished.size();
  }

  public int getNodeCountWithStatus(Status status) {
    return getNodesWithStatus(status).size();
  }

  public Set<T> getNodesWithStatus(Status status) {
    switch (status) {
      case READY:
        return m_nodesReady;
      case RUNNING:
        return m_nodesRunning;
      case FINISHED:
        return m_nodesFinished;
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public String toString() {
    return "[DynamicGraph "
        + "\n  Ready:"
        + m_nodesReady
        + "\n  Running:"
        + m_nodesRunning
        + "\n  Finished:"
        + m_nodesFinished
        + "\n  Edges:\n"
        + m_edges
        + "]";
  }

  private static <T> String dotShortName(T t) {
    String s = t.toString();
    int n2 = s.indexOf('(');
    return s.substring(0, n2).replaceAll("\\Q.\\E", "_");
  }

  /** @return a .dot file (GraphViz) version of this graph. */
  public String toDot() {
    String FREE = "[style=filled color=yellow]";
    String RUNNING = "[style=filled color=green]";
    String FINISHED = "[style=filled color=grey]";
    StringBuilder result = new StringBuilder("digraph g {\n");
    List<T> freeNodes = getFreeNodes();
    String color;
    for (T n : m_nodesReady) {
      color = freeNodes.contains(n) ? FREE : "";
      result.append("  ").append(dotShortName(n)).append(color).append("\n");
    }
    for (T n : m_nodesRunning) {
      color = freeNodes.contains(n) ? FREE : RUNNING;
      result.append("  ").append(dotShortName(n)).append(color).append("\n");
    }
    for (T n : m_nodesFinished) {
      result.append("  ").append(dotShortName(n)).append(FINISHED).append("\n");
    }

    m_edges.appendDotEdges(result, m_nodesFinished);
    result.append("}\n");

    return result.toString();
  }

  /** For tests only */
  Map<T, Map<T, Integer>> getEdges() {
    return m_edges.getEdges();
  }

  /** Manage edges and weights between nodes. */
  private static class Edges<T> {
    // Maps of edges in the graph organized by incoming or outgoing edges. The integer is the weight
    // of the edge. It's
    // important that these maps always stay consistent with each other. All modifications should go
    // through addEdge
    // and removeNode.
    private final Map<T, Map<T, Integer>> m_incomingEdges = new ConcurrentHashMap<>();
    private final Map<T, Map<T, Integer>> m_outgoingEdges = new ConcurrentHashMap<>();

    public void addEdge(int weight, T from, T to, boolean ignoreCycles) {
      if (from.equals(to)) {
        return;
      }

      Integer reversedEdgeWeight = findReversedEdge(from, to);
      if (reversedEdgeWeight != null && reversedEdgeWeight == weight) {
        if (!ignoreCycles) {
          throw new IllegalStateException("Circular dependency: " + from + " <-> " + to);
        } else {
          return;
        }
      }

      addEdgeToMap(m_incomingEdges, to, from, weight);
      addEdgeToMap(m_outgoingEdges, from, to, weight);
    }

    /** @return the set of nodes that have outgoing edges. */
    Set<T> fromNodes() {
      return m_outgoingEdges.keySet();
    }

    Map<T, Integer> from(T node) {
      Map<T, Integer> edges = m_outgoingEdges.get(node);
      return edges == null ? null : Collections.unmodifiableMap(edges);
    }

    Map<T, Integer> to(T node) {
      Map<T, Integer> edges = m_incomingEdges.get(node);
      return edges == null ? null : Collections.unmodifiableMap(edges);
    }

    /**
     * Return the weight of the edge in the graph that is the reversed direction of edge. For
     * example, if edge a -> b exists, and edge b -> a is passed in, then return a -> b.
     *
     * @param from - the from edge
     * @param to - the to edge
     * @return the weight of the reversed edge or null if edge does not exist
     */
    private Integer findReversedEdge(T from, T to) {
      Map<T, Integer> edges = m_outgoingEdges.get(to);
      return edges == null ? null : edges.get(from);
    }

    /**
     * Remove a node from the graph and all associated edges. Each edge needs to be removed from
     * both maps to keep the maps in sync.
     *
     * @param node Node to remove.
     */
    void removeNode(T node) {
      Map<T, Integer> outgoingEdges = m_outgoingEdges.remove(node);
      if (outgoingEdges != null) {
        removeEdgesFromMap(m_incomingEdges, outgoingEdges.keySet(), node);
      }

      Map<T, Integer> incomingEdges = m_incomingEdges.remove(node);
      if (incomingEdges != null) {
        removeEdgesFromMap(m_outgoingEdges, incomingEdges.keySet(), node);
      }
    }

    int getLowestEdgeWeight(Set<T> nodes) {
      if (nodes.isEmpty()) {
        return 0;
      }

      Set<T> intersection = Sets.newHashSet(nodes);
      intersection.retainAll(m_outgoingEdges.keySet());
      if (intersection.isEmpty()) {
        return 0;
      }

      int lowestWeight = Integer.MAX_VALUE;
      for (T node : intersection) {
        Map<T, Integer> weightMap = m_outgoingEdges.get(node);

        // Not catching NoSuchElementException, because that would indicate our graph is corrupt.
        lowestWeight = Math.min(lowestWeight, Collections.min(weightMap.values()));
      }
      return lowestWeight;
    }

    boolean hasAllEdgesWithWeight(T node, int level) {
      Map<T, Integer> weights = m_outgoingEdges.get(node);
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
     * Remove edges from a map given a node and a list of destination nodes. Given:
     *
     * <pre>{@code
     * m_outgoingEdges:
     *    a -> b
     *         c
     *         d
     * m_incomingEdges:
     *    b -> a
     *    c -> a
     *    d -> a
     *
     * }</pre>
     *
     * Then, calling this method to remove node c on both maps as done in removeNode(), would result
     * in a -> c and c -> a edges being removed.
     */
    private static <T> void removeEdgesFromMap(
        Map<T, Map<T, Integer>> map, Collection<T> nodes, T node) {
      for (T k : nodes) {
        Map<T, Integer> edges = map.get(k);

        if (edges == null) {
          throw new IllegalStateException("Edge not found in map.");
        }

        edges.remove(node);
        if (edges.isEmpty()) {
          map.remove(k);
        }
      }
    }

    private static <T> void addEdgeToMap(Map<T, Map<T, Integer>> map, T n1, T n2, int weight) {
      Map<T, Integer> edges = map.computeIfAbsent(n1, k -> new ConcurrentHashMap<>());

      Integer existingWeight = edges.get(n2);
      edges.put(n2, Math.max(weight, existingWeight != null ? existingWeight : Integer.MIN_VALUE));
    }

    /**
     * Allow raw access to the edges, but protect inside unmodifiableMaps. This is for tests,
     * toString and toDot.
     */
    Map<T, Map<T, Integer>> getEdges() {
      Map<T, Map<T, Integer>> edges = Maps.newHashMap();
      for (Map.Entry<T, Map<T, Integer>> es : m_outgoingEdges.entrySet()) {
        edges.put(es.getKey(), Collections.unmodifiableMap(es.getValue()));
      }
      return Collections.unmodifiableMap(edges);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<T, Map<T, Integer>> es : m_outgoingEdges.entrySet()) {
        sb.append("     ").append(es.getKey()).append("\n");
        for (Map.Entry<T, Integer> to : es.getValue().entrySet()) {
          sb.append("        (")
              .append(to.getValue())
              .append(") ")
              .append(to.getKey())
              .append("\n");
        }
      }
      return sb.toString();
    }

    void appendDotEdges(StringBuilder sb, Set<T> finished) {
      for (Map.Entry<T, Map<T, Integer>> es : m_outgoingEdges.entrySet()) {
        T from = es.getKey();
        for (T to : es.getValue().keySet()) {
          String dotted = finished.contains(from) ? "style=dotted" : "";
          sb.append("  ")
              .append(dotShortName(from))
              .append(" -> ")
              .append(dotShortName(to))
              .append(" [dir=back ")
              .append(dotted)
              .append("]\n");
        }
      }
    }
  }
}
