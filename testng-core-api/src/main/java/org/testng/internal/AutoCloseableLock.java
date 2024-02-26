package org.testng.internal;

import java.io.Closeable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A simple abstraction over {@link ReentrantLock} that can be used in conjunction with <code>
 * try..resources</code> constructs.
 */
public final class AutoCloseableLock implements Closeable {

  private final ReentrantLock internalLock = new ReentrantLock();

  public AutoCloseableLock lock() {
    internalLock.lock();
    return this;
  }

  @Override
  public void close() {
    internalLock.unlock();
  }
}
