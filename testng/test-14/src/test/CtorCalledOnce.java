package test;



/**
 * this test verifys that the test class is instantiated exactly once
 * regardless of how many test methods we have, showing that TestNG
 * semantics is quite different from JUnit
 */
public class CtorCalledOnce {
    public static int m_instantiated = 0;

    /**
     * @testng.after-test
     */
    public void afterTest() {
      m_instantiated = 0;
    }

    public CtorCalledOnce() {
        m_instantiated++;
    }

    /**
     * @testng.test
     */
    public void testMethod1() {
        assert m_instantiated == 1 : "Expected 1, was invoked " + m_instantiated + " times";
    }

    /**
     * @testng.test
     */
    public void testMethod2() {
        assert m_instantiated == 1 : "Expected 1, was invoked " + m_instantiated + " times";
    }

    /**
     * @testng.test
     */
    public void testMethod3() {
        assert m_instantiated == 1 : "Expected 1, was invoked " + m_instantiated + " times";
    }

}