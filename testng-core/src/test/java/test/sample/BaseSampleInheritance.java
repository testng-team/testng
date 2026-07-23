package test.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Cedric Beust, Apr 30, 2004 */
public class BaseSampleInheritance {

  protected List<String> m_configurations = new ArrayList<>();

  protected void addConfiguration(String c) {
    m_configurations.add(c);
  }

  protected boolean m_invokedBaseMethod = false;

  @Test(groups = {"inheritedTestMethod"})
  public void baseMethod() {
    m_invokedBaseMethod = true;
  }

  protected boolean m_invokedBaseConfiguration = false;

  @BeforeClass(alwaysRun = true)
  public void baseConfiguration() {
    m_invokedBaseConfiguration = true;
  }

  @BeforeClass(
      groups = {"configuration1"},
      dependsOnGroups = {"configuration0"})
  public void configuration1() {
    //    System.out.println("CONFIGURATION 1");
    addConfiguration("configuration1");
  }

  @Test(dependsOnGroups = {"inheritedTestMethod"})
  public void testBooleans() {
    assertThat(m_invokedBaseMethod)
        .withFailMessage("Didn't invoke test method in base class")
        .isTrue();
    assertThat(m_invokedBaseConfiguration)
        .withFailMessage("Didn't invoke configuration method in base class")
        .isTrue();
  }
}
