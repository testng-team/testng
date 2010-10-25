package test.dependent;

import org.testng.annotations.Test;


/**
 * This class verifies that when methods have dependents, they are run
 * in the correct order.
 *
 * @author Cedric Beust, Aug 19, 2004
 *
 */
public class OrderMethodTest extends BaseOrderMethodTest {
  @Test(groups = { "1.0" })
  public void z_first0() {
//    ppp("1.0");
    m_group1[0] = true;
  }

  @Test(groups = { "2.1" }, dependsOnGroups = { "1.0", "1.1" })
  public void a_second1() {
//    ppp("2.1");
    verifyGroup(2, m_group1);
    m_group2[1] = true;
  }

  @Test(groups = { "1.1" })
  public void z_premiere1() {
//    ppp("1.1");
    m_group1[1] = true;
  }



}
