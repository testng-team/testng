package test.sample;


public class Basic2 {
  private boolean m_basic2WasRun = false;
  private static int m_afterClass = 0;
  
  /**
   * @testng.configuration beforeSuite = "true"
   */
  public void init() {
    m_afterClass = 0;
    m_basic2WasRun = false;
  }

  /**
   * @testng.test dependsOnGroups="basic1"
   */
  public void basic2() {
    m_basic2WasRun = true;
    assert Basic1.getCount() > 0 : "COUNT WAS NOT INCREMENTED";
  }

  private void ppp(String s) {
    System.out.println("[Basic2 "
        + Thread.currentThread().hashCode() + " ] " + hashCode() + " " + s);
  }

  /**
   * @testng.configuration afterTestClass="true"
   */
  public void checkTestAtClassLevelWasRun() {
    m_afterClass++;
    assert m_basic2WasRun : "Class annotated with @Test didn't have its methods run.";
    assert 1 == m_afterClass : "After class should have been called exactly once, not " + m_afterClass;
  }
}