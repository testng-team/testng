package test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.internal.DynamicGraph;

import java.util.Arrays;

public class DynamicGraphTest {

  @Test
  public void simple() {
    DynamicGraph<String> mp = new DynamicGraph<String>();
    mp.addEdge("b1", "a1");
    mp.addEdge("b1", "a2");
    mp.addEdge("b2", "a1");
    mp.addEdge("b2", "a2");
    mp.addEdge("c1", "b1");
    mp.addEdge("c1", "b2");
    mp.addNode("x");
    mp.addNode("y");
    Assert.assertEquals(Arrays.asList(new String[] {"a2", "a1", "y", "x"}), mp.getFreeNodes());
    Assert.assertEquals(Arrays.asList(new String[] {"a2", "y", "x"}),
        mp.getFreeNodesAfterRemoving("a1"));
    Assert.assertEquals(Arrays.asList(new String[] {"b1", "y", "b2", "x"}),
        mp.getFreeNodesAfterRemoving("a2"));
    Assert.assertEquals(Arrays.asList(new String[] {"y", "b2", "x"}),
        mp.getFreeNodesAfterRemoving("b1"));
    Assert.assertEquals(Arrays.asList(new String[] {"y", "x", "c1"}),
        mp.getFreeNodesAfterRemoving("b2"));
    Assert.assertEquals(Arrays.asList(new String[] {"y", "c1"}),
        mp.getFreeNodesAfterRemoving("x"));
  }
}
