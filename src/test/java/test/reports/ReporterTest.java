package test.reports;

import org.testng.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Set;

@Listeners(ReporterTest.class)
public class ReporterTest extends TestListenerAdapter {
    @Override public void onStart (ITestContext testContext) {
        Reporter.log ("foo");
    }
    @Test
    public void testMethod() {
        Reporter.log ("bar"); // This line is required. Else the log that was triggered from onStart() would never be
        // persisted at all.
        Assert.assertTrue (Reporter.getOutput ().size () == 2);
    }
}
