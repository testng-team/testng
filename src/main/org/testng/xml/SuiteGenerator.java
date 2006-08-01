package org.testng.xml;


import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Factory to create custom suites.
 * @author Hani Suleiman
 *         Date: Jul 25, 2005
 *         Time: 1:12:18 PM
 */
public class SuiteGenerator {
  public static LaunchSuite createProxiedXmlSuite(final File xmlSuitePath) {
    return new LaunchSuite.ExistingSuite(xmlSuitePath);
  }

  public static LaunchSuite createCustomizedSuite(final String projectName,
                                                  final Collection<String> packageNames,
                                                  final Collection<String> classNames,
                                                  final Collection<String> methodNames,
                                                  final Collection<String> groupNames,
                                                  final Map<String, String> parameters,
                                                  final String annotationType,
                                                  final int logLevel) {
    if((null != groupNames) && !groupNames.isEmpty()) {
      return new LaunchSuite.ClassListSuite(projectName,
                                            packageNames,
                                            classNames,
                                            groupNames,
                                            parameters,
                                            annotationType,
                                            logLevel);
    }
    else if((classNames != null && classNames.size() > 1) || packageNames != null && packageNames.size() > 0) {
      return new LaunchSuite.ClassListSuite(projectName,
                                            packageNames,
                                            classNames,
                                            groupNames,
                                            parameters,
                                            annotationType,
                                            logLevel);
    }
    else {
      return new LaunchSuite.MethodsSuite(projectName,
                                          classNames.iterator().next(),
                                          methodNames,
                                          parameters,
                                          annotationType,
                                          logLevel);
    }
  }
}
