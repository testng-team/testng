package org.testng.internal.config;

import org.testng.*;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.internal.ClassHelper;
import org.testng.internal.Defense;
import org.testng.internal.Utils;
import org.testng.internal.version.VersionInfo;
import org.testng.log4testng.Logger;

import java.util.*;

/**
 * Implementation of {@link ITestNGConfiguration} specifically designed to
 * parse parameters from a {@link java.util.Map} instance.
 *
 * @since 5.6
 */
public class MapConfigurationParser implements ITestNGConfiguration {

  private static final Logger LOGGER = Logger.getLogger(MapConfigurationParser.class);

  // Set of properties originally given to parser
  Map m_props;
  // ClassLoader to use to resolve test classes
  ClassLoader m_classLoader;
  // Classpath directory to use
  String m_classPath;

  // Generated reports output directory
  String m_outputDirectory;
  // Comma separated groups list
  String m_groups;
  // Comma separated excluded groups list
  String m_excludedGroups;
  // Logging level
  int m_logVerbosity = 0;
  // Whether or not running against junit tests
  boolean m_isJunit = false;
  // Type of annotations
  AnnotationTypeEnum m_annotationType = VersionInfo.getDefaultAnnotationType();
  // Testrunner factory
  Class m_testRunnerFactory;
  // Object factory
  Class m_objectFactory;
  // Listeners
  List<Class> m_listeners;
  // Object instances of listeners
  List<ITestListener> m_listenerObjects;
  // Suite listener instances
  List<ISuiteListener> m_suiteListeners;
  // Test classes
  List<Class> m_testClasses;
  // Test jar path
  String m_testJar;
  // Test source path
  String m_sourcePath;
  // Host IP address
  String m_hostAddress;
  // Socket port
  int m_port;
  // Slave properties file configuration path
  String m_slavePropertiesPath;
  // Master properties file configuration path
  String m_masterPropertiesPath;
  // Parallel mode
  String m_parallelMode;
  // Thread count
  int m_threadCount;
  // Whether or not to use the default listeners
  boolean m_useDefaultListeners = false;
  // Suite name
  String m_suiteName;
  // Test name
  String m_testName;
  // Reporters
  List<ReporterConfig> m_reporters;
  // XML Suite definitions
  List<String> m_xmlSuites;
  // Configured single test to run
  String m_testSetName;

  private int m_lastGoodRootIndex = -1;

  public void configure(TestNG test) {

    Defense.notNull(test, "test");

    test.setOutputDirectory(m_outputDirectory);
    test.setSourcePath(m_sourcePath);
    test.setGroups(m_groups);
    test.setExcludedGroups(m_excludedGroups);
    test.setVerbose(m_logVerbosity);
    test.setJUnit(m_isJunit);
    test.setAnnotations(m_annotationType.toString());

    if (m_testClasses != null)
      test.setTestClasses((Class[])m_testClasses.toArray(new Class[m_testClasses.size()]));
    if (m_xmlSuites != null)
      test.setTestSuites(m_xmlSuites);
    
    test.setUseDefaultListeners(m_useDefaultListeners);
    if (m_objectFactory != null)
      test.setObjectFactory(m_objectFactory);
    if (m_testJar != null)
      test.setTestJar(m_testJar);
    if (m_masterPropertiesPath != null)
      test.setMaster(m_masterPropertiesPath);
    if (m_slavePropertiesPath != null)
      test.setSlave(m_slavePropertiesPath);

    if (m_parallelMode != null)
      test.setParallel(m_parallelMode);
    if (m_threadCount > 0)
      test.setThreadCount(m_threadCount);
    if (m_suiteName != null)
      test.setDefaultSuiteName(m_suiteName);
    if (m_testName != null)
      test.setDefaultTestName(m_testName);
    if (m_listeners != null)
      test.setListenerClasses(m_listeners);
    if (m_listenerObjects != null) {

      for (ITestListener listener : m_listenerObjects)
        test.addListener(listener);
    }
    if (m_suiteListeners != null) {

      for (ISuiteListener listener : m_suiteListeners)
        test.addListener(listener);
    }

    if (m_reporters != null) {
      for (ReporterConfig reporter : m_reporters) {
        
        test.addReporter(reporter);
      }
    }
  }

