package org.testng.internal.thread;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.testng.ITestResult;
import org.testng.internal.invokers.TestMethodWithDataProviderMethodWorker;

public final class Async {

  private Async() {
    // Defeat instantiation
  }

  public static CompletableFuture<List<ITestResult>> run(
      TestMethodWithDataProviderMethodWorker worker, ExecutorService service) {
    AsyncTask asyncTask = new AsyncTask(worker);
    service.execute(asyncTask);
    return asyncTask.result;
  }

  private static class AsyncTask implements Runnable, Comparable<AsyncTask> {
    private final CompletableFuture<List<ITestResult>> result = new CompletableFuture<>();
    private final TestMethodWithDataProviderMethodWorker worker;

    public AsyncTask(TestMethodWithDataProviderMethodWorker worker) {
      this.worker = worker;
    }

    @Override
    public void run() {
      try {
        if (!result.isDone()) {
          result.complete(worker.call());
        }
      } catch (Throwable t) {
        result.completeExceptionally(t);
      }
    }

    @Override
    public int compareTo(AsyncTask o) {
      return worker.compareTo(o.worker);
    }
  }
}
