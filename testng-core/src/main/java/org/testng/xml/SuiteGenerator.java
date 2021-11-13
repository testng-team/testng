package org.testng.xml;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Factory to create custom suites.
 *
 * @author Hani Suleiman Date: Jul 25, 2005 Time: 1:12:18 PM
 */
public class SuiteGenerator {
  private static final Collection<String> EMPTY_CLASS_LIST = Collections.emptyList();

  public static LaunchSuite createProxiedXmlSuite(final File xmlSuitePath) {
    return new LaunchSuite.ExistingSuite(xmlSuitePath);
  }

  public static LaunchSuite createSuite(
      String projectName,
      Collection<String> packageNames,
      Map<String, Collection<String>> classAndMethodNames,
      Collection<String> groupNames,
      Map<String, String> parameters,
      String annotationType,
      int logLevel) {

    LaunchSuite result;
    Collection<String> classes =
        classAndMethodNames != null ? classAndMethodNames.keySet() : EMPTY_CLASS_LIST;
    if ((null != groupNames) && !groupNames.isEmpty()) {
      //
      // Create a suite from groups
      //
      result =
          new LaunchSuite.ClassListSuite(
              projectName, packageNames, classes, groupNames, parameters, annotationType, logLevel);
    } else if (packageNames != null && packageNames.size() > 0) {
      //
      // Create a suite from packages
      //
      result =
          new LaunchSuite.ClassListSuite(
              projectName, packageNames, classes, groupNames, parameters, annotationType, logLevel);
    } else {
      //
      // Default suite creation
      //
      result =
          new LaunchSuite.ClassesAndMethodsSuite(
              projectName, classAndMethodNames, parameters, annotationType, logLevel);
    }

    return result;
  }
}
