package org.testng.xml;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.testng.TestNG;
import org.testng.reporters.XMLStringBuffer;

/**
 * Wrapper for real suites and custom configured suites.
 * Should only be used for integration purposes; creating a custom testng.xml
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public abstract class LaunchSuite {
  protected boolean m_temporary;

  protected LaunchSuite(boolean isTemp) {
    m_temporary= isTemp;
  }

  public boolean isTemporary() {
    return m_temporary;
  }

  public abstract File save(File directory);

  public static class ExistingSuite extends LaunchSuite {

    /**
     * The existing suite path (either relative to the project root or an absolute path)
     */
    private File m_suitePath;

    public ExistingSuite(File path) {
      super(false);

      m_suitePath= path;
    }

    @Override
    public File save(File directory) {
      return m_suitePath;
    }
  }

  private abstract static class CustomizedSuite extends LaunchSuite {
    protected String m_projectName;
    protected String m_suiteName;
    protected String m_annotationType;
    protected Map<String, String> m_parameters;
    private XMLStringBuffer m_suiteBuffer;

    private CustomizedSuite(final String projectName,
                            final String className,
                            final Map<String, String> parameters,
                            final String annotationType) {
      super(true);

      m_projectName= projectName;
      m_suiteName= className;
      m_parameters= parameters;
      if("1.4".equals(annotationType) || TestNG.JAVADOC_ANNOTATION_TYPE.equals(annotationType)) {
        m_annotationType= TestNG.JAVADOC_ANNOTATION_TYPE;
      }
      else {
        m_annotationType= TestNG.JDK5_ANNOTATION_TYPE;
      }
    }

    protected XMLStringBuffer createContentBuffer() {
      XMLStringBuffer suiteBuffer= new XMLStringBuffer(""); //$NON-NLS-1$
      suiteBuffer.setDocType("suite SYSTEM \"" + Parser.TESTNG_DTD_URL + "\"");

      Properties attrs= new Properties();
      attrs.setProperty("name", m_suiteName);
      suiteBuffer.push("suite", attrs);

      if(m_parameters != null) {
        for(Map.Entry<String, String> entry : m_parameters.entrySet()) {
          Properties paramAttrs= new Properties();
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

    private XMLStringBuffer getSuiteBuffer() {
      if(null == m_suiteBuffer) {
        m_suiteBuffer= createContentBuffer();
      }

      return m_suiteBuffer;
    }

    protected abstract void initContentBuffer(XMLStringBuffer suiteBuffer);

    @Override
    public File save(File directory) {
      final File suiteFile= new File(directory, "temp-testng-customsuite.xml");

      saveSuiteContent(suiteFile, getSuiteBuffer());

      return suiteFile;
    }

    protected void saveSuiteContent(final File file, final XMLStringBuffer content) {
      FileWriter fw= null;
      BufferedWriter bw= null;
      try {
        fw= new FileWriter(file);
        bw= new BufferedWriter(fw);
        bw.write(content.getStringBuffer().toString());
        bw.flush();
      }
      catch(IOException ioe) {
      }
      finally {
        if(null != bw) {
          try {
            bw.close();
          }
          catch(IOException ioe) {
          }
        }
        if(null != fw) {
          try {
            fw.close();
          }
          catch(IOException ioe) {
          }
        }
      }
    }
  }

  static class MethodsSuite extends CustomizedSuite {
    protected Collection<String> m_methodNames;
    protected String m_className;
    protected int m_logLevel;

    MethodsSuite(final String projectName,
                 final String className,
                 final Collection<String> methodNames,
                 final Map<String, String> parameters,
                 final String annotationType,
                 final int logLevel) {
      super(projectName, className, parameters, annotationType);

      m_className= className;
      m_methodNames= methodNames;
      m_logLevel= logLevel;
    }

    @Override
    protected void initContentBuffer(XMLStringBuffer suiteBuffer) {
      Properties testAttrs= new Properties();
      testAttrs.setProperty("name", m_className);
      if(m_annotationType != null) {
        testAttrs.setProperty("annotations", m_annotationType);
      }
      testAttrs.setProperty("verbose", String.valueOf(m_logLevel));

      suiteBuffer.push("test", testAttrs);

      suiteBuffer.push("classes");

      Properties classAttrs= new Properties();
      classAttrs.setProperty("name", m_className);

      if((null != m_methodNames) && (m_methodNames.size() > 0)) {
        suiteBuffer.push("class", classAttrs);

        suiteBuffer.push("methods");

        for(Object m_methodName : m_methodNames) {
          Properties methodAttrs= new Properties();
          methodAttrs.setProperty("name", (String) m_methodName);
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

      m_packageNames= packageNames;
      m_classNames= classNames;
      m_groupNames= groupNames;
      m_logLevel= logLevel;
    }

    @Override
    protected void initContentBuffer(XMLStringBuffer suiteBuffer) {
      Properties testAttrs= new Properties();
      testAttrs.setProperty("name", m_projectName);
      if(m_annotationType != null) {
        testAttrs.setProperty("annotations", m_annotationType);
      }
      testAttrs.setProperty("verbose", String.valueOf(m_logLevel));

      suiteBuffer.push("test", testAttrs);

      if(null != m_groupNames) {
        suiteBuffer.push("groups");
        suiteBuffer.push("run");

        for(String m_groupName : m_groupNames) {
          Properties includeAttrs= new Properties();
          includeAttrs.setProperty("name", m_groupName);
          suiteBuffer.addEmptyElement("include", includeAttrs);
        }

        suiteBuffer.pop("run");
        suiteBuffer.pop("groups");
      }

      // packages belongs to suite according to the latest DTD
      if((m_packageNames != null) && (m_packageNames.size() > 0)) {
        suiteBuffer.push("packages");

        for(String m_packageName : m_packageNames) {
          Properties packageAttrs= new Properties();
          packageAttrs.setProperty("name", m_packageName);
          suiteBuffer.addEmptyElement("package", packageAttrs);
        }

        suiteBuffer.pop("packages");
      }
      
      if((m_classNames != null) && (m_classNames.size() > 0)) {
        suiteBuffer.push("classes");

        for(String m_className : m_classNames) {
          Properties classAttrs= new Properties();
          classAttrs.setProperty("name", m_className);
          suiteBuffer.addEmptyElement("class", classAttrs);
        }

        suiteBuffer.pop("classes");
      }
      
      suiteBuffer.pop("test");

    }
  }
}
