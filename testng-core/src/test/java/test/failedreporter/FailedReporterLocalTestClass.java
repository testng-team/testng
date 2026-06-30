package test.failedreporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

/**
 * A simple test class that is used by {@link FailedReporterScenariosTest} class to test out the
 * presence/absence of testng-failed.xml file when there are failures/no failures scenarios.
 */
public class FailedReporterLocalTestClass {

  public static class WithFailure {
    @Test
    public void testMethodWithFailure() {
      fail();
    }
  }

  public static class WithoutFailure {
    @Test
    public void testMethodWithoutFailure() {
      assertThat(true).isTrue();
    }
  }
}
