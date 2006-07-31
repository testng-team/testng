package test.triangle;


/**
 * This class
 *
 * @author cbeust
 */
public class Base {
  protected boolean m_isInitialized = false;

  /**
   * @testng.configuration beforeSuite = "true"
   */
  public void init() {
    CountCalls.resetNumCalls();
  }

  /**
   *
   * @testng.configuration beforeTestClass="true"
   */
  public void initBeforeTestClass() {
    m_isInitialized = true;
  }

  /**
   * @testng.configuration afterTestClass="true"
   */
  public void postAfterTestClass() {
    CountCalls.incr();
  }

}
