package org.testng;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class DryRunTest extends SimpleBaseTest {

    @Test
    public void testDryRun() {
        System.setProperty("testng.mode.dryrun", "true");
        try {
            TestNG tng = create(DryRunSample.class);
            TestListenerAdapter listener = new TestListenerAdapter();
            tng.addListener((ITestNGListener) listener);
            tng.run();
            assertThat(listener.getPassedTests()).hasSize(2);
        } finally {
            System.setProperty("testng.mode.dryrun", "false");
        }
    }

}
