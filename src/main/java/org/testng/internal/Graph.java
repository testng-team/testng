package org.testng.internal;

import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Simple graph class to implement topological sort (used to sort methods based on what groups
 * they depend on).
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class Graph<T> {
  private static boolean m_verbose = false;
  private Map<T, Node<T>> m_nodes = Maps.newLinkedHashMap();
  private List<T> m_strictlySortedNodes = null;

  //  A map of nodes that are not the predecessors of any node
  // (not needed for the algorithm but convenient to calculate
  // the parallel/sequential lists in TestNG).
  private Map<T, Node<T>> m_independentNodes = null;

  public void addNode(T tm) {
    ppp("ADDING NODE " + tm + " " + tm.hashCode());
    m_nodes.put(tm, new Node<>(tm));
    // Initially, all the nodes are put in the independent list as well
  }

  public Set<T> getPredecessors(T node) {
    return findNode(node).getPredecessors().keySet();
  }

  public boolean isIndependent(T object) {
    return m_independentNodes.containsKey(object);
  }

  private Node<T> findNode(T object) {
    return m_nodes.get(object);
  }

  public void addPredecessor(T tm, T predecessor) {
    Node<T> node = findNode(tm);
    if (null == node) {
      throw new TestNGException("Non-existing node: " + tm);
    }
    else {
      node.addPredecessor(predecessor);
      addNeighbor(tm, predecessor);
      // Remove these two nodes from the independent list
      initializeIndependentNodes();
      m_independentNodes.remove(predecessor);
      m_independentNodes.remove(tm);
      ppp("  REMOVED " + predecessor + " FROM INDEPENDENT OBJECTS");
    }
  }

  private void addNeighbor(T tm, T predecessor) {
    findNode(tm).addNeighbor(findNode(predecessor));
  }

  public Set<T> getNeighbors(T t) {
    Set<T> result = new HashSet<>();
    for (Node<T> n : findNode(t).getNeighbors()) {
      result.add(n.getObject());
    }

    return result;
  }

  private Collection<Node<T>> getNodes() {
    return m_nodes.values();
  }

  public Collection<T> getNodeValues() {
    return m_nodes.keySet();
  }

  /**
   * @return All the nodes that don't have any order with each other.
   */
  public Set<T> getIndependentNodes() {
    return m_independentNodes.keySet();
  }

  /**
   * @return All the nodes that have an order with each other, sorted
   * in one of the valid sorts.
   */
  public List<T> getStrictlySortedNodes() {
    return m_strictlySortedNodes;
  }

  public void topologicalSort() {
    ppp("================ SORTING");
    m_strictlySortedNodes = Lists.newArrayList();
    initializeIndependentNodes();

    //
    // Clone the list of nodes but only keep those that are
    // not independent.
    //
    List<Node<T>> nodes2 = Lists.newArrayList();
    for (Node<T> n : getNodes()) {
      if (! isIndependent(n.getObject())) {
        ppp("ADDING FOR SORT: " + n.getObject());
        nodes2.add(n.clone());
      }
      else {
        ppp("SKIPPING INDEPENDENT NODE " + n);
      }
    }

    //
    // Sort the nodes alphabetically to make sure that methods of the same class
    // get run close to each other as much as possible
    //
    Collections.sort(nodes2);

    //
    // Sort
    //
    while (! nodes2.isEmpty()) {

      //
      // Find all the nodes that don't have any predecessors, add
      // them to the result and mark them for removal
      //
      Node<T> node = findNodeWithNoPredecessors(nodes2);
      if (null == node) {
        List<T> cycle = new Tarjan<>(this, nodes2.get(0).getObject()).getCycle();
        StringBuilder sb = new StringBuilder();
        sb.append("The following methods have cyclic dependencies:\n");
        for (T m : cycle) {
          sb.append(m).append("\n");
        }
        throw new TestNGException(sb.toString());
      }
      else {
        m_strictlySortedNodes.add(node.getObject());
        removeFromNodes(nodes2, node);
      }
    }

    ppp("=============== DONE SORTING");
    if (m_verbose) {
      dumpSortedNodes();
    }
  }

  private void initializeIndependentNodes() {
    if (null == m_independentNodes) {
      List<Node<T>> list = Lists.newArrayList(m_nodes.values());
      // Ideally, we should not have to sort this. However, due to a bug where it treats all the methods as
      // dependent nodes. Therefore, all the nodes were mostly sorted alphabetically. So we need to keep
      // the behavior for now.
      Collections.sort(list);
      m_independentNodes = Maps.newLinkedHashMap();
      for (Node<T> node : list) {
        m_independentNodes.put(node.getObject(), node);
      }
    }
  }

  private void dumpSortedNodes() {
    System.out.println("====== SORTED NODES");
    for (T n : m_strictlySortedNodes) {
      System.out.println("              " + n);
    }
    System.out.println("====== END SORTED NODES");
  }

  /**
   * Remove a node from a list of nodes and update the list of predecessors
   * for all the remaining nodes.
   */
  private void removeFromNodes(List<Node<T>> nodes, Node<T> node) {
    nodes.remove(node);
    for (Node<T> n : nodes) {
      n.removePredecessor(node.getObject());
    }
  }

  private static void ppp(String s) {
    if (m_verbose) {
      System.out.println("[Graph] " + s);
    }
  }

  private Node<T> findNodeWithNoPredecessors(List<Node<T>> nodes) {
    for (Node<T> n : nodes) {
      if (! n.hasPredecessors()) {
        return n;
      }
    }

    return null;
  }

  /**
   * @param o
   * @return A list of all the predecessors for o
   */
  public List<T> findPredecessors(T o) {
    // Locate the node
    Node<T> node = findNode(o);
    if (null == node) {
      // This can happen if an interceptor returned new methods
      return Lists.newArrayList();
    }

    // If we found the node, use breadth first search to find all
    // all of the predecessors of o.  "result" is the growing list
    // of all predecessors.  "visited" is the set of items we've
    // already encountered.  "queue" is the queue of items whose
    // predecessors we haven't yet explored.

    LinkedList<T> result = new LinkedList<>();
    Set<T> visited = new HashSet<>();
    LinkedList<T> queue = new LinkedList<>();
    visited.add(o);
    queue.addLast(o);

    while (! queue.isEmpty()) {
      for (T obj : getPredecessors(queue.removeFirst())) {
        if (! visited.contains(obj)) {
          visited.add(obj);
          queue.addLast(obj);
          result.addFirst(obj);
        }
      }
    }

    return result;
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
  public static class Node<T> implements Comparable<Node<T>> {
    private T m_object = null;
    private Map<T, T> m_predecessors = Maps.newHashMap();

    public Node(T tm) {
      m_object = tm;
    }

    private Set<Node<T>> m_neighbors = new HashSet<>();
    public void addNeighbor(Node<T> neighbor) {
      m_neighbors.add(neighbor);
    }

    public Set<Node<T>> getNeighbors() {
      return m_neighbors;
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
     *
     * @return true if this predecessor was found and removed
     */
    public boolean removePredecessor(T o) {
      boolean result = false;

      T pred = m_predecessors.get(o);
      if (null != pred) {
        result = null != m_predecessors.remove(o);
        if (result) {
          ppp("  REMOVED PRED " + o + " FROM NODE " + m_object);
        }
        else {
          ppp("  FAILED TO REMOVE PRED " + o + " FROM NODE " + m_object);
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
      String result = sb.toString();
      return result;
    }

    public void addPredecessor(T tm) {
      ppp("  ADDING PREDECESSOR FOR " + m_object + " ==> " + tm);
      m_predecessors.put(tm, tm);
    }

    public boolean hasPredecessors() {
      return m_predecessors.size() > 0;
    }

    public boolean hasPredecessor(T m) {
      return m_predecessors.containsKey(m);
    }

    @Override
    public int compareTo(Node<T> o) {
      return getObject().toString().compareTo(o.getObject().toString());
    }
  }
}
