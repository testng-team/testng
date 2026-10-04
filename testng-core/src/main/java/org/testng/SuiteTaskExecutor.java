package org.testng;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import org.testng.internal.IConfiguration;
import org.testng.internal.Utils;
import org.testng.internal.thread.TestNGThreadFactory;
import org.testng.internal.thread.graph.GraphOrchestrator;
import org.testng.log4testng.Logger;
import org.testng.thread.IThreadWorkerFactory;

/**
 * Runs suites in parallel, in the order that a graph of the suites allows.
 *
 * <p>In the graph, a parent suite waits for its child suites. {@link TestNG} uses this class when
 * the suite thread pool has more than one thread, or when the suites run in random order.
 */
class SuiteTaskExecutor {
  private final BlockingQueue<Runnable> queue;
  private final IDynamicGraph<ISuite> graph;
  private final IThreadWorkerFactory<ISuite> factory;
  private final IConfiguration configuration;

  private final int threadPoolSize;

  private @Nullable ExecutorService service;
  private @Nullable GraphOrchestrator<ISuite> orchestrator;

  private static final Logger LOGGER = Logger.getLogger(SuiteTaskExecutor.class);

  /**
   * Creates an executor. Call {@link #execute()} to start it.
   *
   * @param configuration the configuration that gives the factory for the thread pool.
   * @param factory the factory that creates a worker for each suite.
   * @param queue the queue for the tasks of the thread pool.
   * @param graph the suites, and the order in which they can run.
   * @param threadPoolSize the number of threads that run suites.
   */
  public SuiteTaskExecutor(
      IConfiguration configuration,
      IThreadWorkerFactory<ISuite> factory,
      BlockingQueue<Runnable> queue,
      IDynamicGraph<ISuite> graph,
      int threadPoolSize) {
    this.configuration = configuration;
    this.factory = factory;
    this.queue = queue;
    this.graph = graph;
    this.threadPoolSize = threadPoolSize;
  }

  /**
   * Starts the thread pool, and starts the suites that can run first. This method does not wait.
   */
  public void execute() {
    String name = "suites-";
    service =
        this.configuration
            .getExecutorServiceFactory()
            .create(
                threadPoolSize,
                threadPoolSize,
                Integer.MAX_VALUE,
                TimeUnit.MILLISECONDS,
                queue,
                new TestNGThreadFactory(name));
    orchestrator = new GraphOrchestrator<>(service, factory, graph, null);
    orchestrator.run();
  }

  /**
   * Waits until every suite finishes. Then logs the error of each worker that ended on an
   * exception.
   */
  public void awaitCompletion() {
    Utils.log("TestNG", 2, "Starting executor for all suites");
    try {
      ExecutorService running = Objects.requireNonNull(service, "execute() has started the pool");
      boolean ignored = running.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      running.shutdownNow();
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
      LOGGER.error(handled.getMessage(), handled);
    }
    reportWorkerFailures();
  }

  /**
   * Logs the error of each suite worker that ended on an exception.
   *
   * <p>The orchestrator marks such a worker as finished, so that the other suites can still run.
   * After that, nothing can tell it from a worker that ended cleanly. Without this log, you would
   * need a debugger to find the cause. A listener that throws is the usual cause. See GITHUB-3243.
   */
  private void reportWorkerFailures() {
    if (orchestrator == null) {
      return;
    }
    for (Throwable failure : orchestrator.getFailures()) {
      LOGGER.error("A suite worker ended on an exception", failure);
    }
  }
}
