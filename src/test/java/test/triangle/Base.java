package test.triangle;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

/**
 * This class
 *
 * @author cbeust
 */
public class Base {
  protected boolean m_isInitialized = false;

  @BeforeSuite
  public void beforeSuite() {
    CountCalls.numCalls = 0;
  }

  @BeforeClass
  public void initBeforeTestClass() {
    m_isInitialized = true;
  }

  @AfterClass
  public void postAfterTestClass() {
    CountCalls.incr();
  }

  private static void ppp(String s) {
    System.out.println("[Base] " + s);
  }
}
