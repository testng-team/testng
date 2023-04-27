package org.testng.xml.internal;

import java.util.List;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.RuntimeBehavior;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The class to work with "-testnames", "-ignoreMissedTestNames", and VM argument
 * "-Dtestng.ignore.missed.testnames". If both "-ignoreMissedTestNames" and VM argument "-Dtestng.ignore.missed.testnames" are set, then either of them has "true" value will enable the feature to ingore partially missed test names and run those existing test names.
 */
public final class TestNamesMatcher {

  private static final Logger LOGGER = Logger.getLogger(TestNamesMatcher.class);

  private final List<XmlSuite> cloneSuites = Lists.newArrayList();
  private final List<String> matchedTestNames = Lists.newArrayList();
  private final List<XmlTest> matchedTests = Lists.newArrayList();
  private final List<String> testNames;

  public TestNamesMatcher(XmlSuite xmlSuite, List<String> testNames) {
    this.testNames = testNames;
    cloneIfContainsTestsWithNamesMatchingAny(xmlSuite, this.testNames);
  }

  /**
   * Recursive search the given testNames from the current {@link XmlSuite} and its child suites.
   *
   * @param xmlSuite The {@link XmlSuite} to work with.
   * @param testNames The list of testnames to iterate through
   */
  private void cloneIfContainsTestsWithNamesMatchingAny(XmlSuite xmlSuite, List<String> testNames) {
    if (testNames == null || testNames.isEmpty()) {
      throw new TestNGException("Please provide a valid list of names to check.");
    }

    // Start searching in the current suite.
    addIfNotNull(cloneIfSuiteContainTestsWithNamesMatchingAny(xmlSuite));

    // Search through all the child suites.
    for (XmlSuite suite : xmlSuite.getChildSuites()) {
      cloneIfContainsTestsWithNamesMatchingAny(suite, testNames);
    }
  }

  public List<XmlSuite> getSuitesMatchingTestNames() {
    return cloneSuites;
  }

  /**
   * Do validation for testNames and notify users if any testNames are missed in suite. This method
   * is also used to decide how to run test suite when test names are given. In legacy logic, if
   * test names are given and exist in suite, then run them; if any of them do not exist in suite,
   * then throw exception and exit. After ignoreMissedTestNames is introduced, if
   * ignoreMissedTestNames is enabled, then any of the given test names exist in suite will be run,
   * and print warning message to tell those test names do not exist in suite.
   *
   * @param ignoreMissedTestNames if true print warning message otherwise throw TestNGException for
   *     missed testNames.
   * @return boolean if ignoreMissedTestNames disabled, then return true if no missed test names in
   *     suite, otherwise throw TestNGException; if ignoreMissedTestNames enabled, then return true
   *     if any test names exist in suite, otehrwise (all given test names are missed) throw TestNGException.
   */
  public boolean validateMissMatchedTestNames(final boolean ignoreMissedTestNames) {
    final List<String> missedTestNames = getMissedTestNames();
    if (!missedTestNames.isEmpty()) {
      final String errMsg = "The test(s) <" + missedTestNames + "> cannot be found in suite.";
	  final boolean enabledIgnoreMissedTestNames = (ignoreMissedTestNames || RuntimeBehavior.ignoreMissedTestNames());
      if (enabledIgnoreMissedTestNames && !matchedTestNames.isEmpty()) {
        LOGGER.warn(errMsg);
        // as long as any test names match, then tell caller to run them.
        return true;
      } else {
        // legacy, throw exception and exit execution
        throw new TestNGException(errMsg);
      }
    }
    return missedTestNames.isEmpty() && !matchedTestNames.isEmpty();
  }
  
  public boolean validateMissMatchedTestNames() {
	  return validateMissMatchedTestNames(false);
  }

  public List<String> getMissedTestNames() {
    List<String> missedTestNames = Lists.newArrayList();
    missedTestNames.addAll(testNames);
    missedTestNames.removeIf(matchedTestNames::contains);
    return missedTestNames;
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
