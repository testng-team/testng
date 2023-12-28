package test.configuration;

import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Simple beforeGroups test: 1 before method and 2 test method
 *
 * @author cbeust
 */
public class ConfigurationGroups1SampleTest {
  private boolean m_before = false;
  private boolean m_f1 = false;

  @BeforeGroups("cg1-1")
  public void before1() {
    Assert.assertFalse(m_before);
    Assert.assertFalse(m_f1);
    m_before = true;
  }

  @Test(groups = "cg1-a")
  public void fa() {}

  @Test(groups = "cg1-1")
  public void f1() {
    Assert.assertTrue(m_before);
    m_f1 = true;
  }

  @Test(dependsOnGroups = {"cg1-a", "cg1-1"})
  public void verify() {
    Assert.assertTrue(m_before);
    Assert.assertTrue(m_f1);
  }
}
