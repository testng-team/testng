package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * a will fail but b should run anyway because of alwaysRun=true
 *
 * Created on Nov 18, 2005
 * @author cbeust
 */
public class DependentOnGroup2AlwaysRunSampleTest {

  private boolean m_ok = false;

  @Test(groups = { "group-a"})
  public void a() {
    throw new RuntimeException("Voluntary failure");
  }

  @Test(groups = { "group-a"})
  public void a2() {
  }

  @Test(dependsOnGroups = {"group-a"}, alwaysRun = true)
  public void b() {
    m_ok = true;
  }

  @Test(dependsOnMethods = {"b"})
  public void verify() {
    Assert.assertTrue(m_ok, "method b() should have been invoked");
  }
}
