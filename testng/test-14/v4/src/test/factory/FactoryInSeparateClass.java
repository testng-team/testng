package test.factory;

import test.triangle.CountCalls;



/**
 * this is like the FactoryTest, except it creates test instances in a separate
 * class from the test class
 */
public class FactoryInSeparateClass {
  static private boolean m_wasRun = false;
  static private int m_checkSum = 0;
  
  /**
   * @testng.configuration beforeSuite = "true"
   */
  public void init() {
    m_wasRun = false;
    m_checkSum = 0;
  }

  public static void addToSum(int i) {
    m_checkSum += i; 
  }

  /**
   * @testng.factory
   */
  public Object[] createObjects() {
    return new Object[] {
      new MyTest(1),
      new MyTest(2),
      new MyTest(3),
    };
  }

    /**
     * @testng.test groups="testMethodOnFactoryClass" dependsOnGroups="MyTest"
     */
    public void checkSum() {
    m_wasRun = true;
      assert (m_checkSum == 6) :
        "Test instances made by factory did not invoke their test methods correctly.  expected 6 but got " + m_checkSum;
    }
  
    public static boolean wasRun() {
      return m_wasRun;
    }
}

