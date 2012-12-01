package org.testng.junit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.Test;

/**
 *
 * @author lukas
 */
public class JUnit3TestRecognizer implements JUnitTestRecognizer {

    public JUnit3TestRecognizer() {
    }

    public boolean isTest(Class c) {
        //class implementing junit.framework.Test with at least one test* method
        if (Test.class.isAssignableFrom(c)) {
            boolean haveTest = false;
            for (Method m : c.getMethods()) {
                if (m.getName().startsWith("test")) {
                    haveTest = true;
                    break;
                }
            }
            if (haveTest) {
                return true;
            }
        }
        try {
            //or a class with public static Test suite() method
            Method m = c.getDeclaredMethod("suite");
            if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                return m.getReturnType().isAssignableFrom(Test.class);
            }
        } catch (Throwable t) {
            return false;
        }
        return false;
    }
}
