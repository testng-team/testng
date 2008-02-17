package org.testng.internal;

import org.testng.TestNGException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Simple graph class to implement topological sort (used to sort methods based on what groups
 * they depend on).
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class Graph<T extends Object> {
  private static boolean m_verbose = false;
  private Map<T, Node<T>> m_nodes = new HashMap<T, Node<T>>();
  private List<T> m_strictlySortedNodes = null;
  
  //  A map of nodes that are not the predecessors of any node
  // (not needed for the algorithm but convenient to calculate
  // the parallel/sequential lists in TestNG).
  private Map<T, Node<T>> m_independentNodes = null;

  public void addNode(T tm) {
    ppp("ADDING NODE " + tm + " " + tm.hashCode());
    m_nodes.put(tm, new Node<T>(tm));
    // Initially, all the nodes are put in the independent list as well
  }
  
  public Set<T> getPredecessors(T node) {
    return findNode(node).getPredecessors().keySet();
  }
  
  /*private boolean hasBeenSorted() {
    return null != m_strictlySortedNodes;
  }*/
  
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
      if (null == m_independentNodes) {
        m_independentNodes = new HashMap<T, Node<T>>();
        for (T k : m_nodes.keySet()) {
          m_independentNodes.put(k, m_nodes.get(k));
        }
      }
      m_independentNodes.remove(predecessor);
      m_independentNodes.remove(tm);
      ppp("  REMOVED " + predecessor + " FROM INDEPENDENT OBJECTS");
    }
  }

  private void addNeighbor(T tm, T predecessor) {
    findNode(tm).addNeighbor(findNode(predecessor));
  }
  
  public Set<T> getNeighbors(T t) {
    Set<T> result = new HashSet<T>();
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
    m_strictlySortedNodes = new ArrayList<T>();
    if (null == m_independentNodes) {
      m_independentNodes = new HashMap<T, Node<T>>();
    }
    
    //
    // Clone the list of nodes but only keep those that are
    // not independent.
    //
    List<Node<T>> nodes2 = new ArrayList<Node<T>>();
    for (Node<T> n : getNodes()) {
      if (! isIndependent((T) n.getObject())) {
        ppp("ADDING FOR SORT: " + n.getObject());
        nodes2.add(n.clone());
      }
      else {
        ppp("SKIPPING INDEPENDENT NODE " + n);
      }
    }
    
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
        List<T> cycle = new Tarjan<T>(this, nodes2.get(0).getObject()).getCycle();
        StringBuffer sb = new StringBuffer();
        sb.append("The following methods have cyclic dependencies:\n");
        for (T m : cycle) {
          sb.append(m).append("\n");
        }
        throw new TestNGException(sb.toString());
      }
      else {
        m_strictlySortedNodes.add((T) node.getObject());
        removeFromNodes(nodes2, node);
      }
    }

    ppp("=============== DONE SORTING");
    if (m_verbose) {
      dumpSortedNodes();
    }
  }
  
  private void dumpSortedNodes() {
    System.out.println("====== SORTED NODES");
    for (T n : m_strictlySortedNodes) {
      System.out.println("              " + n);
    }
    System.out.println("====== END SORTED NODES");
  }

  private void dumpGraph() {
    System.out.println("====== GRAPH");
    for (Node<T> n : m_nodes.values()) {
      System.out.println("  " + n);
    }
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
    List<T> result = new ArrayList<T>();
    // Locate the node
    Node<T> node = findNode(o);
    
    if (null == node) {
      throw new AssertionError("No such node: " + o);
    }
    else {
      List<Node<T>> nodesToWalk = new ArrayList<Node<T>>();
      
      // Found the nodes, now find all its predecessors
      for (Node<T> n : getNodes()) {
        T obj = n.getObject();
        if (node.hasPredecessor(obj)) {
          ppp("FOUND PREDECESSOR " + n);
          if (! result.contains(obj)) {
            result.add(0, obj);
            nodesToWalk.add(n);
          }
        }
      }
      
      // Add all the predecessors of the nodes we just found
      for (Node<T> n : nodesToWalk) {
        List<T> r = findPredecessors(n.getObject());
        for (T obj : r) {
          if (! result.contains(obj)) {
            result.add(0, obj);
          }
        }
      }
    }
    
    return result;
  }
  
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer("[Graph ");
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
    private T m_object = null;
    private Map<T, T> m_predecessors = new HashMap<T, T>();
    
    public Node(T tm) {
      m_object = tm;
    }
    
    private Set<Node<T>> m_neighbors = new HashSet<Node<T>>();
    public void addNeighbor(Node<T> neighbor) {
      m_neighbors.add(neighbor);
    }
    
    public Set<Node<T>> getNeighbors() {
      return m_neighbors;
    }
        
    @Override
    public Node<T> clone() {
      Node<T> result = new Node<T>(m_object);
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
      
//      dump();
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
    
    private void dump() {
      ppp(toString());
    }
    
    @Override
    public String toString() {
      StringBuffer sb = new StringBuffer("[Node:" + m_object);
      sb.append("  pred:");
      for (T o : m_predecessors.values()) {
        sb.append(" " + o.toString());
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
  } 
  
  //
  // class Node
  /////
  
  public static void main(String[] argv) {
    Graph<String> g = new Graph<String>();
    g.addNode("3");
    g.addNode("1");
    g.addNode("2.2");
    g.addNode("independent");
    g.addNode("2.1");
    g.addNode("2");
    
    // 1 ->  2.1, 2.2, 2.3 --> 3
    g.addPredecessor("3", "2");
    g.addPredecessor("3", "2.1");
    g.addPredecessor("3", "2.2");
    g.addPredecessor("2", "1");
    g.addPredecessor("2.1", "1");
    g.addPredecessor("2.2", "1");
    
    g.topologicalSort();
    
    List<String> l = g.getStrictlySortedNodes();
    for (String s : l) {
      System.out.println("  " + s);
    }
    int i = 0;
    assert "1".equals(l.get(i));
    i++;
    assert "2".equals(l.get(i)) || "2.1".equals(l.get(i)) || "2.2".equals(l.get(i));
    i++;
    assert "2".equals(l.get(i)) || "2.1".equals(l.get(i)) || "2.2".equals(l.get(i));
    i++;
    assert "2".equals(l.get(i)) || "2.1".equals(l.get(i)) || "2.2".equals(l.get(i));
    i++;
    assert "3".equals(l.get(i));
    
    assert 1 == g.getIndependentNodes().size();
    
    //
    // Test findPredecessors
    //
    ppp("GRAPH:" + g);
    {
      List<String> predecessors = g.findPredecessors("2");
      assert predecessors.size() == 1;
      assert predecessors.get(0).equals("1");
    }
    
    {
      List<String> predecessors = g.findPredecessors("3");
      assert predecessors.size() == 4;
      assert predecessors.get(0).equals("1");
      assert predecessors.get(1).equals("2.1") ||
        predecessors.get(2).equals("2.2") ||
        predecessors.get(2).equals("2");
      assert predecessors.get(2).equals("2.1") ||
      predecessors.get(2).equals("2.2") ||
      predecessors.get(2).equals("2");
      assert predecessors.get(3).equals("2.1") ||
      predecessors.get(2).equals("2.2") ||
      predecessors.get(2).equals("2");
    }

    ppp("TESTS PASSED");
  }
  
}
