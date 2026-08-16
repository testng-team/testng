package org.testng.internal.collections;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;
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
  private final @Nullable AutoCloseable resource;
  private boolean closed;

  /**
   * @param delegate the iterator that actually produces the elements.
   * @param resource the resource to release on {@link #close()}, or {@code null} if there is
   *     nothing to release.
   */
  public ResourceAwareIterator(Iterator<T> delegate, @Nullable AutoCloseable resource) {
    this.delegate = delegate;
    this.resource = resource;
  }

  /**
   * Builds a {@link CloseableIterator} of {@code Object[]} rows from the iterator produced by a
   * lazily-loaded data provider (either an {@code Iterator} or a {@code Stream}). The declared
   * generic return type is inspected to decide whether each element is already an {@code Object[]}
   * row or a single value that needs to be wrapped into one.
   *
   * @param iterator the iterator produced by the data provider.
   * @param returnType the declared generic return type of the data provider method.
   * @param resource the resource to release on {@link #close()} (typically the {@code Stream} the
   *     iterator was derived from), or {@code null} if there is nothing to release.
   */
  public static CloseableIterator<Object[]> forDataProvider(
      Iterator<Object> iterator, Type returnType, @Nullable AutoCloseable resource) {
    return new ResourceAwareIterator<>(toObjectArrayIterator(iterator, returnType), resource);
  }

  @SuppressWarnings("unchecked")
  private static Iterator<Object[]> toObjectArrayIterator(
      Iterator<Object> iterator, Type returnType) {
    if (!(returnType instanceof ParameterizedType)) {
      // Raw Iterator/Stream, we expect the user to provide rows of the expected shape.
      return (Iterator<Object[]>) (Iterator<?>) iterator;
    }

    // Inspect only the direct element type of the Iterator/Stream. Each element is treated as an
    // already-formed Object[] row only when that element type is itself a reference (non-primitive)
    // array; otherwise every element is wrapped into a single-element row. This keeps, for example,
    // Stream<List<Object[]>> being delivered as one List<Object[]> parameter rather than being
    // mistaken for a row of Object[].
    Type elementType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
    if (isObjectArrayRow(elementType)) {
      return (Iterator<Object[]>) (Iterator<?>) iterator;
    }
    return new OneToTwoDimIterator(iterator);
  }

  private static boolean isObjectArrayRow(Type elementType) {
    if (elementType instanceof Class) {
      Class<?> clazz = (Class<?>) elementType;
      return clazz.isArray() && !clazz.getComponentType().isPrimitive();
    }
    // A generic array type such as T[] is also a reference-array row.
    return elementType instanceof GenericArrayType;
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
