package org.testng;

import java.lang.reflect.Method;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Represents the attributes of a {@link org.testng.annotations.DataProvider} annotated method. */
public interface IDataProviderMethod {
  /**
   * @return - The instance to which the data provider belongs to. <code>null</code> if the data
   *     provider is a static one.
   */
  @Nullable
  Object getInstance();

  /**
   * @return - A {@link Method} object that represents the actual {@literal @}{@link
   *     org.testng.annotations.DataProvider} method, or {@code null} once TestNG has released it --
   *     which it does as soon as the data provider has yielded its rows, so that the method and its
   *     instance do not outlive the run.
   */
  @Nullable
  Method getMethod();

  /** @return The name of this DataProvider. */
  String getName();

  /** @return Whether this data provider should be run in parallel. */
  boolean isParallel();

  /** @return Which indices to run from this data provider, default: all. */
  List<Integer> getIndices();

  /** @return Whether failures in data providers should be treated as test failures */
  default boolean propagateFailureAsTestFailure() {
    return false;
  }

  /**
   * @return - An Class which implements {@link IRetryDataProvider} and which can be used to retry a
   *     data provider.
   */
  default Class<? extends IRetryDataProvider> retryUsing() {
    return IRetryDataProvider.DisableDataProviderRetries.class;
  }

  /**
   * @return - <code>true</code> if TestNG should use data returned by the original data provider
   *     invocation, when a test method fails and is configured to be retried.
   */
  default boolean cacheDataForTestRetries() {
    return true;
  }
}
