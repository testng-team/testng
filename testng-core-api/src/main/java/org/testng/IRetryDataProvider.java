package org.testng;

/** Represents the ability to retry a data provider. */
public interface IRetryDataProvider {

  /**
   * @param dataProvider - The {@link IDataProviderMethod} object which represents the data provider
   *     to be invoked.
   * @return - <code>true</code> if the data provider should be invoked again.
   */
  boolean retry(IDataProviderMethod dataProvider);

  /** A dummy implementation which disables retrying of a failed data provider. */
  class DisableDataProviderRetries implements IRetryDataProvider {

    @Override
    public boolean retry(IDataProviderMethod dataProvider) {
      return false;
    }
  }
}
