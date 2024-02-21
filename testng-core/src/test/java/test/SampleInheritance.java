package test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
    assertEquals(m_configurations.size(), 2, "Expected size 2 found " + m_configurations.size());
    assertEquals(m_configurations.get(0), "configuration0", "Expected configuration0 to be run");
    assertEquals(m_configurations.get(1), "configuration1", "Expected configuration1 to be run");
    addConfiguration("configuration2");
  }

  @Test(
      groups = "final",
      dependsOnGroups = {"inheritedTestMethod"})
  public void inheritedMethodsWereCalledInOrder() {
    assertTrue(m_invokedBaseMethod, "Didn't invoke test method in base class");
    assertTrue(m_invokedBaseConfiguration, "Didn't invoke configuration method in base class");
  }

  @Test(groups = "final2", dependsOnGroups = "final")
  public void configurationsWereCalledInOrder() {
    assertEquals(m_configurations.size(), 3);
    assertEquals(m_configurations.get(0), "configuration0", "Expected configuration0 to be run");
    assertEquals(m_configurations.get(1), "configuration1", "Expected configuration1 to be run");
    assertEquals(m_configurations.get(2), "configuration2", "Expected configuration1 to be run");
  }
}
