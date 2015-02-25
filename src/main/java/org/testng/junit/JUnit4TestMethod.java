package org.testng.junit;

import java.lang.reflect.Method;
import org.junit.runner.Description;
import org.testng.internal.Utils;

/**
 *
 * @author lukas
 */
public class JUnit4TestMethod extends JUnitTestMethod {

    public JUnit4TestMethod(JUnitTestClass owner, Description desc) {
        super(owner, desc.getMethodName(), getMethod(desc), desc);
    }

    @Override
    public Object[] getInstances() {
        return new Object[0];
    }

    private static Method getMethod(Description desc) {
        Class<?> c = desc.getTestClass();
        String method = desc.getMethodName();
        // remove [index] from method name in case of parameterized test
        int idx = method.indexOf('[');
        if (idx != -1) {
            method = method.substring(0, idx);
        }
        try {
            return c.getMethod(method);
        } catch (Throwable t) {
            Utils.log("JUnit4TestMethod", 2,
                    "Method '" + method + "' not found in class '" + c.getName() + "': " + t.getMessage());
            return null;
        }
    }
}
