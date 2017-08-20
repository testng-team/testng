package test.listeners.github1490;

import org.testng.DataProviderInformation;
import org.testng.IDataProviderListener;
import org.testng.ITestNGMethod;

public class DataProviderInfoProvider implements IDataProviderListener {
    public static DataProviderInformation before;
    public static DataProviderInformation after;

    @Override
    public void beforeDataProviderExecution(DataProviderInformation dataProviderInformation, ITestNGMethod method) {
        before = dataProviderInformation;
    }

    @Override
    public void afterDataProviderExecution(DataProviderInformation dataProviderInformation, ITestNGMethod method) {
        after = dataProviderInformation;
    }
}
