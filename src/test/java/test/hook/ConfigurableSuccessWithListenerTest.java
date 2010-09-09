package test.hook;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

@Listeners(ConfigurableListener.class)
public class ConfigurableSuccessWithListenerTest {
  static boolean m_bm = false;
  static boolean m_bc = false;
  static boolean m_bt;
  static boolean m_bs;

  @BeforeSuite
  public void bs() {
    m_bs = true;
  }

  @BeforeMethod
  public void bt() {
    m_bt = true;
  }

  @BeforeMethod
  public void bm(Method m) {
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
