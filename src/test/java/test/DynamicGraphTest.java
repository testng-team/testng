package test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;

import java.util.Arrays;
import java.util.Set;

public class DynamicGraphTest {

  private static <T> void assertFreeNodesEquals(DynamicGraph<T> graph, T[] expected) {
    Assert.assertEqualsNoOrder(graph.getFreeNodes().toArray(), expected);
  }

  @Test
  public void test8() {
    DynamicGraph<String> dg = new DynamicGraph<String>();
    dg.addEdge("b1", "a1");
    dg.addEdge("b1", "a2");
    dg.addEdge("b2", "a1");
    dg.addEdge("b2", "a2");
    dg.addEdge("c1", "b1");
    dg.addEdge("c1", "b2");
    dg.addNode("x");
    dg.addNode("y");
    Set<String> freeNodes = dg.getFreeNodes();
    assertFreeNodesEquals(dg, new String[] {"a1", "a2", "y", "x"});

    dg.setStatus(freeNodes, Status.RUNNING);
    dg.setStatus("a1", Status.FINISHED);
    assertFreeNodesEquals(dg, new String[] {});

    dg.setStatus("a2", Status.FINISHED);
    assertFreeNodesEquals(dg, new String[] { "b1", "b2"});

    dg.setStatus("b2", Status.RUNNING);
    dg.setStatus("b1", Status.FINISHED);
    assertFreeNodesEquals(dg, new String[] {});

    dg.setStatus("b2", Status.FINISHED);
    assertFreeNodesEquals(dg, new String[] { "c1" });
  }

  @Test
  public void test2() {
    DynamicGraph<String> dg = new DynamicGraph<String>();
    dg.addEdge("b1", "a1");
    dg.addEdge("b1", "a2");
    dg.addNode("x");
    Set<String> freeNodes = dg.getFreeNodes();
    assertFreeNodesEquals(dg, new String[] { "a1", "a2", "x" });

    dg.setStatus(freeNodes, Status.RUNNING);
    dg.setStatus("a1", Status.FINISHED);
    assertFreeNodesEquals(dg, new String[] {});

    dg.setStatus("a2", Status.FINISHED);
    assertFreeNodesEquals(dg, new String[] { "b1" });

    dg.setStatus("b2", Status.RUNNING);
    dg.setStatus("b1", Status.FINISHED);
    assertFreeNodesEquals(dg, new String[] {});
  }

}
