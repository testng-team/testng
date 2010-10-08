package test.tmp;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = {"sub"})
public class Sub extends Base {
  boolean m_beforeTest;
  boolean m_afterTest;

    @Configuration(beforeTestClass = true)
    public void subSetup() {
        System.out.println("sub before class");
    }

    @Configuration(afterTestClass = true)
    public void subTeardown() {
        System.out.println("sub after class");
    }

    public void subTest() {
        System.out.println ("sub test");
    }

    @Configuration(afterSuite = true)
    public void verify() {
      Assert.assertTrue(m_beforeTest);
      Assert.assertTrue(m_afterTest);
    }
}
