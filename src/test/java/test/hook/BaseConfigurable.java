package test.hook;

import org.testng.IConfigurable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

import java.lang.reflect.Method;

abstract public class BaseConfigurable implements IConfigurable {
  static int m_hookCount = 0;
  static boolean m_bs = false;
  static boolean m_bt = false;
  static boolean m_bm = false;
  static boolean m_bc = false;
  static String m_methodName = null;

  @BeforeSuite
  public void bs() {
    m_bs = true;
  }

  @BeforeTest
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

}
