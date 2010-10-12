package test.inheritance;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class Child_1 extends ZBase_0 {

  @BeforeMethod
  public void initDialog() {
    m_methodList.add("initDialog");
    ppp("  INIT 1");
  }

  @AfterMethod
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
