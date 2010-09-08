package test.hook;

import org.testng.IConfigurable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

abstract public class BaseConfigurable implements IConfigurable {
  static int m_hookCount = 0;
  static boolean m_bs = false;
  static boolean m_bt = false;
  static boolean m_bm = false;
  static boolean m_bc = false;

  @BeforeSuite
  public void bs() {
    m_bs = true;
  }

  @BeforeMethod
  public void bt() {
    m_bt = true;
  }

  @BeforeMethod
  public void bm() {
    m_bm = true;
  }

  @BeforeClass
  public void bc() {
    m_bc = true;
  }

}
