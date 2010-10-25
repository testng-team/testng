package test.sample;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Check to see that AfterClass is called only at the end and that after methods
 * are called in reverse order of the before methods.
 */
public class AfterClassCalledAtEnd extends BaseAfterClassCalledAtEnd {
  boolean m_before1Class = false;
  boolean m_test1 = false;
  boolean m_test2 = false;
  boolean m_test3 = false;

  @BeforeClass(groups = { "before1Class" } )
  public void before1Class() {
    m_before1Class = true;
  }

  @AfterClass(groups = { "someGroup" })
  public void afterClass() {
    m_afterClass = true;
    assert m_test1 && m_test2 && m_test3 :
      "One of the test methods was not invoked: " + m_test1 + " " + m_test2 + " " + m_test3;
  }

  @Test(description = "Verify that beforeClass and afterClass are called correctly")
  public void test1() {
    m_test1 = true;
    assert m_before1Class : "beforeClass configuration must be called before method";
    assert ! m_afterClass : "afterClass configuration must not be called before test method";
  }

  @Test
  public void test2() {
    m_test2 = true;
    assert m_before1Class : "beforeClass configuration must be called before method";
    assert ! m_afterClass : "afterClass configuration must not be called before test method";
  }

  @Test
  public void test3() {
    m_test3 = true;
    assert m_before1Class : "beforeClass configuration must be called before method";
    assert ! m_afterClass : "afterClass configuration must not be called before test method";
  }

}