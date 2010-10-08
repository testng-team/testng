package test.configuration;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

/**
 * afterGroups test when the group contains more than one method
 *
 * @author cbeust
 * @date Mar 7, 2006
 */
public class ConfigurationGroups6SampleTest {

  private boolean m_after = false;
  private boolean m_run1 = false;
  private boolean m_run2 = false;

  @Test
  public void f() {
    log("f");
  }

  @Test(groups = "cg6-1")
  public void run1() {
    log("run1");
    Assert.assertFalse(m_after);
    m_run1 = true;
  }

  @Test(groups = "cg6-1")
  public void run2() {
    log("run2");
    Assert.assertFalse(m_after);
    m_run2 = true;
  }

  @AfterGroups("cg6-1")
  public void after() {
    log("after");
    Assert.assertTrue(m_run1);
    Assert.assertTrue(m_run2);
    Assert.assertFalse(m_after);
    m_after = true;
  }

  @Test(dependsOnGroups = { "cg6-1" })
  public void verify() {
    log("verify");
    Assert.assertTrue(m_run1, "run1() wasn't run");
    Assert.assertTrue(m_run2, "run2() wasn't run");
    Assert.assertTrue(m_after, "after1() wasn't run");
  }

  private void log(String string) {
    ppp(string);
  }

  private void ppp(String s) {
    if (false) {
      System.out.println("[ConfigurationGroups4SampleTest] " + s);
    }
  }

}
