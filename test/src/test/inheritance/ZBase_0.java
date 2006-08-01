package test.inheritance;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

public class ZBase_0 {
  protected static boolean m_verbose = false;
  protected static List<String> m_methodList = new ArrayList<String>();

  @BeforeTest
  public void beforeTest() {
    m_methodList = new ArrayList<String>();
  }
  
  @BeforeMethod
  public void initApplication() {
    m_methodList.add("initApplication");
    ppp("INIT 0");
  }
  
  @AfterMethod
  public void tearDownApplication() {
    m_methodList.add("tearDownApplication");
    ppp("TEAR DOWN 0");
  }

  private static void ppp(String s) {
    if (m_verbose) {
      System.out.println("[Z0] " + s);
    }
  }
  
  public static List<String> getMethodList() {
    return m_methodList;
  }
}
