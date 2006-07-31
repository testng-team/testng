package test.dependent;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ClassWide1Test {
  private static boolean m_ok = false;

  @Configuration(beforeTest = true)
  public void init() {
    m_ok = false;
  }
  
  @Test
  public void m1() {
    m_ok = true;
  }

  public static boolean m1WasRun() {
    return m_ok;
  }
}
