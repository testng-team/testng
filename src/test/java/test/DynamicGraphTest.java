package test;

import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DynamicGraphTest extends SimpleBaseTest {

  private static class Node {
    private final String name;

    private Node(String name) {
      this.name = name;
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
    Node a1 = new Node("a1");
    Node a2 = new Node("a2");
    Node b1 = new Node("b1");
    Node b2 = new Node("b2");
    Node c1 = new Node("c1");
    dg.addNode(a1);
    dg.addNode(a2);
    dg.addNode(b1);
    dg.addNode(b2);
    dg.addNode(c1);
    dg.addEdge(1, b1, a1, a2);
    dg.addEdge(1, b2, a1, a2);
    dg.addEdge(1, c1, b1, b2);
    dg.addEdge(0, a2, a1, b1, c1);
    dg.addEdge(0, b2, a1, b1, c1);
    Node x = new Node("x");
    Node y = new Node("y");
    dg.addNode(x);
    dg.addNode(y);
    dg.addEdge(0, a1, x, y);
    dg.addEdge(0, b1, x, y);
    dg.addEdge(0, c1, x, y);
    assertFreeNodesEquals(dg, y, x);
    dg.setStatus( dg.getFreeNodes(), Status.RUNNING);

    assertFreeNodesEquals(dg, a1, a2);
    dg.setStatus(dg.getFreeNodes(), Status.RUNNING);

    dg.setStatus(a1, Status.FINISHED);
    assertFreeNodesEquals(dg);

    dg.setStatus(a2, Status.FINISHED);
    assertFreeNodesEquals(dg, b1, b2);

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
    Node a1 = new Node("a1");
    Node a2 = new Node("a2");
    Node b1 = new Node("b1");
    dg.addNode(a1);
    dg.addNode(a2);
    dg.addNode(b1);
    dg.addEdge(1, b1, a1, a2);
    dg.addEdge(0, a2, a1, b1);
    Node x = new Node("x");
    dg.addNode(x);
    dg.addEdge(0, a1, x);
    dg.addEdge(0, b1, x);
    assertFreeNodesEquals(dg, x);
    dg.setStatus(dg.getFreeNodes(), Status.RUNNING);
    assertFreeNodesEquals(dg, a1, a2);
    dg.setStatus(dg.getFreeNodes(), Status.RUNNING);

    dg.setStatus(a1, Status.FINISHED);
    assertFreeNodesEquals(dg);

    dg.setStatus(a2, Status.FINISHED);
    assertFreeNodesEquals(dg, b1);

    Node b2 = new Node("b2"); // 2
    dg.setStatus(b2, Status.RUNNING);
    dg.setStatus(b1, Status.FINISHED);
    assertFreeNodesEquals(dg);
  }

  @Test
  public void testOrderingOfEdgesWithSameWeight() {
    Class<?>[] classes = new Class[] {
        TestClassContainerForGitHubIssue1360.TestNG1.class,
        TestClassContainerForGitHubIssue1360.TestNG2.class,
        TestClassContainerForGitHubIssue1360.TestNG3.class
    };
    List<ITestNGMethod> methods = extractTestNGMethods(classes);
    DynamicGraph<ITestNGMethod> graph = new DynamicGraph<>();

    ListMultiMap<Integer, ITestNGMethod> methodsByPriority = Maps.newListMultiMap();
    for (ITestNGMethod method : methods) {
      methodsByPriority.put(method.getPriority(), method);
      graph.addNode(method);
    }
    List<Integer> availablePriorities = Lists.newArrayList(methodsByPriority.keySet());
    Collections.sort(availablePriorities);
    Integer previousPriority = methods.size() > 0 ? availablePriorities.get(0) : 0;
    for (int i = 1; i < availablePriorities.size(); i++) {
      Integer currentPriority = availablePriorities.get(i);
      for (ITestNGMethod p0Method : methodsByPriority.get(previousPriority)) {
        for (ITestNGMethod p1Method : methodsByPriority.get(currentPriority)) {
          graph.addEdge(1, p1Method.getPriority(), p1Method, p0Method);
        }
      }
      previousPriority = currentPriority;
    }
    List<String> expected = Arrays.asList("TestNG1.test1TestNG1", "TestNG2.test1TestNG2", "TestNG3.test1TestNG3");
    runAssertion(graph, expected);
    expected = Arrays.asList("TestNG1.test2TestNG1", "TestNG2.test2TestNG2", "TestNG3.test2TestNG3");
    runAssertion(graph, expected);

    expected = Arrays.asList("TestNG1.test3TestNG1", "TestNG2.test3TestNG2", "TestNG3.test3TestNG3");
    runAssertion(graph, expected);
  }

  private static void runAssertion(DynamicGraph<ITestNGMethod> graph, List<String> expected) {
    List<ITestNGMethod> p1Methods = graph.getFreeNodes();
    Assert.assertEquals(p1Methods.size(), 3);
    graph.setStatus(p1Methods, Status.FINISHED);
    for (ITestNGMethod p1Method : p1Methods) {
      Assert.assertTrue(expected.contains(constructName(p1Method)));
    }
  }

  private static String constructName(ITestNGMethod method) {
    return method.getConstructorOrMethod().getDeclaringClass().getSimpleName() + "." + method.getMethodName();
  }

}
