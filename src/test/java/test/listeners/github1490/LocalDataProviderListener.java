package test.listeners.github1490;

import org.testng.DataProviderInformation;
import org.testng.IDataProviderListener;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;

import java.util.List;

public class LocalDataProviderListener implements IDataProviderListener {
    public static List<String> messages = Lists.newArrayList();

    @Override
    public void beforeDataProviderExecution(DataProviderInformation dataProviderInformation, ITestNGMethod method) {
        log(method, "before:");
    }

    @Override
    public void afterDataProviderExecution(DataProviderInformation dataProviderInformation, ITestNGMethod method) {
        log(method, "after:");
    }

    private void log(ITestNGMethod method, String prefix) {
        if (method.getInstance() != null) {
            messages.add(prefix + method.getInstance().getClass().getName() + "." + method.getMethodName());
        } else {
            messages.add(prefix + method.getMethodName());
        }
    }
}
