package test.inheritance;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Make sure that configuration inherited methods are invoked in the right
 * order.  The funny naming is to make sure that the class names do not
 * play any role in the ordering of methods.
 *
 * Created on Sep 8, 2005
 * @author cbeust
 */
public class DChild_2 extends Child_1 {

  @BeforeMethod
  public void initDialog2() {
    m_methodList.add("initDialog2");
    ppp("    INIT 2");
  }

  @AfterMethod
  public void tearDownDialog2() {
    m_methodList.add("tearDownDialog2");
    ppp("    TEAR_DOWN 2");
  }

  @Test(groups = {"before" })
  public void test() {
    m_methodList.add("test");
    ppp("      TEST");
  }

  private static void ppp(String s) {
    if (m_verbose) {
      System.out.println("[D2] " + s);
    }
  }
}
