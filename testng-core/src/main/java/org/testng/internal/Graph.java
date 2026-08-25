package org.testng.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.TestNGException;
import org.testng.log4testng.Logger;

/**
 * Simple graph class to implement topological sort (used to sort methods based on what groups they
 * depend on).
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class Graph<T> {
  private final Map<T, Node<T>> m_nodes = new LinkedHashMap<>();
  private @Nullable List<T> m_strictlySortedNodes = null;
  private final Comparator<Node<T>> comparator;

  //  A map of nodes that are not the predecessors of any node
  // (not needed for the algorithm but convenient to calculate
  // the parallel/sequential lists in TestNG).
  private @Nullable Map<T, Node<T>> m_independentNodes = null;

  public Graph(Comparator<Node<T>> comparator) {
    this.comparator = comparator;
  }

  public void addNode(T tm) {
    log(() -> "ADDING NODE " + tm + " " + tm.hashCode());
    m_nodes.put(tm, new Node<>(tm));
    // Initially, all the nodes are put in the independent list as well
  }

  public Set<T> getPredecessors(T node) {
    Node<T> n = findNode(node);
    if (null == n) {
      throw new TestNGException("Non-existing node: " + node);
    }
    return n.getPredecessors().keySet();
  }

  public boolean isIndependent(T object) {
    return initializeIndependentNodes().containsKey(object);
  }

  private @Nullable Node<T> findNode(T object) {
    return m_nodes.get(object);
  }

  public void addPredecessor(T tm, T predecessor) {
    Node<T> node = findNode(tm);
    if (null == node) {
      throw new TestNGException("Non-existing node: " + tm);
    } else {
      node.addPredecessor(predecessor);
      // Remove these two nodes from the independent list
      Map<T, Node<T>> independentNodes = initializeIndependentNodes();
      independentNodes.remove(predecessor);
      independentNodes.remove(tm);
      log(() -> "  REMOVED " + predecessor + " FROM INDEPENDENT OBJECTS");
    }
  }

  private Collection<Node<T>> getNodes() {
    return m_nodes.values();
  }

  /** @return All the nodes that don't have any order with each other. */
  public Set<T> getIndependentNodes() {
    return initializeIndependentNodes().keySet();
  }

  /** @return All the nodes that have an order with each other, sorted in one of the valid sorts. */
  public @Nullable List<T> getStrictlySortedNodes() {
    return m_strictlySortedNodes;
  }

  public void topologicalSort() {
    log("================ SORTING");
    List<T> sorted = new ArrayList<>();
    m_strictlySortedNodes = sorted;
    initializeIndependentNodes();

    //
    // Clone the list of nodes but only keep those that are
    // not independent.
    //
    List<Node<T>> nodes2 =
        getNodes()
            .parallelStream()
            .filter(n -> !isIndependent(n.getObject()))
            .map(Node::clone)
            .sorted(comparator)
            .collect(Collectors.toList());

    //
    // Sort the nodes alphabetically to make sure that methods of the same class
    // get run close to each other as much as possible
    //

    //
    // Sort
    //
    while (!nodes2.isEmpty()) {

      //
      // Find all the nodes that don't have any predecessors, add
      // them to the result and mark them for removal
      //
      Node<T> node = findNodeWithNoPredecessors(nodes2);
      if (null == node) {
        // We found a cycle. Time to use the Tarjan's algorithm to get the cycle.
        List<T> cycle = new Tarjan<>(this, nodes2.get(0).getObject()).getCycle();
        StringBuilder sb = new StringBuilder();
        sb.append("The following methods have cyclic dependencies:\n");
        for (T m : cycle) {
          sb.append(m).append("\n");
        }
        throw new TestNGException(sb.toString());
      } else {
        sorted.add(node.getObject());
        removeFromNodes(nodes2, node);
      }
    }

    log("=============== DONE SORTING");
    dumpSortedNodes(sorted);
  }

  private Map<T, Node<T>> initializeIndependentNodes() {
    Map<T, Node<T>> independentNodes = m_independentNodes;
    if (null == independentNodes) {
      independentNodes =
          new ArrayList<>(m_nodes.values())
              .stream()
                  .sorted(comparator)
                  .collect(
                      Collectors.toMap(
                          Node::getObject, Function.identity(), (a, b) -> a, LinkedHashMap::new));
      m_independentNodes = independentNodes;
    }
    return independentNodes;
  }

  private void dumpSortedNodes(List<T> sorted) {
    log("====== SORTED NODES");
    for (T n : sorted) {
      log("              " + n);
    }
    log("====== END SORTED NODES");
  }

  /**
   * Remove a node from a list of nodes and update the list of predecessors for all the remaining
   * nodes.
   */
  private void removeFromNodes(List<Node<T>> nodes, Node<T> node) {
    nodes.remove(node);
    nodes.parallelStream().forEach(it -> it.removePredecessor(node.getObject()));
  }

  private static void log(String s) {
    log(() -> s);
  }

  private static void log(Supplier<String> s) {
    Logger.getLogger(Graph.class).trace("[Graph] " + s.get());
  }

  private @Nullable Node<T> findNodeWithNoPredecessors(List<Node<T>> nodes) {
    return nodes.parallelStream().filter(it -> !it.hasPredecessors()).findFirst().orElse(null);
  }

  /**
   * @param o - The predecessor
   * @return A list of all the predecessors for o
   */
  public List<T> findPredecessors(T o) {
    // Locate the node
    Node<T> node = findNode(o);
    if (null == node) {
      // This can happen if an interceptor returned new methods
      return new ArrayList<>();
    }

    // If we found the node, use breadth first search to find all
    // all of the predecessors of o.  "result" is the growing list
    // of all predecessors.  "visited" is the set of items we've
    // already encountered.  "queue" is the queue of items whose
    // predecessors we haven't yet explored.

    Deque<T> result = new ArrayDeque<>();
    Set<T> visited = new HashSet<>();
    Deque<T> queue = new ArrayDeque<>();
    visited.add(o);
    queue.addLast(o);

    while (!queue.isEmpty()) {
      for (T obj : getPredecessors(queue.removeFirst())) {
        if (!visited.contains(obj)) {
          visited.add(obj);
          queue.addLast(obj);
          result.addFirst(obj);
        }
      }
    }

    return new ArrayList<>(result);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[Graph ");
    for (T node : m_nodes.keySet()) {
      result.append(findNode(node)).append(" ");
    }
    result.append("]");

    return result.toString();
  }

  /////
  // class Node
  //
  public static class Node<T> {
    private final T m_object;
    private final Map<T, T> m_predecessors = new HashMap<>();

    public Node(T tm) {
      m_object = tm;
    }

    @Override
    public Node<T> clone() {
      Node<T> result = new Node<>(m_object);
      for (T pred : m_predecessors.values()) {
        result.addPredecessor(pred);
      }
      return result;
    }

    public T getObject() {
      return m_object;
    }

    public Map<T, T> getPredecessors() {
      return m_predecessors;
    }

    /**
     * @param o The predecessor to remove
     * @return true if this predecessor was found and removed
     */
    public boolean removePredecessor(T o) {
      boolean result = false;

      T pred = m_predecessors.get(o);
      if (null != pred) {
        result = null != m_predecessors.remove(o);
        if (result) {
          log(() -> "  REMOVED PRED " + o + " FROM NODE " + m_object);
        } else {
          log(() -> "  FAILED TO REMOVE PRED " + o + " FROM NODE " + m_object);
        }
      }

      return result;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("[Node:" + m_object);
      sb.append("  pred:");
      for (T o : m_predecessors.values()) {
        sb.append(" ").append(o);
      }
      sb.append("]");
      return sb.toString();
    }

    public void addPredecessor(T tm) {
      log("  ADDING PREDECESSOR FOR " + m_object + " ==> " + tm);
      m_predecessors.put(tm, tm);
    }

    public boolean hasPredecessors() {
      return !m_predecessors.isEmpty();
    }
  }
}
