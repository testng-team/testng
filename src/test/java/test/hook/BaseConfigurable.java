package test.hook;

import org.testng.IConfigurable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

abstract public class BaseConfigurable implements IConfigurable {
  protected int m_hookCount = 0;
  protected  boolean m_bm = false;
  protected  boolean m_bc = false;
  protected  boolean m_bs = false;
  protected  boolean m_bt = false;

  @BeforeMethod
  public void bm() {
    m_bm = true;
  }

  @BeforeClass
  public void bc() {
    m_bc = true;
  }

  @BeforeSuite
  public void bs() {
    m_bs = true;
  }

  @BeforeTest
  public void bt() {
    m_bt = true;
  }

}
