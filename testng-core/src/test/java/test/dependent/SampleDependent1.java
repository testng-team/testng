package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * This class exercises dependent groups
 *
 * @author Cedric Beust, Aug 19, 2004
 */
public class SampleDependent1 {

  @Test(groups = {"fail"})
  public void fail() {
    assertThat(false).isTrue();
  }

  @Test(dependsOnGroups = {"fail"})
  public void shouldBeSkipped() {}
}
