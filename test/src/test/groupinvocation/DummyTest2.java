package test.groupinvocation;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;


/**
 * This class/interface 
 */
public class DummyTest2 {
  private boolean m_invoked= false;
  
  @Test(groups={"A"})
  public void dummyTest() {
    System.out.println(".dummyTest: should NOT be invoked");
    m_invoked= true;
  }
  
  @AfterClass(alwaysRun=true)
  public void checkInvocations() {
    Assert.assertFalse(m_invoked, "@Test method invoked even if @BeforeGroups failed");
  }
}
