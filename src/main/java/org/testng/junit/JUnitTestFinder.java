package org.testng.junit;

import java.lang.reflect.Modifier;
import org.testng.internal.Utils;

/**
 *
 * @author ljungman
 */
public final class JUnitTestFinder {

    private static final String JUNIT3_TEST = "junit.framework.Test";
    private static final String JUNIT3_FINDER = "org.testng.junit.JUnit3TestRecognizer";
    private static final String JUNIT4_TEST = "org.junit.Test";
    private static final String JUNIT4_FINDER = "org.testng.junit.JUnit4TestRecognizer";
    private static final JUnitTestRecognizer junit3;
    private static final JUnitTestRecognizer junit4;

    static {
        junit3 = getJUnitTestRecognizer(JUNIT3_TEST, JUNIT3_FINDER);
        junit4 = getJUnitTestRecognizer(JUNIT4_TEST, JUNIT4_FINDER);
        if (junit3 == null) {
            Utils.log("JUnitTestFinder", 2, "JUnit3 was not found on the classpath");

        }
        if (junit4 == null) {
            Utils.log("JUnitTestFinder", 2, "JUnit4 was not found on the classpath");
        }
    }

    public static boolean isJUnitTest(Class c) {
        if (!haveJUnit()) {
            return false;
        }
        //only public classes are interesting, so filter out the rest
        if (!Modifier.isPublic(c.getModifiers()) || c.isInterface() || c.isAnnotation() || c.isEnum()) {
            return false;
        }
        return (junit3 != null && junit3.isTest(c)) || (junit4 != null && junit4.isTest(c));
    }

    private static boolean haveJUnit() {
        return junit3 != null || junit4 != null;
    }

    private static JUnitTestRecognizer getJUnitTestRecognizer(String test, String name) {
        try {
            Class.forName(test);
            Class<JUnitTestRecognizer> c = (Class<JUnitTestRecognizer>) Class.forName(name);
            return c.newInstance();
        } catch (Throwable t) {
            //ignore
            return null;
        }
    }
}
