package org.testng.internal.invokers;

import java.util.Iterator;
import org.jspecify.annotations.Nullable;
import org.testng.IDataProviderMethod;
import org.testng.internal.collections.CloseableIterator;

/**
 * A simple holder for parameters that contains the parameters and where these came from (data
 * provider or testng.xml)
 */
public class ParameterHolder implements AutoCloseable {
  /** Origin of the parameters. */
  public enum ParameterOrigin {
    ORIGIN_DATA_PROVIDER, // A data provider
    ORIGIN_XML, // TestNG XML suite
    NATIVE // Native injection is involved.
  }

  final IDataProviderMethod dataProviderHolder;
  public final Iterator<Object[]> parameters;
  final ParameterOrigin origin;

  /**
   * The (optional) closeable data provider source that {@link #parameters} was derived from. It is
   * kept separately from {@link #parameters} - which may have been wrapped by {@code
   * FilteredParameters} and by user supplied {@code IDataProviderInterceptor}s - so that the
   * original resource is released regardless of how the exposed iterator was wrapped or how much of
   * it was consumed.
   */
  private final @Nullable CloseableIterator<Object[]> closeableSource;

  public ParameterHolder(
      Iterator<Object[]> parameters, ParameterOrigin origin, IDataProviderMethod dph) {
    this(parameters, origin, dph, null);
  }

  public ParameterHolder(
      Iterator<Object[]> parameters,
      ParameterOrigin origin,
      IDataProviderMethod dph,
      @Nullable CloseableIterator<Object[]> closeableSource) {
    super();
    this.parameters = parameters;
    this.origin = origin;
    this.dataProviderHolder = dph;
    this.closeableSource = closeableSource;
  }

  /**
   * Releases the resource backing a lazily-loaded data provider (for example a {@code Stream}), if
   * any. Safe to call more than once and a no-op when there is nothing to release. Never throws.
   */
  @Override
  public void close() {
    if (closeableSource != null) {
      closeableSource.close();
    }
  }
}
