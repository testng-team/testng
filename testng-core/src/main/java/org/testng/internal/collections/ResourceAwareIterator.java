package org.testng.internal.collections;

import java.util.Iterator;
import org.testng.internal.Utils;

/**
 * A {@link CloseableIterator} that delegates iteration to another {@link Iterator} and, on {@link
 * #close()}, releases an optional resource (for example the {@link java.util.stream.Stream} the
 * delegate was derived from). When no resource is supplied, {@link #close()} is a no-op.
 *
 * <p>The iterator is consumed lazily; the backing resource is only released when {@link #close()}
 * is called, so a data provider that returns a resource-backed {@code Stream} keeps that resource
 * open for exactly as long as the rows are being pulled.
 */
public class ResourceAwareIterator<T> implements CloseableIterator<T> {

  private final Iterator<T> delegate;
  private final AutoCloseable resource;
  private boolean closed;

  /**
   * @param delegate the iterator that actually produces the elements.
   * @param resource the resource to release on {@link #close()}, or {@code null} if there is
   *     nothing to release.
   */
  public ResourceAwareIterator(Iterator<T> delegate, AutoCloseable resource) {
    this.delegate = delegate;
    this.resource = resource;
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  @Override
  public T next() {
    return delegate.next();
  }

  @Override
  public void remove() {
    delegate.remove();
  }

  @Override
  public void close() {
    if (closed || resource == null) {
      return;
    }
    closed = true;
    try {
      resource.close();
    } catch (Exception e) {
      // Closing happens during clean-up, after the data has already been consumed, so a failure
      // here must never mask the outcome of the test(s) that used this data provider.
      Utils.warn("Failed to close the resource backing a data provider: " + e);
    }
  }
}
