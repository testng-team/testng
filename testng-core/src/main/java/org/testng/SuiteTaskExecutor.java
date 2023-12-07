package org.testng;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.testng.internal.IConfiguration;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.Utils;
import org.testng.internal.thread.TestNGThreadFactory;
import org.testng.internal.thread.graph.GraphOrchestrator;
import org.testng.log4testng.Logger;
import org.testng.thread.IExecutorFactory;
import org.testng.thread.ITestNGThreadPoolExecutor;
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
    if (RuntimeBehavior.favourCustomThreadPoolExecutor()) {
      IExecutorFactory execFactory = configuration.getExecutorFactory();
      ITestNGThreadPoolExecutor executor =
          execFactory.newSuiteExecutor(
              name,
              graph,
              factory,
              threadPoolSize,
              threadPoolSize,
              Integer.MAX_VALUE,
              TimeUnit.MILLISECONDS,
              queue,
              null);
      executor.run();
      service = executor;
    } else {
      service =
          new ThreadPoolExecutor(
              threadPoolSize,
              threadPoolSize,
              Integer.MAX_VALUE,
              TimeUnit.MILLISECONDS,
              queue,
              new TestNGThreadFactory(name));
      GraphOrchestrator<ISuite> executor = new GraphOrchestrator<>(service, factory, graph, null);
      executor.run();
    }
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
