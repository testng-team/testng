package test.listeners.issue3064;

import static org.testng.Assert.fail;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Test
@Listeners(EvidenceListener.class)
public class SampleTestCase {

  @BeforeSuite(alwaysRun = true)
  public void suiteSetup() {
    ITestContext context = Reporter.getCurrentTestResult().getTestContext();
    for (ITestNGMethod method : context.getAllTestMethods()) {
      method.setRetryAnalyzerClass(EvidenceRetryAnalyzer.class);
    }
  }

  @Test
  public void testOne() {
    fail();
  }
}
