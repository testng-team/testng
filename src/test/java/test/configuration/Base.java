package test.configuration;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Test(groups = {"base"})
public class Base {
  static int m_count;

  @BeforeTest
  public void init() {
    m_count = 0;
  }

  @BeforeGroups(groups = "foo")
  public void beforeGroups() {
    m_count++;
  }

  private static void ppp(String s) {
    System.out.println("[Base] " + s);
  }
}