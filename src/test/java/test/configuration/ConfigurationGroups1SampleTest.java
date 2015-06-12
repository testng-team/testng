package test.configuration;

import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple beforeGroups test:  1 before method and 2 test method
 *
 * @author cbeust
 * @date Mar 3, 2006
 */
public class ConfigurationGroups1SampleTest {
  private boolean m_before = false;
  private boolean m_f1 = false;

  @BeforeGroups("cg1-1")
  public void before1() {
    Assert.assertFalse(m_before);
    Assert.assertFalse(m_f1);
    m_before = true;
    log("before1");
  }

  @Test(groups = "cg1-a")
  public void fa() {
    log("fa");
  }

  @Test(groups = "cg1-1")
  public void f1() {
    Assert.assertTrue(m_before);
    m_f1 = true;
    log("f1");
  }

  private List<String> m_list = new ArrayList<>();

  @Test(dependsOnGroups = {"cg1-a", "cg1-1"})
  public void verify() {
    Assert.assertTrue(m_before);
    Assert.assertTrue(m_f1);
  }

  private void log(String s) {
    m_list.add(s);
    ppp(s);
  }

  private void ppp(String s) {
    if (false) {
      System.out.println("[ConfigurationGroups1SampleTest] " + s);
    }
  }

}
