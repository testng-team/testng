package org.testng.xml.internal;

import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Sets;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;
import java.util.Set;

/** A utility class to work with {@link XmlSuite} */
public final class XmlSuiteUtils {

  private XmlSuiteUtils() {
    // Utility class. Defeat instantiation.
  }

  /**
   * A validator that runs through the list of suites and checks if each of the suites contains any
   * {@link XmlTest} with the same name. If found, then a {@link TestNGException} is raised.
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
   * @param suites - The List of {@link XmlSuite} that are to be tested and names updated if
   *     duplicate names found.
   */
  public static void adjustSuiteNamesToEnsureUniqueness(List<XmlSuite> suites) {
    adjustSuiteNamesToEnsureUniqueness(suites, Sets.newHashSet());
  }

  public static XmlSuite newXmlSuiteUsing(List<String> classes) {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setVerbose(0);
    xmlSuite.setName("Jar suite");
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setXmlClasses(constructXmlClassesUsing(classes));
    return xmlSuite;
  }

  /**
   * Ensures that the current suite doesn't contain any duplicate {@link XmlTest} instances. If
   * duplicates are found, then a {@link TestNGException} is raised.
   *
   * @param xmlSuite - The {@link XmlSuite} to work with.
   */
  static void ensureNoDuplicateTestsArePresent(XmlSuite xmlSuite) {
    Set<String> testNames = Sets.newHashSet();
    for (XmlTest test : xmlSuite.getTests()) {
      if (!testNames.add(test.getName())) {
        throw new TestNGException(
            "Two tests in the same suite ["
                + xmlSuite.getName()
                + "] "
                + "cannot have the same name: "
                + test.getName());
      }
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
