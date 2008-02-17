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
    Graph<String> g = new Graph<String>();
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
      Tarjan<String> t = new Tarjan<String>(g, "1");
      Assert.assertEquals(t.getCycle().size(), 3);
    }
    
  }
  
  private Graph<String> createCyclicGraph() {
    Graph<String> g = new Graph<String>();
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
    
    // Invoke sort to make sure there is no side effect
    g.topologicalSort();
    
    //
    // Test findPredecessors
    //
    {
      List<String> predecessors = g.findPredecessors("2");
      assert predecessors.size() == 1;
      assert predecessors.get(0).equals("1");
    }
    
    {
      List<String> predecessors = g.findPredecessors("3");

      assert predecessors.size() == 4;
      assert predecessors.get(0).equals("1");
      assert predecessors.get(1).equals("2.1") 
        || predecessors.get(1).equals("2.2") 
        || predecessors.get(1).equals("2");
      assert predecessors.get(2).equals("2.1") 
        || predecessors.get(2).equals("2.2") 
        || predecessors.get(2).equals("2");
      assert predecessors.get(3).equals("2.1") 
        || predecessors.get(3).equals("2.2") 
        || predecessors.get(3).equals("2");
    }
  }
}
