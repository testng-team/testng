package test.timeout;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class TimeOutIntegrationTest {

    @Test(description = "https://github.com/cbeust/testng/issues/811")
    public void testTimeOutWhenParallelIsTest() {
        TestNG tng = new TestNG();
        tng.setParallel(XmlSuite.ParallelMode.TESTS);
        tng.setTestClasses(new Class[]{TimeOutWithParallelSample.class});

        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener(tla);

        tng.run();

        Assert.assertEquals(tla.getFailedTests().size(), 1);
        Assert.assertEquals(tla.getSkippedTests().size(), 0);
        Assert.assertEquals(tla.getPassedTests().size(), 0);
    }
}
