package test.configuration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BeforeClassThreadA {
    public static long WHEN;

    @BeforeClass(alwaysRun = true)
    public void setup() throws InterruptedException {
        WHEN = System.currentTimeMillis();
        Thread.sleep(2000);
    }

    @Test
    public void execute() {
    }
}
