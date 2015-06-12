package org.testng.internal;

import java.util.Map;
import java.util.Properties;

import org.testng.ITestResult;
import org.testng.collections.Maps;



/**
 * Constants used by TestNG
 *
 * @author Cedric Beust, May 2, 2004
 *
 */
public class Constants {
  private static final String NAMESPACE = "testng";

////////  //
  // Properties
  //
  public static final String PROP_OUTPUT_DIR = NAMESPACE + "." + "outputDir";
//  public static final String PROP_NAME = NAMESPACE + "." + "name";
//  public static final String PROP_INCLUDED_GROUPS = NAMESPACE + "." + "includedGroups";
//  public static final String PROP_EXCLUDED_GROUPS = NAMESPACE + "." + "excludedGroups";
//  public static final String PROP_CLASS_NAMES = NAMESPACE + "." + "classNames";
//  public static final String PROP_VERBOSE = NAMESPACE + "." + "verbose";
//  public static final String PROP_JUNIT= NAMESPACE + "." + "junit";
//  public static final String PROP_QUIET= NAMESPACE + "." + "quiet";
//  public static final String PROP_GROUP= NAMESPACE + "." + "group";

  private static final TestNGProperty[] COMMAND_LINE_PARAMETERS = {
    new TestNGProperty("-d", PROP_OUTPUT_DIR, "Directory where the result files will be created.", "test-output"),
  };

  private static final Map<String, TestNGProperty> m_propertiesByName = Maps.newHashMap();

  static {
//    for (int i = 0; i < PROPERTIES.length; i++) {
//      m_propertiesByName.put(PROPERTIES[i].getName(), PROPERTIES[i]);
//    }
    for (TestNGProperty element : COMMAND_LINE_PARAMETERS) {
      m_propertiesByName.put(element.getName(), element);
    }
  }

  private static TestNGProperty getProperty(String propertyName) {
    TestNGProperty result = m_propertiesByName.get(propertyName);
    assert null != result : "Unknown property : " + propertyName;

    return result;
  }

  public static String getPropertyValue(Properties p, String propertyName) {
    TestNGProperty r= getProperty(propertyName);
    assert null != r : "Unknown property : " + propertyName;

    String result = p.getProperty(r.getName());

    return result;
  }

  public static boolean getBooleanPropertyValue(Properties properties, String propertyName) {
    TestNGProperty p = getProperty(propertyName);
    String r = properties.getProperty(propertyName, p.getDefault());
    boolean result = "true".equalsIgnoreCase(r);

    return Boolean.valueOf(result);
  }

  public static int getIntegerPropertyValue(Properties properties, String propertyName) {
    TestNGProperty p = getProperty(propertyName);
    String r = properties.getProperty(propertyName, p.getDefault());
    int result = Integer.parseInt(r);

    return result;
  }

  public static String getDefaultValueFor(String propertyName) {
    TestNGProperty p = getProperty(propertyName);
    return p.getDefault();
  }

  public static String displayStatus(int status) {
    if (ITestResult.SKIP == status) {
      return "SKIP";
    } else if (ITestResult.SUCCESS == status) {
      return "SUCCESS";
    } else if (ITestResult.FAILURE == status) {
      return "FAILURE";
    } else {
      return "UNKNOWN_STATUS";
    }
  }

}
