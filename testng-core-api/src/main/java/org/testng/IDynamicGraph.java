package org.testng;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents the graphical representative capabilities of an entity. The entities could be either a
 * {@link ISuite} or an {@link ITestNGMethod} object which are usually the logical units of work
 * that TestNG deals with.
 */
public interface IDynamicGraph<T> {

  boolean addNode(T node);

  void addEdge(int weight, T from, T to);

  void setVisualisers(Set<IExecutionVisualiser> listener);

  void addEdges(int weight, T from, Iterable<T> tos);

  List<T> getFreeNodes();

  List<T> getDependenciesFor(T node);

  void setStatus(Collection<T> nodes, Status status);

  void setStatus(T node, Status status);

  int getNodeCount();

  int getNodeCountWithStatus(Status status);

  Set<T> getNodesWithStatus(Status status);

  String toDot();

  enum Status {
    READY,
    RUNNING,
    FINISHED
  }
}
