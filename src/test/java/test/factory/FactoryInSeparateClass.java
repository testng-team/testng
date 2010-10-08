package test.factory;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * this is like the FactoryTest, except it creates test instances in a separate
 * class from the test class
 */
public class FactoryInSeparateClass {
  static private boolean m_wasRun = false;
  static private int m_checkSum = 0;

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
      new MyTest(1),
      new MyTest(2),
      new MyTest(3),
    };
  }

   @Test(groups = "testMethodOnFactoryClass", dependsOnGroups={"MyTest"})
    public void checkSum() {
    m_wasRun = true;
      assert (m_checkSum == 6) :
        "Test instances made by factory did not invoke their test methods correctly.  expected 6 but got " + m_checkSum;
    }

    public static boolean wasRun() {
      return m_wasRun;
    }
}

