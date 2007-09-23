package test.invocationcount;

public class FailedInvocationCount2 {
  int m_count;
  int m_count2;

  /**
   * @testng.before-class
   */
  public void setUp() {
    m_count = 0;
    m_count2 = 0;
  }

  /**
   * @testng.test invocationCount="10" skipFailedInvocations="true"
   */
  public void shouldSkipFromAnnotation() {
    if (m_count++ > 3) {
      throw new RuntimeException();
    }
  }
  
  /**
   * @testng.test invocationCount="10" skipFailedInvocations="false"
   */
  public void shouldNotSkipFromAnnotation() {
    if (m_count2++ > 3) {
      throw new RuntimeException();
    }
  }

}
