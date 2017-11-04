package org.testng.xml.internal;

import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

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

    @Test
    public void testCloneIfContainsTestsWithNamesMatchingAny() {
        XmlSuite suite = createDummySuiteWithTestNamesAs("test1", "test2");
        XmlSuite clonedSuite = XmlSuiteUtils.cloneIfContainsTestsWithNamesMatchingAny(suite, Collections.singletonList("test2"));
        assertThat(suite.getTests()).hasSameElementsAs(clonedSuite.getTests());
    }

    @Test(description = "GITHUB-1594", dataProvider = "getTestnames")
    public void testCloneIfContainsTestsWithNamesMatchingAnyChildSuites(String testname, boolean foundInParent) {
        XmlSuite parentSuite = createDummySuiteWithTestNamesAs("test1", "test2");
        parentSuite.setName("parent_suite");
        XmlSuite childSuite = createDummySuiteWithTestNamesAs("test3", "test4");
        childSuite.setName("child_suite");
        parentSuite.getChildSuites().add(childSuite);
        XmlSuite clonedSuite = XmlSuiteUtils.cloneIfContainsTestsWithNamesMatchingAny(parentSuite, Collections.singletonList(testname));
        if (foundInParent) {
            assertThat(clonedSuite.getTests()).hasSameElementsAs(parentSuite.getTests());
        } else {
            assertThat(clonedSuite.getTests()).hasSameElementsAs(childSuite.getTests());
        }
    }

    @Test(expectedExceptions = TestNGException.class,
            expectedExceptionsMessageRegExp = "\nPlease provide a valid list of names to check.",
            dataProvider = "getData")
    public void testCloneIfContainsTestsWithNamesMatchingAnyNegativeCondition(XmlSuite xmlSuite, List<String> names) {
        XmlSuiteUtils.cloneIfContainsTestsWithNamesMatchingAny(xmlSuite, names);
    }

    @Test(expectedExceptions = TestNGException.class,
            expectedExceptionsMessageRegExp = "\nThe test\\(s\\) \\<\\[test3\\]\\> cannot be found.")
    public void testCloneIfContainsTestsWithNamesMatchingAnyWithoutMatch() {
        XmlSuite xmlSuite = createDummySuiteWithTestNamesAs("test1", "test2");
        XmlSuiteUtils.cloneIfContainsTestsWithNamesMatchingAny(xmlSuite, Collections.singletonList("test3"));
    }

    @DataProvider(name = "getTestnames")
    public Object[][] getTestnameToSearchFor() {
        return new Object[][]{
                {"test4", false},
                {"test1", true}
        };
    }

    @DataProvider(name = "getData")
    public Object[][] getTestData() {
        return new Object[][]{
                {new XmlSuite(), null},
                {new XmlSuite(), Collections.<String>emptyList()}
        };
    }
}
