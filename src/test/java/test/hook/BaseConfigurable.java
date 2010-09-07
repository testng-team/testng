package test.hook;

import org.testng.IConfigurable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

abstract public class BaseConfigurable implements IConfigurable {
  protected int m_hookCount = 0;
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

}
