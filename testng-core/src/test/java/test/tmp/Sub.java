package test.tmp;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"sub"})
public class Sub extends Base {
  boolean m_beforeTest;
  boolean m_afterTest;

  @BeforeClass
  public void subSetup() {
    System.out.println("sub before class");
  }

  @AfterClass
  public void subTeardown() {
    System.out.println("sub after class");
  }

  public void subTest() {
    System.out.println("sub test");
  }

  @AfterSuite
  public void verify() {
    Assert.assertTrue(m_beforeTest);
    Assert.assertTrue(m_afterTest);
  }
}
