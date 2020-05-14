package test.timeout;

import org.testng.annotations.Test;

public class TimeOutWithParallelSample {

    @Test(timeOut = 1_000)
    public void myTestMethod() throws InterruptedException {
        Thread.sleep(1_500);
    }
}
