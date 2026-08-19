package org.testng;

import java.util.Comparator;
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

  public void execute() {
    String name = "test-" + xmlTest.getName();
    int threadCount = Math.max(xmlTest.getThreadCount(), 1);
    this.reUse = xmlTest.getSuite().useGlobalThreadPool();
    if (this.reUse) {
      // A single, common pool is shared between regular test methods and their (parallel)
      // data-driven invocations. It is created via IExecutorServiceFactory#createGlobalThreadPool,
      // which by default returns a ForkJoinPool so that a data-driven method worker waiting for its
      // data-row tasks (submitted back into this same pool) helps run them itself (work-stealing)
      // instead of throttling throughput or dead-locking the suite. See GITHUB-3242.
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
    // A shared global pool (reUse) must not be shut down when this test's graph finishes - other
    // <test> graphs may still be using it. See GITHUB-3242.
    orchestrator = new GraphOrchestrator<>(service, factory, graph, comparator, !reUse);
    orchestrator.run();
  }

  public void awaitCompletion() {
    String msg =
        String.format(
            "Starting executor test %d with time out: %d milliseconds.", timeOut, timeOut);
    Utils.log("TestTaskExecutor", 2, msg);
    try {
      if (reUse) {
        // Shared global pool: wait for this test's graph to finish, but leave the pool running for
        // the other <test>s. It is disposed once, at the end of the run, via ObjectBag cleanup.
        boolean ignored =
            java.util.Objects.requireNonNull(orchestrator, "execute() has started the graph")
                .awaitCompletion(timeOut, TimeUnit.MILLISECONDS);
      } else {
        ExecutorService running =
            java.util.Objects.requireNonNull(service, "execute() has started the pool");
        boolean ignored = running.awaitTermination(timeOut, TimeUnit.MILLISECONDS);
        running.shutdownNow();
      }
    } catch (InterruptedException handled) {
      LOGGER.error(handled.getMessage(), handled);
      Thread.currentThread().interrupt();
    }
  }
}
