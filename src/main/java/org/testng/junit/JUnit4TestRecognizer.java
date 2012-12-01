package org.testng.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.junit.runner.RunWith;

/**
 *
 * @author lukas
 */
public final class JUnit4TestRecognizer implements JUnitTestRecognizer {

    public JUnit4TestRecognizer() {
    }

    public boolean isTest(Class c) {
        for (Annotation an: c.getAnnotations()) {
            if (RunWith.class.isAssignableFrom(an.annotationType())) {
                return true;
            }
        }
        boolean haveTest = false;
        for (Method m : c.getMethods()) {
            for (Annotation a : m.getDeclaredAnnotations()) {
                if (org.junit.Test.class.isAssignableFrom(a.annotationType())) {
                    haveTest = true;
                    break;
                }
            }
        }
        return haveTest;
    }
}
