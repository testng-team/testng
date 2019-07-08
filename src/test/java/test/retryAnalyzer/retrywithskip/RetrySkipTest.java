package test.retryAnalyzer.retrywithskip;

import static org.testng.Assert.assertEquals;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class RetrySkipTest extends SimpleBaseTest {
    
    @Test
    public void test1() {
        TestNG testng = create(RerunTest1.class);
        testng.addListener(new AddReryWithTransformer());
        TestListenerAdapter adapter  = new TestListenerAdapter();
        testng.addListener(adapter);
        testng.run();
        assertEquals(
                adapter.getRetriedTests().size(),
                1,
                "wrong number of retried test case");
    }
    
    @Test
    public void test2() {
        TestNG testng = create(RerunTest2.class);
        TestListenerAdapter adapter  = new TestListenerAdapter();
        testng.addListener(adapter);
        testng.run();        
        assertEquals(
                adapter.getRetriedTests().size(),
                1,
                "wrong number of retried test case");
    }
}
