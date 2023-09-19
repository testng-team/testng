package org.testng.internal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

/**
 * Implementation of the Tarjan algorithm to find and display a cycle in a graph.
 *
 * @author cbeust
 */
public class Tarjan<T> {
  int m_index = 0;
  private final Stack<T> stack;
  Map<T, Integer> visitedNodes = Maps.newHashMap();
  Map<T, Integer> m_lowlinks = Maps.newHashMap();
  private List<T> m_cycle;

  public Tarjan(Graph<T> graph, T start) {
    stack = new Stack<>();
    run(graph, start);
  }

  private void run(Graph<T> graph, T start) {
    visitedNodes.put(start, m_index);
    m_lowlinks.put(start, m_index);
    m_index++;
    stack.push(start);

    for (T predecessor : graph.getPredecessors(start)) {
      if (!visitedNodes.containsKey(predecessor)) {
        run(graph, predecessor);
        int min = Math.min(m_lowlinks.get(start), m_lowlinks.get(predecessor));
        m_lowlinks.put(start, min);
      } else if (stack.contains(predecessor)) {
        int min = Math.min(m_lowlinks.get(start), visitedNodes.get(predecessor));
        m_lowlinks.put(start, min);
      }
    }

    if (Objects.equals(m_lowlinks.get(start), visitedNodes.get(start))) {
      m_cycle = Lists.newArrayList();
      T n;
      do {
        n = stack.pop();
        m_cycle.add(n);
      } while (!n.equals(start));
    }
  }

  public List<T> getCycle() {
    return m_cycle;
  }
}
