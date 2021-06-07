package test.reports;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(UniqueReporterInjectionTest.ReporterListenerForIssue1227.class)
public class UniqueReporterInjectionSample2 {
  @Test
  public void testMethod() {
    Assert.assertTrue(true);
  }
}
