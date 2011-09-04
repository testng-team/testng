package test.bug90;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class Sample {
  static boolean m_afterClassWasRun = false;

  @Test
  public void test1() {
  }


  @Test
  public void test2() {
  }
  
  @AfterClass
  public void afterClass() {
    m_afterClassWasRun = true;
  }
}
