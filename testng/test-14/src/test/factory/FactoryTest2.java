package test.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class
 * 
 * @author cbeust
 */
public class FactoryTest2 {
  private static Map m_numbers = new HashMap();
  private int m_number;
  
  public FactoryTest2() {
    
  }
  
  public static Map getNumbers() {
    return m_numbers;
  }
  
  public FactoryTest2(int n) {
    m_number = n;
  }

  /**
   * @testng.test groups="first"
   */
  public void testInt() {
    Integer n = new Integer(m_number);
    m_numbers.put(n, n);
  }
  
}
