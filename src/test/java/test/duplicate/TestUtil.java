package test.duplicate;

import java.util.concurrent.TimeUnit;

/**
 * Created by EasonYi
 */
public class TestUtil {
    public static void sleepSomeMilliSeconds(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
