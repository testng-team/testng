package test.timeout;

import org.testng.annotations.Test;

public class TestTimeOutSampleTest {

    @Test
    public void timeoutTest() {
        try {
            Thread.sleep(2_000);
        } catch (InterruptedException handled) {
            Thread.currentThread().interrupt();
        }
    }
}
