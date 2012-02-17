package org.testng.junit;

import org.junit.runner.Description;

/**
 *
 * @author lukas
 */
public class JUnit4TestClass extends JUnitTestClass {

    public JUnit4TestClass(Description test) {
        super(test.getTestClass());
    }
}
