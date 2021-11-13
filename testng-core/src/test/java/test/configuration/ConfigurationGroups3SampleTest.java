package test.configuration;

import java.util.ArrayList;
import java.util.List;
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
    log("before1");
  }

  @Test(groups = "cg34-a")
  public void fa() {
    log("fa");
  }

  @Test(groups = "cg34-1")
  public void f1() {
    Assert.assertTrue(m_before);
    Assert.assertTrue(Base3.getBefore());
    m_f1 = true;
    log("f1");
  }

  private List<String> m_list = new ArrayList<>();

  @Test(dependsOnGroups = {"cg34-a", "cg34-1"})
  public void verify() {
    Assert.assertTrue(m_before);
    Assert.assertTrue(Base3.getBefore());
    Assert.assertTrue(m_f1);
  }

  private void log(String s) {
    m_list.add(s);
  }

  public static boolean getF1() {
    return m_f1;
  }
}
