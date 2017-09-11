package test.suites.github1533;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.collections.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SuiteCounter implements ISuiteListener {
    private final AtomicInteger counter = new AtomicInteger(0);
    private final List<String> suiteNames = Lists.newArrayList();

    @Override
    public void onStart(ISuite suite) {
        counter.incrementAndGet();
        suiteNames.add(suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
    }

    public int getCounter() {
        return counter.get();
    }

    public List<String> getSuiteNames() {
        return suiteNames;
    }
}
