package test.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * afterGroups test when the group contains more than one method
 *
 * @author cbeust
 */
public class ConfigurationGroups6SampleTest {

  private boolean m_after = false;
  private boolean m_run1 = false;
  private boolean m_run2 = false;

  @Test
  public void f() {}

  @Test(groups = "cg6-1")
  public void run1() {
    Assert.assertFalse(m_after);
    m_run1 = true;
  }

  @Test(groups = "cg6-1")
  public void run2() {
    Assert.assertFalse(m_after);
    m_run2 = true;
  }

  @AfterGroups("cg6-1")
  public void after() {
    Assert.assertTrue(m_run1);
    Assert.assertTrue(m_run2);
    Assert.assertFalse(m_after);
    m_after = true;
  }

  @Test(dependsOnGroups = {"cg6-1"})
  public void verify() {
    Assert.assertTrue(m_run1, "run1() wasn't run");
    Assert.assertTrue(m_run2, "run2() wasn't run");
    Assert.assertTrue(m_after, "after1() wasn't run");
  }
}
