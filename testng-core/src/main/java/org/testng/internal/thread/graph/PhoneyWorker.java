package org.testng.internal.thread.graph;

import java.util.List;
import javax.annotation.Nonnull;
import org.testng.thread.IWorker;

class PhoneyWorker<T> implements IWorker<T> {
  private final long threadId;

  public PhoneyWorker(long threadId) {
    this.threadId = threadId;
  }

  @Override
  public List<T> getTasks() {
    return null;
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
  public int compareTo(@Nonnull IWorker<T> o) {
    return 0;
  }

  @Override
  public void run() {}

  @Override
  public long getThreadIdToRunOn() {
    return threadId;
  }
}
