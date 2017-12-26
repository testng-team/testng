package org.testng.xml.internal;

import java.util.Iterator;
import java.util.List;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The class to work with "-testnames"
 *
 */
public final class TestNamesMatcher {
    
    private final List<XmlSuite> cloneSuites = Lists.newArrayList();
    private final List<String> matchedTestNames = Lists.newArrayList();
    private final List<XmlTest> matchedTests = Lists.newArrayList();
    private final List<String> testNames;
    
    public TestNamesMatcher(XmlSuite xmlSuite, List<String> testNames){
        this.testNames = testNames;
        cloneIfContainsTestsWithNamesMatchingAny(xmlSuite, this.testNames);        
    }

    /**
     * Recursive search the given testNames from the current {@link XmlSuite} and its child suites.
     *
     * @param xmlSuite  The {@link XmlSuite} to work with.
     * @param testNames The list of testnames to iterate through
     */
    private void cloneIfContainsTestsWithNamesMatchingAny(XmlSuite xmlSuite, List<String> testNames) {
        if (testNames == null || testNames.isEmpty()) {
            throw new TestNGException("Please provide a valid list of names to check.");
        }
        
        //Start searching in the current suite.
        addIfNotNull(cloneIfSuiteContainTestsWithNamesMatchingAny(xmlSuite));
        
        //Search through all the child suites.
        for (XmlSuite suite : xmlSuite.getChildSuites()) {
            cloneIfContainsTestsWithNamesMatchingAny(suite, testNames);
        }
    }
    
    public List<XmlSuite> getSuitesMatchingTestNames() {
        return cloneSuites;
    }

    /**
     * Getting miss-matched testNames
     */
    public List<String> getMissMatchedTestNames(){
        List<String> tmpTestNames = Lists.newArrayList();
        tmpTestNames.addAll(testNames);
        Iterator<String> testNameIterator = tmpTestNames.iterator();
        while (testNameIterator.hasNext()) {
            String testName = testNameIterator.next();
            if (matchedTestNames.contains(testName)) {
                testNameIterator.remove();
            }
        }
        return tmpTestNames;        
    }

    public List<XmlTest> getMatchedTests() {
        return matchedTests;
    }
    
    private void addIfNotNull(XmlSuite xmlSuite) {
        if (xmlSuite != null) {
            cloneSuites.add(xmlSuite);
        }
    }

    private XmlSuite cloneIfSuiteContainTestsWithNamesMatchingAny(XmlSuite suite) {
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

}
