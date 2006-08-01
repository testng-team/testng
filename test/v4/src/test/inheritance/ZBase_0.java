package test.inheritance;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Configuration;

public class ZBase_0 {
  protected static boolean m_verbose = false;
  protected static List<String> m_methodList = new ArrayList<String>();

  @Configuration(beforeTest = true)
  public void beforeTest() {
    m_methodList = new ArrayList<String>();
  }
  
  @Configuration(beforeTestMethod = true)
  public void initApplication() {
    m_methodList.add("initApplication");
    ppp("INIT 0");
  }
  
  @Configuration(afterTestMethod = true)
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
