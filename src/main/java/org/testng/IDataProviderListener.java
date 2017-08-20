package org.testng;

/**
 * A listener that gets invoked before and after a data provider is invoked by TestNG.
 */
public interface IDataProviderListener extends ITestNGListener {

    /**
     * This method gets invoked just before a data provider is invoked.
     *
     * @param dataProviderInformation - A {@link DataProviderInformation} object that contains details about the
     *                                data provider that is about to be executed.
     * @param method                  - The {@link ITestNGMethod} method that is going to consume the data
 *                                provided by the data provider.
     */
    void beforeDataProviderExecution(DataProviderInformation dataProviderInformation, ITestNGMethod method);

    /**
     * This method gets invoked just after a data provider is invoked.
     * @param dataProviderInformation - A {@link DataProviderInformation} object that contains details about the
     * @param method             - The {@link ITestNGMethod} method that received the data
 *                           provided by the data provider.
     */
    void afterDataProviderExecution(DataProviderInformation dataProviderInformation, ITestNGMethod method);
}
