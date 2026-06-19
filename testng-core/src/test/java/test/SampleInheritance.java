package test;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.sample.BaseSampleInheritance;

public class SampleInheritance extends BaseSampleInheritance {

  // Test dependency of configuration methods
  @BeforeClass(groups = {"configuration0"})
  public void configuration0() {
    addConfiguration("configuration0");
    //    System.out.println("CONFIGURATION 0");
  }

  @BeforeClass(
      groups = "final",
      dependsOnGroups = {"configuration1"})
  public void configuration2() {
    assertThat(m_configurations.size())
        .withFailMessage("Expected size 2 found " + m_configurations.size())
        .isEqualTo(2);
    assertThat(m_configurations.get(0))
        .withFailMessage("Expected configuration0 to be run")
        .isEqualTo("configuration0");
    assertThat(m_configurations.get(1))
        .withFailMessage("Expected configuration1 to be run")
        .isEqualTo("configuration1");
    addConfiguration("configuration2");
  }

  @Test(
      groups = "final",
      dependsOnGroups = {"inheritedTestMethod"})
  public void inheritedMethodsWereCalledInOrder() {
    assertThat(m_invokedBaseMethod)
        .withFailMessage("Didn't invoke test method in base class")
        .isTrue();
    assertThat(m_invokedBaseConfiguration)
        .withFailMessage("Didn't invoke configuration method in base class")
        .isTrue();
  }

  @Test(groups = "final2", dependsOnGroups = "final")
  public void configurationsWereCalledInOrder() {
    assertThat(m_configurations.size()).isEqualTo(3);
    assertThat(m_configurations.get(0))
        .withFailMessage("Expected configuration0 to be run")
        .isEqualTo("configuration0");
    assertThat(m_configurations.get(1))
        .withFailMessage("Expected configuration1 to be run")
        .isEqualTo("configuration1");
    assertThat(m_configurations.get(2))
        .withFailMessage("Expected configuration1 to be run")
        .isEqualTo("configuration2");
  }
}
