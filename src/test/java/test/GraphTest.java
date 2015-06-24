package test;

import org.testng.Assert;
import org.testng.TestNGException;
import org.testng.annotations.Test;
import org.testng.internal.Graph;
import org.testng.internal.Tarjan;

import java.util.List;


/**
 * This class
 *
 * @author cbeust
 */
public class GraphTest {

  @Test
  public void sort() {
    Graph<String> g = new Graph<>();
    g.addNode("3");
    g.addNode("1");
    g.addNode("2.2");
    g.addNode("independent");
    g.addNode("2.1");
    g.addNode("2");

    g.addPredecessor("3", "2");
    g.addPredecessor("3", "2.1");
    g.addPredecessor("3", "2.2");
    g.addPredecessor("2", "1");
    g.addPredecessor("2.1", "1");
    g.addPredecessor("2.2", "1");

    g.topologicalSort();
    List<String> l = g.getStrictlySortedNodes();
    int i = 0;
    Assert.assertTrue("1".equals(l.get(i)));
    i++;
    Assert.assertTrue("2".equals(l.get(i)) || "2.1".equals(l.get(i)) || "2.2".equals(l.get(i)));
    i++;
    Assert.assertTrue("2".equals(l.get(i)) || "2.1".equals(l.get(i)) || "2.2".equals(l.get(i)));
    i++;
    Assert.assertTrue("2".equals(l.get(i)) || "2.1".equals(l.get(i)) || "2.2".equals(l.get(i)));
    i++;
    Assert.assertTrue("3".equals(l.get(i)));

    Assert.assertTrue(1 == g.getIndependentNodes().size());
  }

  @Test(expectedExceptions = org.testng.TestNGException.class)
  public void cycleShouldFail() {
    Graph<String> g = createCyclicGraph();
    g.topologicalSort();
  }

  @Test
  public void cycleShouldBeCorrect() {
    Graph<String> g = null;
    try {
      g = createCyclicGraph();
      g.topologicalSort();
    }
    catch(TestNGException ex) {
      Tarjan<String> t = new Tarjan<>(g, "1");
      Assert.assertEquals(t.getCycle().size(), 3);
    }

  }

  private Graph<String> createCyclicGraph() {
    Graph<String> g = new Graph<>();
    g.addNode("3");
    g.addNode("2");
    g.addNode("1");

    g.addPredecessor("3", "2");
    g.addPredecessor("2", "1");
    g.addPredecessor("1", "3");

    return g;
  }

  @Test
  public void findPredecessors() {
    Graph<String> g = new Graph<>();
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

    // Invoke sort to make sure there is no side effect
    g.topologicalSort();

    //
    // Test findPredecessors
    //
    {
      List<String> predecessors = g.findPredecessors("2");
      Assert.assertTrue(predecessors.size() == 1);
      Assert.assertTrue(predecessors.get(0).equals("1"));
    }

    {
      List<String> predecessors = g.findPredecessors("3");

      Assert.assertTrue(predecessors.size() == 4);
      Assert.assertTrue(predecessors.get(0).equals("1"));
      Assert.assertTrue(predecessors.get(1).equals("2.1")
        || predecessors.get(1).equals("2.2")
        || predecessors.get(1).equals("2"));
      Assert.assertTrue(predecessors.get(2).equals("2.1")
        || predecessors.get(2).equals("2.2")
        || predecessors.get(2).equals("2"));
      Assert.assertTrue(predecessors.get(3).equals("2.1")
        || predecessors.get(3).equals("2.2")
        || predecessors.get(3).equals("2"));
    }
  }

  // Using an earlier implementation of Graph.findPrecessors, finding
  // predecessors in certain kinds of graphs where many nodes have the
  // same predecessors could be very slow, since the old implementation
  // would explore the same nodes repeatedly.  This situation could
  // happen when test methods are organized in several groups, with
  // dependsOnGroups annotations so that each method in one group depends
  // on all of the methods in another group.  If there were several
  // such groups depending on each other in a chain, the combinatorics
  // of the old method became excessive.  This test builds a 72-node graph that
  // emulates this situation, then call Graph.findPredecessors.  The old
  // implementation run this in 70+ seconds on my computer, the new implementation
  // takes a few milliseconds.  In practice, with larger graphs, the former
  // slowness could get very extreme, taking hours or more to complete
  // in some real user situations.
  //
  @Test(timeOut = 5000) // If this takes more than 5 seconds we've definitely regressed.
  public void findPredecessorsTiming() {
    Graph<String> g = new Graph<>();

    final String rootNode = "myroot";
    final String independentNode = "independent";
    g.addNode(rootNode);
    g.addNode(independentNode);

    final int maxDepth = 7;
    final int nodesPerDepth = 10; // must be < 100
    //
    // Add maxDepth groups of new nodes, where each group contains nodesPerDepth
    // nodes, and each node in a group a depth N has each node in the group
    // at depth (N-1) as a predecessor.
    //
    for (int depth = 1; depth <= maxDepth; depth++) {
      for (int i = 0; i < nodesPerDepth; i++) {
        String newNode = String.valueOf(i + (100 * depth));
        g.addNode(newNode);
        if (depth == 1) {
          continue;
        }
        for (int j = 0; j < nodesPerDepth; j++) {
          String prevNode = String.valueOf(j + (100 * (depth - 1)));
          g.addPredecessor(newNode, prevNode);
        }
      }
    }

    // Finally, make all of the nodes in the group at depth maxDepth
    // be predecessors of rootNode.
    //
    for (int i = 0; i < nodesPerDepth; i++) {
      String node = String.valueOf(i + (100 * maxDepth));
      g.addPredecessor(rootNode, node);
    }

    // Now we're done building the graph, which has (maxDepth * nodesPerDepth) + 2
    // nodes.  rootNode has all of the other nodes except independentNode
    // as predecessors.

    //
    // Test findPredecessors
    //
    {
      List<String> predecessors = g.findPredecessors(rootNode);
      Assert.assertTrue(predecessors.size() == (maxDepth * nodesPerDepth));
    }
  }
}
