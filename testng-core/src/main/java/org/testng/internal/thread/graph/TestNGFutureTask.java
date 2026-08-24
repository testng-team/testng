package org.testng.internal.thread.graph;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;
import org.jspecify.annotations.Nullable;
import org.testng.thread.IWorker;

public class TestNGFutureTask<T> extends FutureTask<IWorker<T>> implements IWorker<T> {

  private final IWorker<T> worker;
  private final BiConsumer<IWorker<T>, @Nullable Throwable> callback;

  public TestNGFutureTask(IWorker<T> worker, BiConsumer<IWorker<T>, @Nullable Throwable> callback) {
    super(worker, worker);
    this.callback = callback;
    this.worker = worker;
  }

  @Override
  public void run() {
    super.run();
  }

  @Override
  protected void done() {
    @Nullable Throwable throwable = null;
    try {
      // get() is called only to detect exceptional completion; the result is always the worker
      // itself (see super(worker, worker) above). The worker must reach the callback either way,
      // or the orchestrator never learns which graph nodes finished. See GITHUB-3238.
      super.get();
    } catch (InterruptedException | ExecutionException e) {
      throwable = e;
    }
    callback.accept(worker, throwable);
  }

  @Override
  public List<T> getTasks() {
    return worker.getTasks();
  }

  @Override
  public long getTimeOut() {
    return worker.getTimeOut();
  }

  @Override
  public int getPriority() {
    return worker.getPriority();
  }

  @Override
  public int compareTo(IWorker<T> o) {
    return this.worker.compareTo(o);
  }
}
