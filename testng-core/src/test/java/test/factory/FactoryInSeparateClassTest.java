package test.factory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * this is like the FactoryTest, except it creates test instances in a separate class from the test
 * class
 */
public class FactoryInSeparateClassTest {
  private static boolean m_wasRun = false;
  private static int m_checkSum = 0;

  public static void addToSum(int i) {
    m_checkSum += i;
  }

  @BeforeTest
  public void beforeTest() {
    m_wasRun = false;
    m_checkSum = 0;
  }

  @Factory
  public Object[] createObjects() {
    return new Object[] {
      new MySample(1), new MySample(2), new MySample(3),
    };
  }

  @Test(
      groups = "testMethodOnFactoryClass",
      dependsOnGroups = {"MySample"})
  public void checkSum() {
    m_wasRun = true;
    assertThat(m_checkSum)
        .withFailMessage(
            "Test instances made by factory did not invoke their test methods correctly.  expected 6 but got "
                + m_checkSum)
        .isEqualTo(6);
  }

  public static boolean wasRun() {
    return m_wasRun;
  }
}
