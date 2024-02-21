package org.testng;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.testng.internal.IConfiguration;
import org.testng.internal.Utils;
import org.testng.internal.thread.TestNGThreadFactory;
import org.testng.internal.thread.graph.GraphOrchestrator;
import org.testng.log4testng.Logger;
import org.testng.thread.IThreadWorkerFactory;

class SuiteTaskExecutor {
  private final BlockingQueue<Runnable> queue;
  private final IDynamicGraph<ISuite> graph;
  private final IThreadWorkerFactory<ISuite> factory;
  private final IConfiguration configuration;

  private final int threadPoolSize;

  private ExecutorService service;

  private static final Logger LOGGER = Logger.getLogger(SuiteTaskExecutor.class);

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
    GraphOrchestrator<ISuite> executor = new GraphOrchestrator<>(service, factory, graph, null);
    executor.run();
  }

  public void awaitCompletion() {
    Utils.log("TestNG", 2, "Starting executor for all suites");
    try {
      boolean ignored = service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      service.shutdownNow();
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
      LOGGER.error(handled.getMessage(), handled);
    }
  }
}
