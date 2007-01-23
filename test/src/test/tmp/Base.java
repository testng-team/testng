package test.tmp;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

@Test(groups = {"base"})
public class Base {
  private static int m_count = 0;
  
  @BeforeGroups(groups = "foo")
  public void beforeGroups() {
    ppp("BEFORE_GROUPS");
    m_count++;
  }
  
  @AfterTest
  public void verify() {
    Assert.assertEquals(m_count, 1);
  }
  
  private static void ppp(String s) {
    System.out.println("[Base] " + s);
  }
}