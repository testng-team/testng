package org.testng.xml;


import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Factory to create custom suites.
 * @author Hani Suleiman
 *         Date: Jul 25, 2005
 *         Time: 1:12:18 PM
 */
public class SuiteGenerator {
  private static final Collection<String> EMPTY_CLASS_LIST= Collections.emptyList();
  
  public static LaunchSuite createProxiedXmlSuite(final File xmlSuitePath) {
    return new LaunchSuite.ExistingSuite(xmlSuitePath);
  }

  public static LaunchSuite createSuite(final String projectName,
                                        final Collection<String> packageNames,
                                        final Map<String, Collection<String>> classAndMethodNames,
                                        final Collection<String> groupNames,
                                        final Map<String, String> parameters,
                                        final String annotationType,
                                        final int logLevel) {
    Collection<String> classes= classAndMethodNames != null ? classAndMethodNames.keySet() : EMPTY_CLASS_LIST;
    if((null != groupNames) && !groupNames.isEmpty()) {
      return new LaunchSuite.ClassListSuite(projectName,
                                            packageNames,
                                            classes,
                                            groupNames,
                                            parameters,
                                            annotationType,
                                            logLevel);
    }
    else if(packageNames != null && packageNames.size() > 0) {
      return new LaunchSuite.ClassListSuite(projectName,
                                            packageNames,
                                            classes,
                                            groupNames,
                                            parameters,
                                            annotationType,
                                            logLevel);
    }
    else {
      return new LaunchSuite.ClassesAndMethodsSuite(projectName,
                                                    classAndMethodNames,
                                                    parameters,
                                                    annotationType,
                                                    logLevel);
    }
  }

  /**
   * @deprecated use {@link #createSuite(String, java.util.Collection, java.util.Map, java.util.Collection, java.util.Map, String, int)} instead.
   */
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
