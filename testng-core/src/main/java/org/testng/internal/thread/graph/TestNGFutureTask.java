package org.testng.internal.thread.graph;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;
import org.testng.thread.IWorker;

public class TestNGFutureTask<T> extends FutureTask<IWorker<T>> implements IWorker<T> {

  private final IWorker<T> worker;
  private final BiConsumer<IWorker<T>, Throwable> callback;

  public TestNGFutureTask(IWorker<T> worker, BiConsumer<IWorker<T>, Throwable> callback) {
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
    Throwable throwable = null;
    IWorker<T> result = null;
    try {
      result = super.get();
    } catch (InterruptedException | ExecutionException e) {
      throwable = e;
    }
    callback.accept(result, throwable);
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
