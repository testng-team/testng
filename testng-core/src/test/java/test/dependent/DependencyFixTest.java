package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class DependencyFixTest {
  @Test(dependsOnMethods = "helloWorld", ignoreMissingDependencies = true)
  public void dependentOnNonExistingMethod() {
    assertThat(true).isTrue();
  }

  @Test(dependsOnMethods = "dependentOnNonExistingMethod")
  public void dependentOnExistingMethod() {
    assertThat(true).isTrue();
  }

  @Test(
      groups = "selfSufficient",
      dependsOnGroups = "nonExistingGroup",
      ignoreMissingDependencies = true)
  public void dependentOnNonExistingGroup() {
    assertThat(true).isTrue();
  }
}
