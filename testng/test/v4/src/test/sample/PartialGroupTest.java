package test.sample;

import org.testng.annotations.*;

/**
 * This class tests groups that are partially defined at the class level
 * and then augmented at the method level.
 * 
 * @author cbeust
 */

@Test(groups = { "classGroup" })
public class PartialGroupTest {
  public static boolean m_successMethod = false;
  public static boolean m_successClass = false;
  
  @Configuration(beforeTestClass = true)
  public void init() {
    m_successMethod = false;
    m_successClass = false;
  }
  
  @Test(groups = { "methodGroup" })
  public void testMethodGroup() {
    m_successMethod = true;
  }
  
  @Test
  public void testClassGroupShouldFail() {
    assert false;
  }
  
  @Test(groups = { "methodGroup" })
  public void testMethodGroupShouldFail() {
    assert false;
  }

  @Test
  public void testClassGroup() {
    m_successClass = true;
  }
  
}