  @SuppressWarnings("all")
  public void load(Map config) {

    Defense.notNull(config, "config");
    m_props = config;

    Iterator it = config.keySet().iterator();
    while (it.hasNext()) {

      String key = (String) it.next();
      Object value = m_props.get(key);

      LOGGER.debug("configuration key: \"" + key + "\" = \"" + value + "\"");

      // handle each kind of configuration

      if (OUTPUT_DIRECTORy.equalsIgnoreCase(key)) {

        m_outputDirectory = (String)value;

      } else if (GROUPS.equalsIgnoreCase(key)) {

        m_groups = (String)value;

      } else if (EXCLUDED_GROUPS.equalsIgnoreCase(key)) {

        m_excludedGroups = (String)value;

      } else if (LOG_LEVEL.equalsIgnoreCase(key)) {

        m_logVerbosity = Integer.valueOf((String)value);

      } else if (JUNIT.equalsIgnoreCase(key)) {

        m_isJunit = Boolean.valueOf((String)value);

      } else if (ANNOTATION_TYPE.equalsIgnoreCase(key)) {

        AnnotationTypeEnum type = AnnotationTypeEnum.valueOf((String)value);
        if (type != null)
          m_annotationType = type;

      } else if (TESTRUNNER_FACTORY.equalsIgnoreCase(key)) {

        m_testRunnerFactory = fileToClass((String)value);

      } else if (OBJECT_FACTORY.equalsIgnoreCase(key)) {

        m_objectFactory = fileToClass((String)value);

      } else if (TEST_LISTENER.equalsIgnoreCase(key)) {

        if (ITestListener.class.isInstance(value)) {

          m_listenerObjects = new ArrayList<ITestListener>();
          m_listenerObjects.add((ITestListener)value);
        } else if (Collection.class.isInstance(value)) {

          m_listenerObjects = new ArrayList<ITestListener>();
          m_listenerObjects.addAll((Collection)value);
        } else {

          m_listeners = csvToClassList((String)value);
        }

      } else if (SUITE_LISTENER.equalsIgnoreCase(key)) {

        if (ISuiteListener.class.isInstance(value)) {

          m_suiteListeners = new ArrayList<ISuiteListener>();
          m_suiteListeners.add((ISuiteListener)value);
        } else if (Collection.class.isInstance(value)) {

          m_suiteListeners = new ArrayList<ISuiteListener>();
          m_suiteListeners.addAll((Collection)value);
        }

      } else if (TEST_CLASSES.equalsIgnoreCase(key)) {

        m_testClasses = csvToClassList((String)value);

      } else if (TEST_JAR.equalsIgnoreCase(key)) {

        m_testJar = (String)value;

      } else if (SOURCE_DIRECTORY.equalsIgnoreCase(key)) {

        m_sourcePath = (String)value;

      } else if (HOST.equalsIgnoreCase(key)) {

        m_hostAddress = (String)value;

      } else if (PORT.equalsIgnoreCase(key)) {

        m_port = Integer.valueOf((String)value);

      } else if (SLAVE_PROPERTIES.equalsIgnoreCase(key)) {

        m_slavePropertiesPath = (String)value;

      } else if (MASTER_PROPERTIES.equalsIgnoreCase(key)) {

        m_masterPropertiesPath = (String)value;

      } else if (PARALLEL_MODE.equalsIgnoreCase(key)) {

        m_parallelMode = (String)value;

      } else if (THREAD_COUNT.equalsIgnoreCase(key)) {

        m_threadCount = Integer.valueOf((String)value);

      } else if (USE_DEFAULT_LISTENERS.equalsIgnoreCase(key)) {

        m_useDefaultListeners = Boolean.valueOf((String)value);

      } else if (DEFAULT_SUITE_NAME.equalsIgnoreCase(key)) {

        m_suiteName = (String)value;

      } else if (DEFAULT_TEST_NAME.equalsIgnoreCase(key)) {

        m_testName = (String)value;

      } else if (REPORTER.equalsIgnoreCase(key)) {

        m_reporters = new ArrayList<ReporterConfig>();
        m_reporters.add(ReporterConfig.deserialize((String)value));

      } else if (XML_SUITES.equalsIgnoreCase(key)) {

        String[] files = Utils.split((String)value, ",");
        m_xmlSuites = new ArrayList<String>();

        for (String file : files) {

          if (file.endsWith(".xml"))
            m_xmlSuites.add(file);
        }
      } else if (TEST_CLASSPATH.equalsIgnoreCase(key)) {

        m_classPath = (String) value;

      } else if (CLASS_LOADER.equalsIgnoreCase(key)) {

        m_classLoader = (ClassLoader) value;
        
      } else {

        LOGGER.warn("Unknown configuration key with [" + key + " = " + value + "]");

      }
    }
  }

