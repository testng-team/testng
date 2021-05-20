package test.retryAnalyzer.dataprovider;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static org.testng.Assert.assertEquals;

public class RetryAnalyzerWithComplexDataProviderTest extends SimpleBaseTest {

    @Test(description = "GITHUB-2148")
    public void testWithoutDataProvider() {
        TestNG testng = create(RetryAnalyzerWithoutDataProvider.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener(tla);
        testng.run();
        assertEquals(tla.getPassedTests().size(), 1);
        assertEquals(tla.getSkippedTests().size(), 3);
    }

    @Test(description = "GITHUB-2148")
    public void testWithDataProviderStringArray() {
        TestNG testng = create(DataProviderWithStringArraySample.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener(tla);
        testng.run();
        assertEquals(tla.getPassedTests().size(), 1);
        assertEquals(tla.getSkippedTests().size(), 3);
    }

    @Test(description = "GITHUB-2148")
    public void testWithSingleParam() {
        TestNG testng = create(DataProviderWithSingleParam.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener(tla);
        testng.run();
        assertEquals(tla.getPassedTests().size(), 2);
        assertEquals(tla.getSkippedTests().size(), 3);
    }

    @Test(description = "GITHUB-2148")
    public void testWithDataProviderWithObjectAndArraySample() {
        TestNG testng = create(ComplexDataProviderWithObjectAndArraySample.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener(tla);
        testng.run();
        assertEquals(tla.getPassedTests().size(), 1);
        assertEquals(tla.getSkippedTests().size(), 3);
    }

    @Test(description = "GITHUB-2148")
    public void testDataProviderWithRetryAttemptsFailure() {
        TestNG testng = create(DataProviderWithRetryAttemptsFailure.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener(tla);
        testng.run();
        assertEquals(tla.getFailedTests().size(), 1);
        assertEquals(tla.getSkippedTests().size(), 3);
    }
}
