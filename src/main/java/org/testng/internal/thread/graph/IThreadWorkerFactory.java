package org.testng.internal.thread.graph;

import java.util.List;
import java.util.Set;


/**
 * A factory that creates workers used by {@code GraphThreadPoolExecutor}
 * @author nullin
 *
 * @param <T>
 */
public interface IThreadWorkerFactory<T> {

  /**
   * Creates {@code IWorker} for specified set of tasks. It is not necessary that
   * number of workers returned be same as number of tasks entered.
   *
   * @param freeNodes tasks that need to be executed
   * @return list of workers
   */
  List<IWorker<T>> createWorkers(List<T> freeNodes);
}
