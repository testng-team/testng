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

    @Test
    public void testCloneIfContainsTestsWithNamesMatchingAny() {
        XmlSuite suite = createDummySuiteWithTestNamesAs("test1", "test2");
        XmlSuiteUtils xmlSuiteUitls = new XmlSuiteUtils();
        xmlSuiteUitls.cloneIfContainsTestsWithNamesMatchingAny(suite, Collections.singletonList("test2"));
        List<XmlTest> xmlTests = xmlSuiteUitls.getMatchedTests();
        assertThat(suite.getTests()).hasSameElementsAs(xmlTests);
    }

    @Test(description = "GITHUB-1594", dataProvider = "getTestnames")
    public void testCloneIfContainsTestsWithNamesMatchingAnyChildSuites(String testname, boolean foundInParent, boolean foundInChildOfChild) {
        XmlSuite parentSuite = createDummySuiteWithTestNamesAs("test1", "test2");
        parentSuite.setName("parent_suite");
        XmlSuite childSuite = createDummySuiteWithTestNamesAs("test3", "test4");
        childSuite.setName("child_suite");
        parentSuite.getChildSuites().add(childSuite);
        XmlSuite childOfChildSuite = createDummySuiteWithTestNamesAs("test5", "test6");
        childSuite.getChildSuites().add(childOfChildSuite);
        XmlSuiteUtils xmlSuiteUitls = new XmlSuiteUtils();
        xmlSuiteUitls.cloneIfContainsTestsWithNamesMatchingAny(parentSuite, Collections.singletonList(testname));
        List<XmlTest> xmlTests = xmlSuiteUitls.getMatchedTests();
        if (foundInParent) {
            assertThat(xmlTests).hasSameElementsAs(parentSuite.getTests());
        } else if (!foundInChildOfChild) {
            assertThat(xmlTests).hasSameElementsAs(childSuite.getTests());
        } else {
            assertThat(xmlTests).hasSameElementsAs(childOfChildSuite.getTests());
        }
    }

    @Test(expectedExceptions = TestNGException.class,
            expectedExceptionsMessageRegExp = "\nPlease provide a valid list of names to check.",
            dataProvider = "getData")
    public void testCloneIfContainsTestsWithNamesMatchingAnyNegativeCondition(XmlSuite xmlSuite, List<String> names) {
        XmlSuiteUtils xmlSuiteUitls = new XmlSuiteUtils();
        xmlSuiteUitls.cloneIfContainsTestsWithNamesMatchingAny(xmlSuite, names);
    }
    
    @Test
    public void testIfTestnamesComesFromDifferentSuite() {
        XmlSuite parentSuite = createDummySuiteWithTestNamesAs("test1", "test2");
        parentSuite.setName("parent_suite");
        XmlSuite childSuite = createDummySuiteWithTestNamesAs("test3", "test4");
        childSuite.setName("child_suite");
        parentSuite.getChildSuites().add(childSuite);
        XmlSuite childOfChildSuite = createDummySuiteWithTestNamesAs("test5", "test6");
        childSuite.getChildSuites().add(childOfChildSuite);
        XmlSuiteUtils xmlSuiteUitls = new XmlSuiteUtils();
        xmlSuiteUitls.cloneIfContainsTestsWithNamesMatchingAny(parentSuite,
                new ArrayList<>(Arrays.asList("test1", "test3", "test5")));
        List<String> matchedTestnames = xmlSuiteUitls.getMatchedTestNames();
        assertThat(matchedTestnames).hasSameElementsAs(Arrays.asList("test1", "test3", "test5"));
    }
    
    @Test(expectedExceptions = TestNGException.class,
            expectedExceptionsMessageRegExp = "\nThe test\\(s\\) \\<\\[test3\\]\\> cannot be found.")
    public void testCloneIfContainsTestsWithNamesMatchingAnyWithoutMatch() {
        XmlSuite xmlSuite = createDummySuiteWithTestNamesAs("test1", "test2");
        XmlSuiteUtils xmlSuiteUitls = new XmlSuiteUtils();
        xmlSuiteUitls.cloneIfContainsTestsWithNamesMatchingAny(xmlSuite, Collections.singletonList("test3"));
        List<XmlSuite> clonedSuites = xmlSuiteUitls.getCloneSuite();
        if (!CollectionUtils.hasElements(clonedSuites)) {
            throw new TestNGException(
                    "The test(s) <" + Collections.singletonList("test3").toString() + "> cannot be found.");
        }
    }

    @DataProvider(name = "getTestnames")
    public Object[][] getTestnameToSearchFor() {
        return new Object[][]{
            { "test4", false, false },
            { "test1", true, false },
            { "test5", false, true }
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
