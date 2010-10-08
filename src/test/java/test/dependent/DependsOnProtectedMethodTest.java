package test.dependent;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class DependsOnProtectedMethodTest {
  private boolean m_before1 = false;
  private boolean m_before2 = false;

  @BeforeMethod(dependsOnMethods = { "before2" })
  protected void before() {
    m_before1 = true;
  }

  @BeforeMethod
  protected void before2() {
    m_before2 = true;
  }

  @Test
  public void verifyBeforeInvocations() {
    Assert.assertTrue(m_before1 && m_before2, "Protected dependent @BeforeMethods should have been invoked");
  }
}
