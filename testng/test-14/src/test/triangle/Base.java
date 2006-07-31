package test.triangle;


/**
 * This class
 *
 * @author cbeust
 */
public class Base {
  protected boolean m_isInitialized = false;

  /**
   * @testng.before-suite
   */
  public void init() {
    CountCalls.resetNumCalls();
  }

  /**
   *
   * @testng.before-class
   */
  public void initBeforeTestClass() {
    m_isInitialized = true;
  }

  /**
   * @testng.after-class
   */
  public void postAfterTestClass() {
    CountCalls.incr();
  }

}
