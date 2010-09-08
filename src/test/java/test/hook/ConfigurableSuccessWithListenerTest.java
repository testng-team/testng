package test.hook;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ConfigurableListener.class)
public class ConfigurableSuccessWithListenerTest {
  protected  boolean m_bm = false;
  protected  boolean m_bc = false;

  @BeforeMethod
  public void bm() {
    m_bm = true;
  }

  @BeforeClass
  public void bc() {
    m_bc = true;
  }

  @Test
  public void hookWasRun() {
    Assert.assertEquals(ConfigurableListener.m_hookCount, 2);
    Assert.assertTrue(m_bc);
    Assert.assertTrue(m_bm);
  }
}
