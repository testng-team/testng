package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * a will fail but b should run anyway because of alwaysRun=true
 *
 * @author cbeust
 */
public class DependentOnMethod1AlwaysRunSampleTest {

  private boolean m_ok = false;

  @Test
  public void a() {
    throw new RuntimeException("Voluntary failure");
  }

  @Test(
      dependsOnMethods = {"a"},
      alwaysRun = true)
  public void b() {
    m_ok = true;
  }

  @Test(dependsOnMethods = {"b"})
  public void verify() {
    Assert.assertTrue(m_ok, "method b() should have been invoked");
  }
}
