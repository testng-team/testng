package org.testng.dependent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.BaseTest;

public class MissingMethodTest extends BaseTest {

  @Test
  public void verifyThatExceptionIsThrownIfMissingMethod() {
    addClass("org.testng.dependent.samples.MissingMethodSampleTest");

    // alwaysRunDespiteMissingMethod names a method that does not exist and sets alwaysRun.
    // alwaysRun says to run when a dependency fails. It does not excuse a dependency that was
    // never there, so TestNG refuses the run. Only ignoreMissingDependencies does that.
    assertThatThrownBy(this::run)
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("alwaysRunDespiteMissingMethod")
        .hasMessageContaining("nonexistent method \"missingMethod\"");
  }
}
