package test.tmp;

import org.testng.ITest;
import org.testng.annotations.Test;

public class B implements ITest {
  private String m_name;
  
  public B(String s) {
    m_name = s;
  }
  
  @Test
  public void t() {
    
  }

  public String getTestName() {
    return m_name;
  }
}