package org.testng;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.testng.internal.IConfiguration;
import org.testng.internal.ObjectBag;
import org.testng.internal.Utils;
import org.testng.internal.thread.TestNGThreadFactory;
import org.testng.internal.thread.ThreadUtil;
import org.testng.internal.thread.graph.GraphOrchestrator;
import org.testng.log4testng.Logger;
import org.testng.thread.IThreadWorkerFactory;
import org.testng.xml.XmlTest;

/**
 * Runs the test methods of one {@code <test>} in parallel, in the order that a graph of the methods
 * allows.
 *
 * <p>{@link TestRunner} uses this class when the {@code <test>} runs its methods in parallel.
 */
class TestTaskExecutor {
  private final BlockingQueue<Runnable> queue;
  private final @Nullable Comparator<ITestNGMethod> comparator;
  private final IDynamicGraph<ITestNGMethod> graph;
  private final XmlTest xmlTest;
  private final IThreadWorkerFactory<ITestNGMethod> factory;
  private final IConfiguration configuration;
  private final long timeOut;

  private @Nullable ExecutorService service;
  private @Nullable GraphOrchestrator<ITestNGMethod> orchestrator;
  private boolean reUse;

  private static final Logger LOGGER = Logger.getLogger(TestTaskExecutor.class);

  /**
   * Creates an executor. Call {@link #execute()} to start it.
   *
   * @param configuration the configuration that gives the factory for the thread pool.
   * @param xmlTest the {@code <test>} that gives the thread count and the time-out.
   * @param factory the factory that creates the workers for the test methods.
   * @param queue the queue for the tasks of the thread pool.
   * @param graph the test methods, and the order in which they can run.
   * @param comparator sorts the methods that are free to run, or {@code null} to keep their order.
   */
  public TestTaskExecutor(
      IConfiguration configuration,
      XmlTest xmlTest,
      IThreadWorkerFactory<ITestNGMethod> factory,
      BlockingQueue<Runnable> queue,
      IDynamicGraph<ITestNGMethod> graph,
      @Nullable Comparator<ITestNGMethod> comparator) {
    this.configuration = configuration;
    this.xmlTest = xmlTest;
    this.factory = factory;
    this.queue = queue;
    this.graph = graph;
    this.comparator = comparator;
    this.timeOut = xmlTest.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS);
  }

  /**
   * Starts the thread pool, and starts the test methods that can run first. This method does not
   * wait.
   *
   * <p>When the suite uses the global thread pool, the {@code <test>} tags of the suite share one
   * pool. Otherwise, this method creates a pool for this {@code <test>}.
   */
  public void execute() {
    String name = "test-" + xmlTest.getName();
    int threadCount = Math.max(xmlTest.getThreadCount(), 1);
    this.reUse = xmlTest.getSuite().useGlobalThreadPool();
    if (this.reUse) {
      // One shared pool runs the test methods and the parallel invocations of their data
      // providers. IExecutorServiceFactory#createGlobalThreadPool creates it, and by default it is
      // a ForkJoinPool. The worker of a data-driven method puts the tasks for its data rows into
      // this same pool, and then waits for them. In a ForkJoinPool, the waiting worker runs those
      // tasks itself (work-stealing). In another pool, the run could slow down or stop in a
      // deadlock. See GITHUB-3242.
      String threadNamePrefix = ThreadUtil.THREAD_NAME + "-" + name;
      Supplier<Object> supplier =
          () ->
              configuration
                  .getExecutorServiceFactory()
                  .createGlobalThreadPool(threadCount, threadNamePrefix);
      ObjectBag bag = ObjectBag.getInstance(xmlTest.getSuite());
      service = (ExecutorService) bag.createIfRequired(ExecutorService.class, supplier);
    } else {
      service =
          configuration
              .getExecutorServiceFactory()
              .create(
                  threadCount,
                  threadCount,
                  0,
                  TimeUnit.MILLISECONDS,
                  queue,
                  new TestNGThreadFactory(name));
    }
    // Do not shut down the shared global pool (reUse) when the graph of this <test> finishes. The
    // graphs of other <test> tags can still use it. See GITHUB-3242.
    orchestrator = new GraphOrchestrator<>(service, factory, graph, comparator, !reUse);
    orchestrator.run();
  }

  /**
   * Waits until every test method finishes, or until the time-out of the {@code <test>} ends. Then
   * logs the error of each worker that ended on an exception.
   *
   * <p>A pool of this {@code <test>} shuts down after the wait. The shared global pool keeps
   * running for the other {@code <test>} tags.
   */
  public void awaitCompletion() {
    String msg =
        String.format(
            "Starting executor test %d with time out: %d milliseconds.", timeOut, timeOut);
    Utils.log("TestTaskExecutor", 2, msg);
    try {
      if (reUse) {
        // With the shared global pool, wait for the graph of this <test> only. Keep the pool
        // running for the other <test> tags. ObjectBag.cleanup() shuts it down once, at the end of
        // the run.
        boolean ignored =
            Objects.requireNonNull(orchestrator, "execute() has started the graph")
                .awaitCompletion(timeOut, TimeUnit.MILLISECONDS);
      } else {
        ExecutorService running = Objects.requireNonNull(service, "execute() has started the pool");
        boolean ignored = running.awaitTermination(timeOut, TimeUnit.MILLISECONDS);
        running.shutdownNow();
      }
    } catch (InterruptedException handled) {
      LOGGER.error(handled.getMessage(), handled);
      Thread.currentThread().interrupt();
    }
    reportWorkerFailures();
  }

  /**
   * Logs the error of each worker that ended on an exception.
   *
   * <p>The orchestrator marks such a worker as finished, so that the graph can go on. After that,
   * nothing can tell it from a worker that ended cleanly. Without this log, you would need a
   * debugger to find the cause. A listener that throws is the usual cause. See GITHUB-3243.
   */
  private void reportWorkerFailures() {
    if (orchestrator == null) {
      return;
    }
    for (Throwable failure : orchestrator.getFailures()) {
      LOGGER.error("A worker of test " + xmlTest.getName() + " ended on an exception", failure);
    }
  }
}
