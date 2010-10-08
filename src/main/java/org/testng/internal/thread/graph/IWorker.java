package org.testng.internal.thread.graph;

import java.util.List;

/**
 * A runnable object that is used by {@code GraphThreadPoolExecutor} to execute
 * tasks
 */
public interface IWorker<T> extends Runnable, Comparable<IWorker<T>> {

  /**
   * @return list of tasks this worker is working on.
   */
  List<T> getTasks();

  /**
   * @return the maximum time allowed for the worker to complete the task.
   */
  long getTimeOut();

  /**
   * @return the priority of this task.
   */
  int getPriority();
}
