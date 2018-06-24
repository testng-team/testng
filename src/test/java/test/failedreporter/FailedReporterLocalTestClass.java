package test.failedreporter;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A simple test class that is used by {@link FailedReporterScenariosTest} class to test out the
 * presence/absence of testng-failed.xml file when there are failures/no failures scenarios.
 */
public class FailedReporterLocalTestClass {

  public static class WithFailure {
    @Test
    public void testMethodWithFailure() {
      Assert.fail();
    }
  }

  public static class WithoutFailure {
    @Test
    public void testMethodWithoutFailure() {
      Assert.assertTrue(true);
    }
  }
}
