package test.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * afterGroups test with more than one group
 *
 * @author cbeust
 * @date Mar 7, 2006
 */
public class ConfigurationGroups5SampleTest {

  private boolean m_after = false;
  private boolean m_run1 = false;
  private boolean m_run2 = false;
  private int m_afterCount = 0;

  @Test
  public void f() {
    log("f");
  }

  @Test(groups = "cg5-1")
  public void run1() {
    log("run1");
    if (m_afterCount == 0) {
      Assert.assertFalse(m_after);
    }
    m_run1 = true;
  }

  @Test(groups = "cg5-2")
  public void run2() {
    log("run2");
    if (m_afterCount == 0) {
      Assert.assertFalse(m_after);
    }
    m_run2 = true;
  }

  @AfterGroups({ "cg5-1", "cg5-2"})
  public void after() {
    log("after");
    m_afterCount++;
    Assert.assertTrue(m_run1 || m_run2);
    if (m_afterCount == 0) {
      Assert.assertFalse(m_after);
    }
    m_after = true;
  }

  @Test(dependsOnGroups = { "cg5-1", "cg5-2" })
  public void verify() {
    log("verify");
    Assert.assertTrue(m_run1, "run1() wasn't run");
    Assert.assertTrue(m_run2, "run2() wasn't run");
    Assert.assertTrue(m_after, "after1() wasn't run");
    Assert.assertEquals(2, m_afterCount);
  }

  private void log(String string) {
    ppp(string);
  }

  private void ppp(String s) {
    if (false) {
      System.out.println("[ConfigurationGroups5SampleTest] " + s);
    }
  }

}
