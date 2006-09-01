package org.testng.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.testng.TestNG;
import org.testng.reporters.XMLStringBuffer;

/**
 * This class describes the tag &lt;suite&gt; in testng.xml.
 *
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 * @author <a href = 'mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlSuite implements Serializable, Cloneable {
  // Possible value for the attribute annotations of <suite>
  // It would be nice to factor out these names as they are
  // also defined in the TestNG class.
  
  /** @deprecated use JAVADOC_ANNOTATION_TYPE. */
  @Deprecated
  public static final String JAVADOC = "javadoc";

  /** The JDK50 annotation type ID ("JDK5"). */
  public static final String JDK5_ANNOTATION_TYPE = TestNG.JDK5_ANNOTATION_TYPE;
  
  /** The JavaDoc annotation type ID ("javadoc"). */
  public static final String JAVADOC_ANNOTATION_TYPE = TestNG.JAVADOC_ANNOTATION_TYPE;
  
  /** Parallel modes */
  public static final String PARALLEL_TESTS = "tests";
  public static final String PARALLEL_METHODS = "methods";
  
  private String m_test;
  
  /** The suite name. */
  private String m_name = TestNG.DEFAULT_SUITE_NAME;
  
  /** The suite verbose flag. (0 to 10)*/
  private Integer m_verbose = new Integer(1);
  
  private String m_parallel = null;
  
  /** JUnit compatibility flag. */
  private Boolean m_isJUnit = Boolean.FALSE;
  
  /** The thread count. */
  private int m_threadCount = 5;
  
  /** The suite annotation type. */
  private String m_annotations = JDK5_ANNOTATION_TYPE;
  
  /** The packages containing test classes. */
  private List<XmlPackage> m_xmlPackages = new ArrayList<XmlPackage>();
  
  /** BeanShell expression. */
  private String m_expression = null;
  
  /** Suite level method selectors. */
  private List<XmlMethodSelector> m_methodSelectors = new ArrayList<XmlMethodSelector>();
  
  /** Tests in suite. */
  private List<XmlTest> m_tests = new ArrayList<XmlTest>();
  
  /** Suite level parameters. */
  private Map<String, String> m_parameters = new HashMap<String, String>();
  
  /** Name of the XML file */
  private String m_fileName;

  /** Time out for methods/tests */
  private String m_timeOut;

  /**
   * @return the fileName
   */
  public String getFileName() {
    return m_fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName(String fileName) {
    m_fileName = fileName;
  }

  /**
   * Returns the parallel mode.
   */
  public String getParallel() {
    return m_parallel;
  }

  /**
   * Sets the parallel mode
   */
  public void setParallel(String parallel) {
    m_parallel = parallel;
  }

  /**
   * Returns the verbose.
   * @return the verbose.
   */
  public Integer getVerbose() {
    return m_verbose;
  }

  /**
   * Set the verbose.
   * @param verbose The verbose to set.
   */
  public void setVerbose(Integer verbose) {
    m_verbose = verbose;
  }

  /**
   * Returns the name.
   * @return the name.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Sets the name.
   * @param name The name to set.
   */
  public void setName(String name) {
    m_name = name;
  }

  /**
   * Returns the test.
   * @return the test.
   */
  public String getTest() {
    return m_test;
  }

  /**
   * Returns the tests.
   * @return the tests.
   */
  public List<XmlTest> getTests() {
    return m_tests;
  }

  /**
   * Returns the method selectors.
   *
   * @return the method selectors.
   */
  public List<XmlMethodSelector> getMethodSelectors() {
    return m_methodSelectors;
  }

  /**
   * Sets the method selectors.
   *
   * @param methodSelectors the method selectors.
   */
  public void setMethodSelectors(List<XmlMethodSelector> methodSelectors) {
    m_methodSelectors = methodSelectors;
  }

  /**
   * Sets the bean shell expression.
   *
   * @param expression the bean shell expression.
   */
  public void setBeanShellExpression(String expression) {
    m_expression = expression;
  }

  /**
   * Returns the bean shell expression.
   *
   * @return the bean shell expression.
   * @deprecated use getBeanShelltExpression()
   */
  @Deprecated
  public String getExpression() {
    return m_expression;
  }

  /**
   * Returns the bean shell expression.
   *
   * @return the bean shell expression.
   */
  public String getBeanShellExpression() {
    return m_expression;
  }
  
  /**
   * Sets parameters.
   * @param parameters the parameters.
   */
  public void setParameters(Map<String, String> parameters) {
    m_parameters = parameters;
  }

  /**
   * Returns the parameters defined in this suite only.
   * @return The parameters defined in this suite only.
   */
  public Map<String, String> getParameters() {
    return m_parameters;
  }

  /**
   * Returns the parameters defined in this suite and all its XmlTests.
   * @return The parameters defined in this suite and all its XmlTests.
   */
  public Map<String, String> getAllParameters() {
    Map<String, String> result = new HashMap<String, String>();
    for (String key : m_parameters.keySet()) {
      result.put(key, m_parameters.get(key));
    }

    for (XmlTest test : getTests()) {
      Map<String, String> tp = test.getParameters();
      for (String key : tp.keySet()) {
        result.put(key, tp.get(key));
      }
    }

    return result;
  }

  /**
   * Returns the parameter defined in this suite only.
   * @param name the parameter name.
   * @return The parameter defined in this suite only.
   */
  public String getParameter(String name) {
    return m_parameters.get(name);
  }

  /**
   * Returns the threadCount.
   * @return the threadCount.
   */
  public int getThreadCount() {
    return m_threadCount;
  }

  /**
   * Set the thread count.
   * @param threadCount The thread count to set.
   */
  public void setThreadCount(int threadCount) {
    m_threadCount = threadCount;
  }

  /**
   * Returns the annotation type.
   * @return the annotation type.
   */
  public String getAnnotations() {
    return m_annotations;
  }

  /**
   * Sets the annotation type.
   * 
   * @param annotations the annotation type ("javadoc" or "JDK5").
   */
  public void setAnnotations(String annotations) {
    m_annotations = annotations;
  }

  /**
   * Returns the JUnit compatibility flag.
   * @return the JUnit compatibility flag.
   */
  public Boolean isJUnit() {
    return m_isJUnit;
  }

  /**
   * Sets the JUnit compatibility flag.
   *
   * @param isJUnit the JUnit compatibility flag.
   */
  public void setJUnit(Boolean isJUnit) {
    m_isJUnit = isJUnit;
  }

  /**
   * Sets the XML packages.
   *
   * @param packages the XML packages.
   */
  public void setXmlPackages(List<XmlPackage> packages) {
    m_xmlPackages = packages;
  }

  /**
   * Returns the XML packages.
   *
   * @return the XML packages.
   */
  public List<XmlPackage> getXmlPackages() {
    return m_xmlPackages;
  }

  /**
   * Returns a String representation of this XML suite.
   *
   * @return a String representation of this XML suite.
   */
  public String toXml() {
    XMLStringBuffer xsb = new XMLStringBuffer("");
    xsb.setDocType("suite SYSTEM \"" + Parser.TESTNG_DTD_URL + "\"");
    Properties p = new Properties();
    p.setProperty("name", getName());
    p.setProperty("verbose", getVerbose().toString());
    p.setProperty("parallel", "" + getParallel());
    p.setProperty("thread-count", "" + getThreadCount());
    p.setProperty("annotations", getAnnotations());

    xsb.push("suite", p);

    for (String paramName : m_parameters.keySet()) {
      Properties paramProps = new Properties();
      paramProps.setProperty("name", paramName);
      paramProps.setProperty("value", m_parameters.get(paramName));

      xsb.addEmptyElement("parameter", paramProps);
    }

    for (XmlTest test : getTests()) {
      xsb.getStringBuffer().append(test.toXml("  "));
    }

    if (null != getXmlPackages() && !getXmlPackages().isEmpty()) {
      xsb.push("packages");

      for (XmlPackage pack : getXmlPackages()) {
        xsb.getStringBuffer().append(pack.toXml("  "));
      }

      xsb.pop("packages");
    }

    if (null != getMethodSelectors() && !getMethodSelectors().isEmpty()) {
      xsb.push("method-selectors");
      for (XmlMethodSelector selector : getMethodSelectors()) {
        xsb.getStringBuffer().append(selector.toXml("  "));
      }

      xsb.pop("method-selectors");
    }

    xsb.pop("suite");

    return xsb.toXML();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer("[Suite: \"" + m_name + "\" ");

    for (XmlTest t : m_tests) {
      result.append("  " + t.toString()).append(" ");
    }

    result.append("]");

    return result.toString();
  }

  /**
   * Logs to System.out.
   * @param s the message to log.
   */
  private static void ppp(String s) {
    System.out.println("[XmlSuite] " + s);
  }

  /**
   * {@inheritDoc}
   * Note that this is not a full clone:  XmlTest children are not cloned by this
   * method.
   */
  @Override
  public Object clone() {
    XmlSuite result = new XmlSuite();

    result.setName(getName());
    result.setAnnotations(getAnnotations());
    result.setParallel(getParallel());
    result.setThreadCount(getThreadCount());
    result.setParameters(getAllParameters());
    result.setVerbose(getVerbose());
    result.setXmlPackages(getXmlPackages());
    result.setBeanShellExpression(getExpression());
    result.setMethodSelectors(getMethodSelectors());

    return result;
  }

    public void setTimeOut(String timeOut) {
        m_timeOut = timeOut;
    }
    
    public String getTimeOut() {
        return m_timeOut;
    }
    
    public long getTimeOut(long def) {
        long result = def;
        if (m_timeOut != null) {
            result = new Long(m_timeOut).longValue();
        }
        
        return result;
    }
}
