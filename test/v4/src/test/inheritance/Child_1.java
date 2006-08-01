package test.inheritance;

import org.testng.annotations.Configuration;

public class Child_1 extends ZBase_0 {

  @Configuration(beforeTestMethod = true)
  public void initDialog() {
    m_methodList.add("initDialog");
    ppp("  INIT 1");
  }
  
  @Configuration(afterTestMethod = true)
  public void tearDownDialog() {
    m_methodList.add("tearDownDialog");
    ppp("  TEAR_DOWN 1");
  }
  
  private static void ppp(String s) {
    if (m_verbose) {
      System.out.println("[C1] " + s);
    }
  }
  
}
