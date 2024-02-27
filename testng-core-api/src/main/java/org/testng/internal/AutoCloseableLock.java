package org.testng.internal;

import java.io.Closeable;
import java.util.Objects;
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

  public boolean isHeldByCurrentThread() {
    return internalLock.isHeldByCurrentThread();
  }

  @Override
  public void close() {
    internalLock.unlock();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    AutoCloseableLock that = (AutoCloseableLock) object;
    return Objects.equals(internalLock, that.internalLock);
  }

  @Override
  public int hashCode() {
    return Objects.hash(internalLock);
  }
}
