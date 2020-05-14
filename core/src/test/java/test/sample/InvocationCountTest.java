package test.sample;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;


/**
 * This class is used to test invocationCountTest
 *
 * @author cbeust
 */
public class InvocationCountTest {

  //
  // Invocation test
  //
  private static int m_count = 0;

  @AfterSuite(groups = {"invocationOnly"})
  public void afterSuite() {
    m_count = 0;
    m_count2 = 0;
    m_count3 = 0;
  }

  @Test(groups = { "invocationOnly"}, invocationCount = 10 )
  public void tenTimesShouldSucceed() {
    m_count++;
  }

  //
  // Invocation + Success percentage test
  // This method will work the first 8 times and fail after that, but overall
  // the test should still pass because successPercentage = 80
  //
  private static int m_count2 = 0;

  @Test(groups = { "successPercentageThatSucceedsOnly" },
    invocationCount = 10, successPercentage = 80)
  public void successPercentageShouldSucceed() {
    if (m_count2 >= 8) {
      throw new RuntimeException("Called more than eight times : " + m_count2);
    }
    m_count2++;
  }

  //
  // Invocation + Success percentage test
  // This method will work the first 8 times and fail after that.  One of
  // the failures will fall under the percentage tolerance but the next one
  // will not.
  //
  private static int m_count3 = 0;

  @Test(groups = { "successPercentageThatFailsOnly" },
    invocationCount = 10, successPercentage = 90)
  public void successPercentageShouldFail() {
    if (m_count3>= 8) {
      throw new RuntimeException("Called more than eight times : " + m_count3);
    }
    m_count3++;
  }

  @AfterClass(groups = { "invocationOnly"})
  public void verify() {
    assert 10 == m_count : "Method should have been invoked 10 times but was invoked "
      + m_count + " times";
  }

  public static void ppp(String s) {
    System.out.println("[InvocationCount] " + s);
  }

}
