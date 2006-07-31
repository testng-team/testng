package test.sample;


/**
 * This class tests groups that are partially defined at the class level
 * and then augmented at the method level.
 *
 * @author cbeust
 * @testng.test groups="classGroup"
 */
public class PartialGroupTest {
  public static boolean m_successMethod = false;
  public static boolean m_successClass = false;

  /**
   * @testng.before-class
   */
  public void init() {
    m_successMethod = false;
    m_successClass = false;
  }

  /**
   * @testng.test groups="methodGroup"
   */
  public void testMethodGroup() {
    m_successMethod = true;
  }

  /**
   * @testng.test
   */
  public void testClassGroup() {
    m_successClass = true;
  }

  /**
   * @testng.test groups="methodGroup"
   */
  public void testMethodGroupShouldFail() {
//  	System.out.println("testMethodGroupShouldFail");
  	assert false;
  }
  
  /**
   * @testng.test
   */
  public void testClassGroupShouldFail() {
//  	System.out.println("testClassGroupShouldFail");
   assert false;
 }


}
