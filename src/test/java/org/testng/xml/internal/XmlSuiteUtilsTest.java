package org.testng.xml.internal;

import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.CollectionUtils;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlSuiteUtilsTest extends SimpleBaseTest {

    @Test(expectedExceptions = TestNGException.class,
            expectedExceptionsMessageRegExp = "\nTwo tests in the same suite \\[random_suite\\] cannot have the same name: test1")
    public void testEnsureNoDuplicateTestsArePresentNegativeCondition() {
        XmlSuite suite = createDummySuiteWithTestNamesAs("test1", "test1");
        XmlSuiteUtils.ensureNoDuplicateTestsArePresent(suite);
    }

    @Test
    public void testEnsureNoDuplicateTestsArePresent() {
        XmlSuite suite = createDummySuiteWithTestNamesAs("test1", "test2");
        XmlSuiteUtils.ensureNoDuplicateTestsArePresent(suite);
    }
}
