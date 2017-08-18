package test.github1490;

import org.testng.DataProviderInformation;
import org.testng.IDataProviderListener;
import org.testng.ITestNGMethod;

public class DataProviderInfoProvider implements IDataProviderListener {
    public static DataProviderInformation before;
    public static DataProviderInformation after;

    @Override
    public void beforeDataProviderExecution(ITestNGMethod method, DataProviderInformation dataProviderInformation) {
        before = dataProviderInformation;
    }

    @Override
    public void afterDataProviderExecution(ITestNGMethod method, DataProviderInformation dataProviderInformation) {
        after = dataProviderInformation;
    }
}
