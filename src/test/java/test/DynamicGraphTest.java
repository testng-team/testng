package test;

import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;

import java.util.Comparator;
import java.util.List;

public class DynamicGraphTest {

  private static class Node {
    private final String name;
    private final int priority;

    private Node(String name) {
      this(name, 0);
    }

    private Node(String name, int priority) {
      this.name = name;
      this.priority = priority;
    }
  }

  private static void assertFreeNodesEquals(DynamicGraph<Node> graph, Node... expected) {
    List<Node> freeNodes = graph.getFreeNodes();
    String[] actual = new String[freeNodes.size()];
    for (int i=0; i<actual.length; i++) {
      actual[i] = freeNodes.get(i).name;
    }
    String[] expectedNodes = new String[expected.length];
    for (int i=0; i<expectedNodes.length; i++) {
      expectedNodes[i] = expected[i].name;
    }
    Assert.assertEqualsNoOrder(graph.getFreeNodes().toArray(), expected);
  }

  @Test
  public void test8() {
    /*
      digraph test8 {
        a1;
        a2;
        b1 -> {a1; a2;}
        b2 -> {a1; a2;}
        c1 -> {b1; b2;}
        x;
        y;
      }
    */
    DynamicGraph<Node> dg = new DynamicGraph<>();
    dg.setComparator(new Comparator<Node>() {
      @Override
      public int compare(Node o1, Node o2) {
        return o1.priority - o2.priority;
      }
    });
    Node a1 = new Node("a1", 1);
    Node a2 = new Node("a2", 2);
    Node b1 = new Node("b1", 1);
    Node b2 = new Node("b2", 2);
    Node c1 = new Node("c1", 1);
    dg.addNode(a1);
    dg.addNode(a2);
    dg.addNode(b1);
    dg.addNode(b2);
    dg.addNode(c1);
    dg.addEdge(b1, a1, a2);
    dg.addEdge(b2, a1, a2);
    dg.addEdge(c1, b1, b2);
    Node x = new Node("x");
    Node y = new Node("y");
    dg.addNode(x);
    dg.addNode(y);
    List<Node> freeNodes = dg.getFreeNodes();
    assertFreeNodesEquals(dg, y, x);
    dg.setStatus(freeNodes, Status.RUNNING);

    freeNodes = dg.getFreeNodes();
    assertFreeNodesEquals(dg, a1);
    dg.setStatus(freeNodes, Status.RUNNING);

    freeNodes = dg.getFreeNodes();
    assertFreeNodesEquals(dg, a2);
    dg.setStatus(freeNodes, Status.RUNNING);

    dg.setStatus(a1, Status.FINISHED);
    assertFreeNodesEquals(dg);

    dg.setStatus(a2, Status.FINISHED);
    assertFreeNodesEquals(dg, b1);

    dg.setStatus(b2, Status.RUNNING);
    dg.setStatus(b1, Status.FINISHED);
    assertFreeNodesEquals(dg);

    dg.setStatus(b2, Status.FINISHED);
    assertFreeNodesEquals(dg, c1);
  }

  @Test
  public void test2() {
    /*
      digraph test2 {
        a1;
        a2;
        b1 -> {a1; a2;}
        x;
      }
    */
    DynamicGraph<Node> dg = new DynamicGraph<>();
    Node a1 = new Node("a1", 1);
    Node a2 = new Node("a2", 2);
    Node b1 = new Node("b1", 1);
    dg.addNode(a1);
    dg.addNode(a2);
    dg.addNode(b1);
    dg.addEdge(b1, a1, a2);
    Node x = new Node("x");
    dg.addNode(x);
    List<Node> freeNodes = dg.getFreeNodes();
    assertFreeNodesEquals(dg, a1, a2, x);

    dg.setStatus(freeNodes, Status.RUNNING);
    dg.setStatus(a1, Status.FINISHED);
    assertFreeNodesEquals(dg);

    dg.setStatus(a2, Status.FINISHED);
    assertFreeNodesEquals(dg, b1);

    Node b2 = new Node("b2", 2);
    dg.setStatus(b2, Status.RUNNING);
    dg.setStatus(b1, Status.FINISHED);
    assertFreeNodesEquals(dg);
  }

}
