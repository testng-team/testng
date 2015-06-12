package test.factory;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is created by FactoryWithInstanceInfoTest2
 *
 * @author cbeust
 */
public class FactoryWithInstanceInfoTest2 {
  private static Map<Integer, Integer> m_numbers = new HashMap<>();
  private int m_number;

  public FactoryWithInstanceInfoTest2() {
    throw new RuntimeException("Shouldn't be invoked");
  }

  public static Map<Integer, Integer> getNumbers() {
    return m_numbers;
  }

  public FactoryWithInstanceInfoTest2(int n) {
    m_number = n;
  }

  @Test(groups = { "first" })
  public void testInt() {
    Integer n = m_number;
    m_numbers.put(n, n);
  }

  private static void ppp(String s) {
    System.out.println("[FactoryTest2] " + s);
  }

}
