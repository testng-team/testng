package org.testng.internal;

import org.testng.collections.Lists;
import org.testng.collections.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class allows a caller to pass a list of Callable workers that will be run in threads
 * taken from the thread pool.  Each list of callables is indexed with a key, and when they
 * have all completed, the listener passed in @code{submitTask} will be invoked with all
 * the futures.  Additional workers can be submitted to this pool while it is in existence
 * and the caller can either block while waiting for the results (@code{submitTasksAndWait})
 * or submit them asynchronously (@code{submitTasks}) and supply listeners to notify when
 * the callers are done.
 *  
 * @author cbeust
 *
 * @param <KeyType> The type of the key to index each list of Callables
 * @param <FutureType> The type of the result returned by the futures
 */
public class PoolService<KeyType, FutureType> {
  private static PoolService m_instance;

  private ExecutorService m_service;
  private HashMap<KeyType, List<Future<FutureType>>> m_futureMap;
  private Thread m_listenerThread;
  private Map<KeyType, PoolListener<KeyType, FutureType>> m_listeners;

  /**
   * One pool is created per XmlSuite object.
   * 
   * @param threadPoolSize the size of the thread pool
   */
  private PoolService(int threadPoolSize) {
    m_instance = this;
    m_service = Executors.newFixedThreadPool(threadPoolSize);
    m_futureMap = new HashMap<KeyType, List<Future<FutureType>>>();
    m_listeners = Maps.newHashMap();

    m_listenerThread = new Thread() {
      public void run() {
        System.out.println("Listener thread starting, futures:" + m_futureMap.size());
        while (m_futureMap.size() > 0) {
          List<KeyType> doneFutures = Lists.newArrayList();
          for (KeyType key : m_futureMap.keySet()) {
            List<Future<FutureType>> futures = m_futureMap.get(key);
            if (isFinished(futures)) {
              PoolListener<KeyType, FutureType> listener = m_listeners.get(key);
              if (listener != null) {
                listener.onFinish(key, futures);
              }
              m_listeners.remove(key);
              doneFutures.add(key);
            }
          }
          for (KeyType key : doneFutures) {
            m_futureMap.remove(key);
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        System.out.println("Listener thread ending");
      }
    };
  }

  public static void initialize(int threadCount) {
    m_instance = new PoolService(threadCount);
  }

  public static PoolService getInstance() {
    if (m_instance == null) {
      throw new RuntimeException("The Service Pool was not created, should never happen");
    }

    return m_instance;
  }

  /**
   * This listener will be invoked as soon as all the Callables in a given list have completed.
   */
  public static interface PoolListener<KeyType, FutureType> {
    public void onFinish(KeyType key, List<Future<FutureType>> results);
  }

  /**
   * Submit the tasks to the thread pool.
   */
  public List<FutureType> submitTasksAndWait(KeyType key, List<Callable<FutureType>> tasks) {
    submitTasks(key, tasks, null);
    List<Future<FutureType>> futures = m_futureMap.get(key);
    while (!isFinished(futures)) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    List<FutureType> result = Lists.newArrayList();
    for (Future<FutureType> future : futures) {
      try {
        result.add(future.get());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  public void submitTasks(KeyType key, List<Callable<FutureType>> tasks,
      PoolListener<KeyType, FutureType> listener) {

    for (Callable<FutureType> task : tasks) {
      Future<FutureType> future = m_service.submit(task);
      List<Future<FutureType>> list = m_futureMap.get(key);
      if (list == null) {
        list = Lists.newArrayList();
        m_futureMap.put(key, list);
      }
      list.add(future);
    }

    if (listener != null) {
      m_listeners.put(key, listener);

      if (!m_listenerThread.isAlive()) {
        m_listenerThread.start();
      }
    }
  }

  private boolean isFinished(List<Future<FutureType>> futures) {
    for (Future<FutureType> f : futures) {
      if (!f.isDone())
        return false;
    }
    return true;
  }

  /**
   * Shut down the service.
   */
  public void shutdown() {
    Utils.log(getClass().getName(), 2, "Shutting down poolservice " + this + " terminated:"
        + m_service.isTerminated());
    //      if (m_service.isTerminated()) {
    m_service.shutdown();
    //      }
  }

}
