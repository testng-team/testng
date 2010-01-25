package org.testng.xml;


import org.testng.collections.Lists;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.log4testng.Logger;
import org.testng.reporters.XMLStringBuffer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Wrapper for real suites and custom configured suites.
 * Should only be used for integration purposes; creating a custom testng.xml
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public abstract class LaunchSuite {
  /** This class's log4testng Logger. */
  private static final Logger LOGGER = Logger.getLogger(LaunchSuite.class);
  
  protected boolean m_temporary;
  
  /**
   * Constructs a <code>LaunchSuite</code>
   *
   * @param isTemp the temporary status
   */
  protected LaunchSuite(boolean isTemp) {
    m_temporary = isTemp;
  }
  
  /**
   * Returns the temporary state.
   * @return the temporary state.
   */
  public boolean isTemporary() {
    return m_temporary;
  }
  
  /**
   * Saves the suite file in the specified directory and returns the file 
   * pathname.
   *
   * @param directory the directory where the suite file is to be saved.
   * @return the file pathname of the saved file.
   */
  public abstract File save(File directory);

  /**
   * <code>ExistingSuite</code> is a non-temporary LaunchSuite based on an existing 
   * file.
   */
  public static class ExistingSuite extends LaunchSuite {
    
    /**
     * The existing suite path (either relative to the project root or an absolute path)
     */
    private File m_suitePath;

    /**
     * Constructs a <code>ExistingSuite</code> based on an existing file 
     *
     * @param path the path to the existing Launch suite.
     */
    public ExistingSuite(File path) {
      super(false);
      
      m_suitePath = path;
    }

    /**
     * {@inheritDoc} This implementation saves nothing because the suite file already 
     * exists.
     */
    @Override
    public File save(File directory) {
      return m_suitePath;
    }
  }

  /**
   * <code>CustomizedSuite</code> TODO cquezel JavaDoc.
   */
  private abstract static class CustomizedSuite extends LaunchSuite {
    protected String m_projectName;
    protected String m_suiteName;
    
    /** The annotation type. May be null. */
    protected AnnotationTypeEnum m_annotationType;
    
    protected Map<String, String> m_parameters;
    
    /** The string buffer used to write the XML file. */
    private XMLStringBuffer m_suiteBuffer;
    
    /**
     * Constructs a <code>CustomizedSuite</code> TODO cquezel JavaDoc.
     *
     * @param projectName
     * @param className
     * @param parameters
     * @param annotationType
     */
    private CustomizedSuite(final String projectName,
        final String className,
        final Map<String, String> parameters,
        final String annotationType) 
    {
      super(true);
      
      m_projectName = projectName;
      m_suiteName = className;
      m_parameters = parameters;
      m_annotationType = annotationType == null ? null : AnnotationTypeEnum.valueOf(annotationType, false);
    }

    /**
     * TODO cquezel JavaDoc
     *
     * @return
     */
    protected XMLStringBuffer createContentBuffer() {
      XMLStringBuffer suiteBuffer = new XMLStringBuffer(""); //$NON-NLS-1$
      suiteBuffer.setDocType("suite SYSTEM \"" + Parser.TESTNG_DTD_URL + "\"");
      
      Properties attrs = new Properties();
      attrs.setProperty("parallel", XmlSuite.PARALLEL_NONE);
      attrs.setProperty("name", m_suiteName);
      suiteBuffer.push("suite", attrs);
      
      if (m_parameters != null) {
        for (Map.Entry<String, String> entry : m_parameters.entrySet()) {
          Properties paramAttrs = new Properties();
          paramAttrs.setProperty("name", entry.getKey());
          paramAttrs.setProperty("value", entry.getValue());
          suiteBuffer.push("parameter", paramAttrs);
          suiteBuffer.pop("parameter");
        }
      }
      
      initContentBuffer(suiteBuffer);
      
      suiteBuffer.pop("suite");
      
      return suiteBuffer;
    }

    /**
     * TODO cquezel JavaDoc
     *
     * @return
     */
    private XMLStringBuffer getSuiteBuffer() {
      if (null == m_suiteBuffer) {
        m_suiteBuffer = createContentBuffer();
      }
      
      return m_suiteBuffer;
    }

    /**
     * Initializes the content of the xml string buffer.  
     *
     * @param suiteBuffer the string buffer to initialize.
     */
    protected abstract void initContentBuffer(XMLStringBuffer suiteBuffer);
    
    /**
     * {@inheritDoc} This implementation saves the suite to the "temp-testng-customsuite.xml" 
     * file in the specified directory. 
     */
    @Override
    public File save(File directory) {
      final File suiteFile = new File(directory, "temp-testng-customsuite.xml");
      
      saveSuiteContent(suiteFile, getSuiteBuffer());
      
      return suiteFile;
    }
    
    /**
     * Saves the content of the string buffer to the specified file.
     *
     * @param file the file to write to.
     * @param content the content to write to the file.
     */
    protected void saveSuiteContent(final File file, final XMLStringBuffer content) {
      
      try {
        FileWriter fw = new FileWriter(file);
        try {
          fw.write(content.getStringBuffer().toString());
        }
        finally {
          fw.close();
        }
      }
      catch (IOException ioe) {
        // TODO CQ is this normal to swallow exception here 
        LOGGER.error("IO Exception", ioe);
      }
    }
  }

  /**
   * A <code>MethodsSuite</code> is a suite made up of methods. 
   */
  static class MethodsSuite extends CustomizedSuite {
    protected Collection<String> m_methodNames;
    protected String m_className;
    protected int m_logLevel;
    
    /**
     * Constructs a <code>MethodsSuite</code> TODO cquezel JavaDoc.
     *
     * @param projectName
     * @param className
     * @param methodNames
     * @param parameters
     * @param annotationType (may be null)
     * @param logLevel
     */
    MethodsSuite(final String projectName,
        final String className,
        final Collection<String> methodNames,
        final Map<String, String> parameters,
        final String annotationType,
        final int logLevel) {
      super(projectName, className, parameters, annotationType);
      
      m_className = className;
      m_methodNames = methodNames;
      m_logLevel = logLevel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initContentBuffer(XMLStringBuffer suiteBuffer) {
      Properties testAttrs = new Properties();
      testAttrs.setProperty("name", m_className);
      if (m_annotationType != null) {
        testAttrs.setProperty("annotations", m_annotationType.getName());
      }
      testAttrs.setProperty("verbose", String.valueOf(m_logLevel));
      
      suiteBuffer.push("test", testAttrs);
      
      suiteBuffer.push("classes");
      
      Properties classAttrs = new Properties();
      classAttrs.setProperty("name", m_className);
      
      if ((null != m_methodNames) && (m_methodNames.size() > 0)) {
        suiteBuffer.push("class", classAttrs);
        
        suiteBuffer.push("methods");
        
        for (Object methodName : m_methodNames) {
          Properties methodAttrs = new Properties();
          methodAttrs.setProperty("name", (String) methodName);
          suiteBuffer.addEmptyElement("include", methodAttrs);
        }
        
        suiteBuffer.pop("methods");
        suiteBuffer.pop("class");
      }
      else {
        suiteBuffer.addEmptyElement("class", classAttrs);
      }
      suiteBuffer.pop("classes");
      suiteBuffer.pop("test");
    }
  }

  static class ClassesAndMethodsSuite extends CustomizedSuite {
    protected Map<String, Collection<String>> m_classes;
    protected int m_logLevel;
    
    ClassesAndMethodsSuite(final String projectName,
        final Map<String, Collection<String>> classes,
        final Map<String, String> parameters,
        final String annotationType,
        final int logLevel) {
      super(projectName, "Custom suite", parameters, annotationType);
      m_classes = classes;
      m_logLevel = logLevel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initContentBuffer(XMLStringBuffer suiteBuffer) {
      Properties testAttrs = new Properties();
      testAttrs.setProperty("name", m_projectName);
      if (m_annotationType != null) {
        testAttrs.setProperty("annotations", m_annotationType.getName());
      }
      testAttrs.setProperty("verbose", String.valueOf(m_logLevel));
      
      suiteBuffer.push("test", testAttrs);
      
      suiteBuffer.push("classes");
      
      for(Map.Entry<String, Collection<String>> entry : m_classes.entrySet()) {
        Properties classAttrs = new Properties();
        classAttrs.setProperty("name", entry.getKey());
        
        Collection<String> methodNames= sanitize(entry.getValue());
        if ((null != methodNames) && (methodNames.size() > 0)) {
          suiteBuffer.push("class", classAttrs);
          
          suiteBuffer.push("methods");
          
          for (String methodName : methodNames) {
            Properties methodAttrs = new Properties();
            methodAttrs.setProperty("name", methodName);
            suiteBuffer.addEmptyElement("include", methodAttrs);
          }
          
          suiteBuffer.pop("methods");
          suiteBuffer.pop("class");
        }
        else {
          suiteBuffer.addEmptyElement("class", classAttrs);
        }
      }
      suiteBuffer.pop("classes");
      suiteBuffer.pop("test");
    }
    
    private Collection<String> sanitize(Collection<String> source) {
      if(null == source) return null;
      
      List<String> result= Lists.newArrayList();
      for(String name: source) {
        if(!"".equals(name)) {
          result.add(name);
        }
      }
      
      return result;
    }
  }
  
  /**
   * <code>ClassListSuite</code> TODO cquezel JavaDoc.
   */
  static class ClassListSuite extends CustomizedSuite {
    protected Collection<String> m_packageNames;
    protected Collection<String> m_classNames;
    protected Collection<String> m_groupNames;
    protected int m_logLevel;
    
    ClassListSuite(final String projectName,
        final Collection<String> packageNames,
        final Collection<String> classNames,
        final Collection<String> groupNames,
        final Map<String, String> parameters,
        final String annotationType,
        final int logLevel) {
      super(projectName, "Custom suite", parameters, annotationType);
      
      m_packageNames = packageNames;
      m_classNames = classNames;
      m_groupNames = groupNames;
      m_logLevel = logLevel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initContentBuffer(XMLStringBuffer suiteBuffer) {
      Properties testAttrs = new Properties();
      testAttrs.setProperty("name", m_projectName);
      if (m_annotationType != null) {
        testAttrs.setProperty("annotations", m_annotationType.getName());
      }
      testAttrs.setProperty("verbose", String.valueOf(m_logLevel));
      
      suiteBuffer.push("test", testAttrs);
      
      if (null != m_groupNames) {
        suiteBuffer.push("groups");
        suiteBuffer.push("run");
        
        for (String groupName : m_groupNames) {
          Properties includeAttrs = new Properties();
          includeAttrs.setProperty("name", groupName);
          suiteBuffer.addEmptyElement("include", includeAttrs);
        }
        
        suiteBuffer.pop("run");
        suiteBuffer.pop("groups");
      }
      
      // packages belongs to suite according to the latest DTD
      if ((m_packageNames != null) && (m_packageNames.size() > 0)) {
        suiteBuffer.push("packages");
        
        for (String packageName : m_packageNames) {
          Properties packageAttrs = new Properties();
          packageAttrs.setProperty("name", packageName);
          suiteBuffer.addEmptyElement("package", packageAttrs);
        }
        suiteBuffer.pop("packages");
      }
      
      if ((m_classNames != null) && (m_classNames.size() > 0)) {
        suiteBuffer.push("classes");
        
        for (String className : m_classNames) {
          Properties classAttrs = new Properties();
          classAttrs.setProperty("name", className);
          suiteBuffer.addEmptyElement("class", classAttrs);
        }
        
        suiteBuffer.pop("classes");
      }
      suiteBuffer.pop("test");
    }
  }
}
