package org.testng.xml.internal;

import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A utility class that exposes helper methods to work with {@link XmlSuite}
 */
public final class XmlSuiteUtils {
    private List<XmlSuite> cloneSuites = Lists.newArrayList();
    private List<String> matchedTestNames = Lists.newArrayList();
    private List<XmlTest> matchedTests = Lists.newArrayList();

    /**
     * Recursive search the given testNames from the current {@link XmlSuite} and its child suites.
     *
     * @param xmlSuite  The {@link XmlSuite} to work with.
     * @param testNames The list of testnames to iterate through
     */
    public void cloneIfContainsTestsWithNamesMatchingAny(XmlSuite xmlSuite, List<String> testNames) {
        if (testNames == null || testNames.isEmpty()) {
            throw new TestNGException("Please provide a valid list of names to check.");
        }
        
        //Start searching in the current suite.
        addIfNotNull(cloneIfSuiteContainTestsWithNamesMatchingAny(xmlSuite, testNames));
        
        //Search through all the child suites.
        for (XmlSuite suite : xmlSuite.getChildSuites()) {
            cloneIfContainsTestsWithNamesMatchingAny(suite, testNames);
        }
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

    public static XmlSuite newXmlSuiteUsing(List<String> classes) {
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setVerbose(0);
        xmlSuite.setName("Jar suite");
        XmlTest xmlTest = new XmlTest(xmlSuite);
        xmlTest.setXmlClasses(constructXmlClassesUsing(classes));
        return xmlSuite;
    }
    
    public List<XmlSuite> getCloneSuite() {
        return cloneSuites;
    }

    /**
     * @param testNames input from m_testNames
     * 
     */
    public List<String> getMissMatchedTestNames(List<String> testNames){
        Iterator<String> testNameIterator = testNames.iterator();
        while (testNameIterator.hasNext()) {
            String testName = testNameIterator.next();
            if (matchedTestNames.contains(testName)) {
                testNameIterator.remove();
            }
        }
        return testNames;
        
    }

    public List<XmlTest> getMatchedTests() {
        return matchedTests;
    }

    public List<String> getMatchedTestNames() {
        return matchedTestNames;
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
    
    private void addIfNotNull(XmlSuite xmlSuite) {
        if (xmlSuite != null) {
            cloneSuites.add(xmlSuite);
        }
    }

    private static List<XmlClass> constructXmlClassesUsing(List<String> classes) {
        List<XmlClass> xmlClasses = Lists.newLinkedList();
        for (String cls : classes) {
            XmlClass xmlClass = new XmlClass(cls);
            xmlClasses.add(xmlClass);
        }
        return xmlClasses;
    }

    private XmlSuite cloneIfSuiteContainTestsWithNamesMatchingAny(XmlSuite suite, List<String> testNames) {
        List<XmlTest> tests = Lists.newLinkedList();
        for (XmlTest xt : suite.getTests()) {
            if (xt.nameMatchesAny(testNames)) {
                tests.add(xt);
                matchedTestNames.add(xt.getName());
                matchedTests.add(xt);
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
