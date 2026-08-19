package org.testng.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of the Tarjan algorithm to find and display a cycle in a graph.
 *
 * @author cbeust
 */
public class Tarjan<T> {
  int m_index = 0;
  private final ArrayDeque<T> stack;
  Map<T, Integer> visitedNodes = new HashMap<>();
  Map<T, Integer> m_lowlinks = new HashMap<>();
  private List<T> m_cycle = new ArrayList<>();

  public Tarjan(Graph<T> graph, T start) {
    stack = new ArrayDeque<>();
    run(graph, start);
  }

  /** Every node this is asked for has been given a lowlink by {@link #run} on the way in. */
  private int lowlinkOf(T node) {
    return Objects.requireNonNull(m_lowlinks.get(node), "no lowlink recorded for the node");
  }

  private void run(Graph<T> graph, T start) {
    visitedNodes.put(start, m_index);
    m_lowlinks.put(start, m_index);
    m_index++;
    stack.push(start);

    for (T predecessor : graph.getPredecessors(start)) {
      if (!visitedNodes.containsKey(predecessor)) {
        run(graph, predecessor);
        int min = Math.min(lowlinkOf(start), lowlinkOf(predecessor));
        m_lowlinks.put(start, min);
      } else if (stack.contains(predecessor)) {
        int min = Math.min(lowlinkOf(start), Objects.requireNonNull(visitedNodes.get(predecessor)));
        m_lowlinks.put(start, min);
      }
    }

    if (Objects.equals(m_lowlinks.get(start), visitedNodes.get(start))) {
      m_cycle = new ArrayList<>();
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
