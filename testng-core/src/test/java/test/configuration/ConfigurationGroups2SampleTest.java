package test.configuration;

import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * beforeGroups test: make sure that the beforeGroups method is invoked only once even if two test
 * methods belong to the group
 *
 * @author cbeust
 */
public class ConfigurationGroups2SampleTest {
  private boolean m_before = false;
  private boolean m_f1 = false;
  private boolean m_g1 = false;

  @BeforeGroups("cg2-1")
  public void before1() {
    Assert.assertFalse(m_before);
    Assert.assertFalse(m_f1);
    Assert.assertFalse(m_g1);
    m_before = true;
  }

  @Test(groups = "cg2-a")
  public void fa() {}

  @Test(groups = "cg2-1")
  public void f1() {
    Assert.assertTrue(m_before);
    m_f1 = true;
  }

  @Test(groups = "cg2-1")
  public void g1() {
    Assert.assertTrue(m_before);
    m_g1 = true;
  }

  @Test(dependsOnGroups = {"cg2-a", "cg2-1"})
  public void verify() {
    Assert.assertTrue(m_before);
    Assert.assertTrue(m_f1);
    Assert.assertTrue(m_g1);
  }
}
