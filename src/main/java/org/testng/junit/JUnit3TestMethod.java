package org.testng.junit;

import java.lang.reflect.Method;
import junit.framework.Test;
import org.testng.internal.Utils;

/**
 *
 * @author lukas
 */
public class JUnit3TestMethod extends JUnitTestMethod {

    public JUnit3TestMethod(JUnitTestClass owner, Test test) {
        super(owner, getMethod(test), test);
    }

    private static Method getMethod(Test t) {
        String name = null;
        try {
            Method nameMethod = t.getClass().getMethod("getName");
            name = (String) nameMethod.invoke(t);
            return t.getClass().getMethod(name);
        } catch (Throwable th) {
            Utils.log("JUnit3TestMethod", 2,
                    "Method '" + name + "' not found in class '" + t + "': " + th.getMessage());
            return null;
        }
    }
}
