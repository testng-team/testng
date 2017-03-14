package org.testng.internal;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

/**
 * Implementation of the Tarjan algorithm to find and display a cycle in a graph.
 * @author cbeust
 */
public class Tarjan<T> {
  int m_index = 0;
  private Stack<T> m_s;
  Map<T, Integer> m_indices = Maps.newHashMap();
  Map<T, Integer> m_lowlinks = Maps.newHashMap();
  private List<T> m_cycle;

  public Tarjan(Graph<T> graph, T start) {
    m_s = new Stack<>();
    run(graph, start);
  }

  private void run(Graph<T> graph, T v) {
    m_indices.put(v, m_index);
    m_lowlinks.put(v, m_index);
    m_index++;
    m_s.push(v);

    for (T vprime : graph.getPredecessors(v)) {
      if (! m_indices.containsKey(vprime)) {
        run(graph, vprime);
        int min = Math.min(m_lowlinks.get(v), m_lowlinks.get(vprime));
        m_lowlinks.put(v, min);
      }
      else if (m_s.contains(vprime)) {
        m_lowlinks.put(v, Math.min(m_lowlinks.get(v), m_indices.get(vprime)));
      }
    }

    if (Objects.equals(m_lowlinks.get(v), m_indices.get(v))) {
      m_cycle = Lists.newArrayList();
      T n;
      do {
        n = m_s.pop();
        m_cycle.add(n);
      } while (! n.equals(v));
    }

  }

  public static void main(String[] args) {
    Graph<String> g = new Graph<>();
    g.addNode("a");
    g.addNode("b");
    g.addNode("c");
    g.addNode("d");

    String[] edges = new String[] {
        "a", "b",
        "b", "a",
        "c", "d",
        "d", "a",
        "a", "c",
    };

    for (int i = 0; i < edges.length; i += 2) {
      g.addPredecessor(edges[i], edges[i+1]);
    }

    new Tarjan<>(g, "a");
  }

  public List<T> getCycle() {
    return m_cycle;
  }

}
