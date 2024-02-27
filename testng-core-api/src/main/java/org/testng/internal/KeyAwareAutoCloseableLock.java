package org.testng.internal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple abstraction over {@link java.util.concurrent.locks.ReentrantLock} that can be used when
 * we need to be dealing with a dictionary of lockable objects wherein we traditionally would have
 * used the <code>synchronized</code> keyword.
 */
public final class KeyAwareAutoCloseableLock {
  private final Map<Object, AutoCloseableLock> internalMap = new ConcurrentHashMap<>();

  public AutoReleasable lockForObject(Object key) {
    AutoCloseableLock internal =
        internalMap.computeIfAbsent(Objects.requireNonNull(key), k -> new AutoCloseableLock());
    return new AutoReleasable(internal.lock(), () -> internalMap.remove(key));
  }

  public static class AutoReleasable implements AutoCloseable {

    private final AutoCloseableLock lock;
    private final Runnable cleanupAction;

    AutoReleasable(AutoCloseableLock lock, Runnable cleanupAction) {
      this.lock = Objects.requireNonNull(lock);
      this.cleanupAction =
          this.lock.isHeldByCurrentThread() ? () -> {} : Objects.requireNonNull(cleanupAction);
    }

    @Override
    public void close() {
      lock.close();
      cleanupAction.run();
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      AutoReleasable that = (AutoReleasable) object;
      return Objects.equals(lock, that.lock);
    }

    @Override
    public int hashCode() {
      return Objects.hash(lock);
    }
  }
}
