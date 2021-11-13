package test.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * Simple afterGroups test
 *
 * @author cbeust
 * @date Mar 7, 2006
 */
public class ConfigurationGroups4SampleTest {

  private boolean m_after = false;
  private boolean m_run = false;

  @Test
  public void f() {}

  @Test(groups = "cg4-1")
  public void run() {
    Assert.assertFalse(m_after);
    m_run = true;
  }

  @AfterGroups("cg4-1")
  public void after1() {
    Assert.assertTrue(m_run);
    Assert.assertFalse(m_after);
    m_after = true;
  }

  @Test(dependsOnGroups = "cg4-1")
  public void verify() {
    Assert.assertTrue(m_run, "run() wasn't run");
    Assert.assertTrue(m_after, "after1() wasn't run");
  }
}
