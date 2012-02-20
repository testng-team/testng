package org.testng.junit;

import junit.framework.Test;

/**
 *
 * @author lukas
 */
public class JUnit3TestClass extends JUnitTestClass {

    public JUnit3TestClass(Test test) {
        super(test.getClass());
    }
}
