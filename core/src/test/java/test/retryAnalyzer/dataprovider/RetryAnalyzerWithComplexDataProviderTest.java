package test.retryAnalyzer.dataprovider;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static org.testng.Assert.assertEquals;

public class RetryAnalyzerWithComplexDataProviderTest extends SimpleBaseTest {

    @Test(description = "GITHUB-2148")
    public void ensureTestsAreRetried() {
        TestNG testng = create(ComplexDataProviderSample.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        testng.addListener(tla);
        testng.run();
        assertEquals(tla.getPassedTests().size(), 5);
        assertEquals(tla.getFailedTests().size(), 1);
        assertEquals(tla.getSkippedTests().size(), 15);
    }
}
