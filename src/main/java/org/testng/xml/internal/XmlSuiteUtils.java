package org.testng.xml.internal;

import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;
import java.util.Set;

/**
 * A utility class that exposes helper methods to work with {@link XmlSuite}
 */
public final class XmlSuiteUtils {

    private XmlSuiteUtils() {
        //Utility class. Defeat instantiation.
    }

    /**
     * Creates a cloned copy of the current {@link XmlSuite} if the current suite
     * contains at-least on &lt;test&gt; whose name matches with the provided names.
     *
     * @param xmlSuite  The {@link XmlSuite} to work with.
     * @param testNames The list of testnames to iterate through
     * @return - A {@link XmlSuite} that contains all the tests whose name matched with the provided names.
     * that was passed. If there were no &lt;test&gt; match that was found throws a {@link TestNGException}
     */
    public static XmlSuite cloneIfContainsTestsWithNamesMatchingAny(XmlSuite xmlSuite, List<String> testNames) {
        if (testNames == null || testNames.isEmpty()) {
            throw new TestNGException("Please provide a valid list of names to check.");
        }

        //Search through all the child suites.
        for (XmlSuite suite : xmlSuite.getChildSuites()) {
            XmlSuite clonedSuite = cloneIfSuiteContainTestsWithNamesMatchingAny(suite, testNames);
            if (clonedSuite != null) {
                return clonedSuite;
            }
        }

        //Tests weren't found in child suites. Lets search in the current suite.
        XmlSuite clonedSuite = cloneIfSuiteContainTestsWithNamesMatchingAny(xmlSuite, testNames);

        if (clonedSuite == null) {
            throw new TestNGException("The test(s) <" + testNames.toString() + "> cannot be found.");
        }
        return clonedSuite;
    }

    /**
     * A validator that runs through the list of suites and checks if each of the suites contains
     * any {@link XmlTest} with the same name. If found, then a {@link TestNGException} is raised.
     *
     * @param suites - The list of {@link XmlSuite} to validate.
     */
    public static void validateIfSuitesContainDuplicateTests(List<XmlSuite> suites) {
        for (XmlSuite suite : suites) {
            ensureNoDuplicateTestsArePresent(suite);
            validateIfSuitesContainDuplicateTests(suite.getChildSuites());
        }
    }

    /**
     * Ensure that two XmlSuite don't have the same name
     *
     * @param suites - The List of {@link XmlSuite} that are to be tested and names updated if duplicate
     *               names found.
     */
    public static void adjustSuiteNamesToEnsureUniqueness(List<XmlSuite> suites) {
        adjustSuiteNamesToEnsureUniqueness(suites, Sets.<String>newHashSet());
    }

    /**
     * Ensures that the current suite doesn't contain any duplicate {@link XmlTest} instances.
     * If duplicates are found, then a {@link TestNGException} is raised.
     *
     * @param xmlSuite - The {@link XmlSuite} to work with.
     */
    static void ensureNoDuplicateTestsArePresent(XmlSuite xmlSuite) {
        Set<String> testNames = Sets.newHashSet();
        for (XmlTest test : xmlSuite.getTests()) {
            if (!testNames.add(test.getName())) {
                throw new TestNGException("Two tests in the same suite [" + xmlSuite.getName() + "] "
                        + "cannot have the same name: " + test.getName());
            }
        }
    }

    private static XmlSuite cloneIfSuiteContainTestsWithNamesMatchingAny(XmlSuite suite, List<String> testNames) {
        List<XmlTest> tests = Lists.newLinkedList();
        for (XmlTest xt : suite.getTests()) {
            if (xt.nameMatchesAny(testNames)) {
                tests.add(xt);
            }
        }
        if (tests.isEmpty()) {
            return null;
        }
        return cleanClone(suite, tests);
    }

    private static XmlSuite cleanClone(XmlSuite xmlSuite, List<XmlTest> tests) {
        XmlSuite result = (XmlSuite) xmlSuite.clone();
        result.getTests().clear();
        result.getTests().addAll(tests);
        return result;
    }

    private static void adjustSuiteNamesToEnsureUniqueness(List<XmlSuite> suites, Set<String> names) {
        for (XmlSuite suite : suites) {
            String name = suite.getName();

            int count = 0;
            String tmpName = name;
            while (names.contains(tmpName)) {
                tmpName = name + " (" + count++ + ")";
            }

            if (count > 0) {
                suite.setName(tmpName);
                names.add(tmpName);
            } else {
                names.add(name);
            }

            adjustSuiteNamesToEnsureUniqueness(suite.getChildSuites(), names);
        }
    }
}
