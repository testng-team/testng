package org.testng.annotations;

import java.util.List;
import org.testng.IRetryDataProvider;

/** Encapsulate the @DataProvider / @testng.data-provider annotation */
public interface IDataProviderAnnotation extends IAnnotation {
  /** @return The name of this DataProvider. */
  String getName();

  void setName(String name);

  /**
   * Whether this data provider should be used in parallel.
   *
   * @return true if in parallel
   */
  boolean isParallel();

  void setParallel(boolean parallel);

  List<Integer> getIndices();

  void setIndices(List<Integer> indices);

  /** Have TestNG consider failures in data provider methods as test failures. */
  void propagateFailureAsTestFailure();

  /** @return - <code>true</code>If data provider failures should be propagated as test failures */
  boolean isPropagateFailureAsTestFailure();

  /**
   * @param retry - A Class that implements {@link IRetryDataProvider} and which can be used to
   *     retry a data provider.
   */
  void setRetryUsing(Class<? extends IRetryDataProvider> retry);

  /**
   * @return - An Class which implements {@link IRetryDataProvider} and which can be used to retry a
   *     data provider.
   */
  Class<? extends IRetryDataProvider> retryUsing();

  /**
   * @param cache - when set to <code>true</code>, TestNG does not invoke the data provider again
   *     when retrying failed tests using a retry analyzer.
   */
  void cacheDataForTestRetries(boolean cache);

  /**
   * @return - <code>true</code> if TestNG should use data returned by the original data provider
   *     invocation, when a test method fails and is configured to be retried.
   */
  boolean isCacheDataForTestRetries();
}
