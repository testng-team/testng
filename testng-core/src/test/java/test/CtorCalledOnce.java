package test;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

/**
 * this test verifys that the test class is instantiated exactly once regardless of how many test
 * methods we have, showing that TestNG semantics is quite different from JUnit
 */
public class CtorCalledOnce {
  public static int instantiated = 0;

  public CtorCalledOnce() {
    instantiated++;
  }

  @Test
  public void testMethod1() {
    assertThat(instantiated)
        .withFailMessage("Expected 1, was invoked " + instantiated + " times")
        .isEqualTo(1);
  }

  @Test
  public void testMethod2() {
    assertThat(instantiated)
        .withFailMessage("Expected 1, was invoked " + instantiated + " times")
        .isEqualTo(1);
  }

  @Test
  public void testMethod3() {
    assertThat(instantiated)
        .withFailMessage("Expected 1, was invoked " + instantiated + " times")
        .isEqualTo(1);
  }

  @AfterTest
  public void afterTest() {
    instantiated = 0;
  }
}
