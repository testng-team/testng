package org.testng.internal.collections;

import java.util.Iterator;
import org.jspecify.annotations.Nullable;

/**
 * An {@link Iterator} that may own a resource (such as a {@link java.util.stream.Stream}) which
 * must be released once iteration is finished. Callers that drive the iterator are responsible for
 * invoking {@link #close()} when they are done consuming it - whether the iterator was fully
 * drained or not.
 *
 * <p>{@link #close()} never throws a checked exception, is idempotent and is a no-op for iterators
 * that own no resource.
 */
public interface CloseableIterator<T extends @Nullable Object> extends Iterator<T>, AutoCloseable {

  @Override
  void close();
}
