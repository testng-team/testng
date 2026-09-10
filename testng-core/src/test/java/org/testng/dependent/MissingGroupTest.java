package org.testng.dependent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.TestNGException;
import org.testng.annotations.Test;
import test.BaseTest;

public class MissingGroupTest extends BaseTest {

  @Test
  public void verifyThatExceptionIsThrownIfMissingGroup() {
    addClass("org.testng.dependent.samples.MissingGroupSampleTest");

    // shouldBeSkipped depends on a group no class declares, and does not set
    // ignoreMissingDependencies. TestNG refuses the run rather than skipping the method.
    assertThatThrownBy(this::run)
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("shouldBeSkipped")
        .hasMessageContaining("nonexistent group \"missing-group\"");
  }
}
