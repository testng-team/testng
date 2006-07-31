package test.sample;

import org.testng.annotations.*;

/**
 * This class fails in setUp and should result in 1 failure, 1 skip
 * 
 * @author cbeust
 */
public class SetUpWithParameterTest {
  
  @Configuration(beforeTestClass = true)
  public void setUp(String bogusParameter) {
  }

  @Test
  public void test() {
    
  }
}
