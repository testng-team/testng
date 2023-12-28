package test.configuration;

import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * beforeGroups test: make sure that if before methods are scattered on more than one class, they
 * are still taken into account
 *
 * @author cbeust
 */
public class ConfigurationGroups3SampleTest extends Base3 {
  private boolean m_before = false;
  private static boolean m_f1 = false;

  @BeforeGroups("cg34-1")
  public void before1() {
    Assert.assertFalse(m_before);
    Assert.assertFalse(m_f1);
    m_before = true;
  }

  @Test(groups = "cg34-a")
  public void fa() {}

  @Test(groups = "cg34-1")
  public void f1() {
    Assert.assertTrue(m_before);
    Assert.assertTrue(Base3.getBefore());
    m_f1 = true;
  }

  @Test(dependsOnGroups = {"cg34-a", "cg34-1"})
  public void verify() {
    Assert.assertTrue(m_before);
    Assert.assertTrue(Base3.getBefore());
    Assert.assertTrue(m_f1);
  }

  public static boolean getF1() {
    return m_f1;
  }
}
