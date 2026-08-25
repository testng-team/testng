package org.testng;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link TestNG#runSuitesLocally()}, which is public because Maven Surefire calls
 * it.
 */
public class TestNGRunSuitesLocallyTest {

  /**
   * The "nothing to run" branch used to answer {@link java.util.Collections#emptyList()} while
   * every other branch answered an {@code ArrayList}, so what a caller could do with the answer
   * depended on whether a suite had been found. This prints a usage banner, which is what the
   * branch does; it neither throws nor exits.
   */
  @Test
  public void runningNoSuitesAnswersAMutableList() {
    List<ISuite> suites = new TestNG().runSuitesLocally();

    assertThat(suites).isEmpty();
    suites.add(null);
    assertThat(suites).hasSize(1);
  }
}
