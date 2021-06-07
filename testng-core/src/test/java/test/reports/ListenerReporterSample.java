package test.reports;

import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ListenerReporterSample.class)
public class ListenerReporterSample extends TestListenerAdapter {

  @Override
  public void onStart(ITestContext testContext) {
    Reporter.log("foo");
    super.onStart(testContext);
  }

  @Test
  public void testMethod() {
    Reporter.log(
        "bar"); // This line is required. Else the log that was triggered from onStart() would never
    // be
    // persisted at all.
  }
}
