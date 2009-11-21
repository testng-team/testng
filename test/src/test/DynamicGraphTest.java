package test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;

import java.util.Arrays;
import java.util.Set;

public class DynamicGraphTest {

  @Test
  public void simple() {
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
    Assert.assertEquals(
        freeNodes,
        Arrays.asList(new String[] {"a2", "a1", "y", "x"}));

    dg.setStatus(freeNodes, Status.RUNNING);

    dg.setStatus("a1", Status.FINISHED);
    Assert.assertEquals(
        dg.getFreeNodes(),
        Arrays.asList(new String[] { }));

    dg.setStatus("a2", Status.FINISHED);
    Assert.assertEquals(
        dg.getFreeNodes(),
        Arrays.asList(new String[] { "b1", "b2" }));

    dg.setStatus("b2", Status.RUNNING);
    dg.setStatus("b1", Status.FINISHED);
    Assert.assertEquals(
        dg.getFreeNodes(),
        Arrays.asList(new String[] { }));

    dg.setStatus("b2", Status.FINISHED);
    Assert.assertEquals(
        dg.getFreeNodes(),
        Arrays.asList(new String[] { "c1" }));

  }
}
