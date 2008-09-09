package test.timeout;

import org.testng.annotations.Test;

public class TestTimeOutSampleTest {

    @Test
    public void sleepsForFiveSeconds() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
//            System.out.println("Interrupted exception");
        }
//        System.out.println("Finished normally");
    }
}
