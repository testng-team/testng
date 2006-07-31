package test.tmp;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = {"base"})
public class Base {
  protected boolean m_beforeTest = false;
  protected boolean m_afterTest = false;

    @Configuration(beforeTestClass = true)
    public void baseSetup() {
      m_beforeTest = true;
      System.out.println("base before class");
    }

    @Configuration(afterTestClass = true)
    public void baseTeardown() {
      m_afterTest = true;
      System.out.println("base after class");
    }
}