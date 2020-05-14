package test.timeout.github1493;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestClassSample {
    @Test(timeOut = 1000)
    public void testMethod() throws Exception {
        TimeUnit.SECONDS.sleep(2);
    }
}

