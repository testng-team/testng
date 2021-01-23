package test.sample;


import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * @author Cedric Beust, Apr 30, 2004
 */
public class BaseSampleInheritance {

  protected List<String> m_configurations = new ArrayList<>();
  protected boolean m_invokedBaseMethod = false;
  protected boolean m_invokedBaseConfiguration = false;

  protected void addConfiguration(String c) {
    m_configurations.add(c);
  }

  @Test(groups = {"inheritedTestMethod"})
  public void baseMethod() {
    m_invokedBaseMethod = true;
  }

  @BeforeClass
  public void baseConfiguration() {
    m_invokedBaseConfiguration = true;
  }

  @BeforeClass(groups = {"configuration1"},
      dependsOnGroups = {"configuration0"})
  public void configuration1() {
//    System.out.println("CONFIGURATION 1");
    addConfiguration("configuration1");
  }

  @Test(dependsOnGroups = {"inheritedTestMethod"})
  public void testBooleans() {
    assert m_invokedBaseMethod : "Didn't invoke test method in base class";
    assert m_invokedBaseConfiguration : "Didn't invoke configuration method in base class";
  }

}
