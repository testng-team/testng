package org.testng;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.testng.internal.IConfiguration;
import org.testng.internal.ObjectBag;
import org.testng.internal.Utils;
import org.testng.internal.thread.TestNGThreadFactory;
import org.testng.internal.thread.graph.GraphOrchestrator;
import org.testng.log4testng.Logger;
import org.testng.thread.IThreadWorkerFactory;
import org.testng.xml.XmlTest;

class TestTaskExecutor {
  private final BlockingQueue<Runnable> queue;
  private final Comparator<ITestNGMethod> comparator;
  private final IDynamicGraph<ITestNGMethod> graph;
  private final XmlTest xmlTest;
  private final IThreadWorkerFactory<ITestNGMethod> factory;
  private final IConfiguration configuration;
  private final long timeOut;

  private ExecutorService service;

  private static final Logger LOGGER = Logger.getLogger(TestTaskExecutor.class);

  public TestTaskExecutor(
      IConfiguration configuration,
      XmlTest xmlTest,
      IThreadWorkerFactory<ITestNGMethod> factory,
      BlockingQueue<Runnable> queue,
      IDynamicGraph<ITestNGMethod> graph,
      Comparator<ITestNGMethod> comparator) {
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
    boolean reUse = xmlTest.getSuite().useGlobalThreadPool();
    Supplier<Object> supplier =
        () ->
            configuration
                .getExecutorServiceFactory()
                .create(
                    threadCount,
                    threadCount,
                    0,
                    TimeUnit.MILLISECONDS,
                    queue,
                    new TestNGThreadFactory(name));
    if (reUse) {
      ObjectBag bag = ObjectBag.getInstance(xmlTest.getSuite());
      service = (ExecutorService) bag.createIfRequired(ExecutorService.class, supplier);
    } else {
      service = (ExecutorService) supplier.get();
    }
    GraphOrchestrator<ITestNGMethod> executor =
        new GraphOrchestrator<>(service, factory, graph, comparator);
    executor.run();
  }

  public void awaitCompletion() {
    String msg =
        String.format(
            "Starting executor test %d with time out: %d milliseconds.", timeOut, timeOut);
    Utils.log("TestTaskExecutor", 2, msg);
    try {
      boolean ignored = service.awaitTermination(timeOut, TimeUnit.MILLISECONDS);
      service.shutdownNow();
    } catch (InterruptedException handled) {
      LOGGER.error(handled.getMessage(), handled);
      Thread.currentThread().interrupt();
    }
  }
}