  /**
   * Converts the given comma separated list of class names into class
   * instances.
   *
   * @param value
   *          The comma separated value list of class names.
   * @return The resolved classes. May be empty if none were resolvable.
   */
  List<Class> csvToClassList(String value) {

    String[] names = Utils.split(value, ",");
    List<Class> ret = new ArrayList<Class>();

    for (String className : names) {

      Class clazz = fileToClass(className);
      if (clazz != null)
        ret.add(clazz);
    }

    return ret;
  }

  /**
   * Returns the Class object corresponding to the given name. The name may be
   * of the following form:
   * <ul>
   * <li>A class name: "org.testng.TestNG"</li>
   * <li>A class file name: "/testng/src/org/testng/TestNG.class"</li>
   * <li>A class source name: "d:\testng\src\org\testng\TestNG.java"</li>
   * </ul>
   *
   * @param file
   *          the class name.
   * @return the class corresponding to the name specified.
   */
  private Class fileToClass(String file) {
    Class result = null;

    if(!file.endsWith(".class") && !file.endsWith(".java")) {
      // Doesn't end in .java or .class, assume it's a class name
      result = ClassHelper.forName(file);

      if (null == result) {
        throw new TestNGException("Cannot load class from file: " + file);
      }

      return result;
    }

    int classIndex = file.lastIndexOf(".class");
    if (-1 == classIndex) {

      classIndex = file.lastIndexOf(".java");
    }

    // Transforms the file name into a class name.

    // Remove the ".class" or ".java" extension.
    String shortFileName = file.substring(0, classIndex);

    // Split file name into segments. For example "c:/java/classes/com/foo/A"
    // becomes {"c:", "java", "classes", "com", "foo", "A"}
    String[] segments = shortFileName.split("[/\\\\]", -1);

    //
    // Check if the last good root index works for this one. For example, if the previous
    // name was "c:/java/classes/com/foo/A.class" then m_lastGoodRootIndex is 3 and we
    // try to make a class name ignoring the first m_lastGoodRootIndex segments (3). This
    // will succeed rapidly if the path is the same as the one from the previous name.
    //
    if (-1 != m_lastGoodRootIndex) {

      // TODO use a SringBuffer here
      String className = segments[m_lastGoodRootIndex];
      for (int i = m_lastGoodRootIndex + 1; i < segments.length; i++) {
        className += "." + segments[i];
      }

      result = ClassHelper.forName(className);

      if (null != result) {
        return result;
      }
    }

    //
    // We haven't found a good root yet, start by resolving the class from the end segment
    // and work our way up.  For example, if we start with "c:/java/classes/com/foo/A"
    // we'll start by resolving "A", then "foo.A", then "com.foo.A" until something
    // resolves.  When it does, we remember the path we are at as "lastGoodRoodIndex".
    //

    StringBuffer className = new StringBuffer();
    for (int i = segments.length - 1; i >= 0; i--) {

      if (className.length() > 0) {

        className.insert(0, segments[i] + ".");
      } else {

        className.insert(0, segments[i]);
      }
      
      result = ClassHelper.forName(className.toString());

      if (null != result) {
        
        m_lastGoodRootIndex = i;
        break;
      }
    }

    if (null == result) {
      throw new TestNGException("Cannot load class from file: " + file);
    }

    return result;
  }

}
