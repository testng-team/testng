package org.testng.internal.thread.graph;

import java.util.List;
import org.testng.thread.IWorker;

class PhoneyWorker<T> implements IWorker<T> {
  private final long threadId;

  public PhoneyWorker(long threadId) {
    this.threadId = threadId;
  }

  @Override
  public List<T> getTasks() {
    // A PhoneyWorker stands in for a thread id, never for work: it has no tasks to report.
    return List.of();
  }

  @Override
  public long getTimeOut() {
    return 0;
  }

  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public int compareTo(IWorker<T> o) {
    return 0;
  }

  @Override
  public void run() {}

  @Override
  public long getThreadIdToRunOn() {
    return threadId;
  }
}
