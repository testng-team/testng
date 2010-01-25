package org.testng.xml;

import org.testng.IObjectFactory;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.internal.version.VersionInfo;
import org.testng.reporters.XMLStringBuffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
  public static final String JDK5_ANNOTATION_TYPE = AnnotationTypeEnum.JDK.getName();
  
  /** The JavaDoc annotation type ID ("javadoc"). */
  public static final String JAVADOC_ANNOTATION_TYPE = AnnotationTypeEnum.JAVADOC.getName();
  
  /** Parallel modes */
  public static final String PARALLEL_TESTS = "tests";
  public static final String PARALLEL_METHODS = "methods";
  public static final String PARALLEL_CLASSES = "classes";
  public static final String PARALLEL_NONE = "none";
  
  private String m_test;
  
  /** The default suite name TODO CQ is this OK as a default name. */
  private static final String DEFAULT_SUITE_NAME = "";

  /** The suite name (defaults to DEFAULT_SUITE_NAME) */
  private String m_name = DEFAULT_SUITE_NAME;
  
  /** The suite verbose flag. (0 to 10)*/
  private Integer m_verbose = null;
  
  private String m_parallel = null;
  
  /** JUnit compatibility flag. */
  private Boolean m_isJUnit = Boolean.FALSE;
  
  private Boolean m_skipFailedInvocationCounts = Boolean.FALSE;
  
  /** The thread count. */
  private int m_threadCount = 5;

  /** Thread count for the data provider pool */
  public static final int DEFAULT_DATA_PROVIDER_THREAD_COUNT = 10;
  private int m_dataProviderThreadCount = DEFAULT_DATA_PROVIDER_THREAD_COUNT;
  
  /** The suite annotation type. */
  private AnnotationTypeEnum m_annotations;

  /** The suite default annotation type. */ 
  private AnnotationTypeEnum m_defaultAnnotations = VersionInfo.getDefaultAnnotationType();
  
  /** The packages containing test classes. */
  private List<XmlPackage> m_xmlPackages = Lists.newArrayList();
  
  /** BeanShell expression. */
  private String m_expression = null;
  
  /** Suite level method selectors. */
  private List<XmlMethodSelector> m_methodSelectors = Lists.newArrayList();
  
  /** Tests in suite. */
  private List<XmlTest> m_tests = Lists.newArrayList();
  
  /** Suite level parameters. */
  private Map<String, String> m_parameters = Maps.newHashMap();
  
  /** Name of the XML file */
  private String m_fileName;

  /** Time out for methods/tests */
  private String m_timeOut;

  private List<String> m_suiteFiles = Lists.newArrayList();

  private IObjectFactory m_objectFactory;

  private List<String> m_listeners = Lists.newArrayList();

  private static final long serialVersionUID = 4999962288272750226L;

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
   * @return the parallel mode.
   */
  public String getParallel() {
    return m_parallel;
  }

  public IObjectFactory getObjectFactory() {
    return m_objectFactory;
  }

  public void setObjectFactory(IObjectFactory objectFactory) {
    m_objectFactory = objectFactory;
  }

  /**
   * Sets the parallel mode
   * @param parallel the parallel mode
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
   * @return The parameters defined in this suite and all its XmlTests.
   */
  public Map<String, String> getAllParameters() {
    Map<String, String> result = Maps.newHashMap();
    for (Map.Entry<String, String> entry : m_parameters.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }

    for (XmlTest test : getTests()) {
      Map<String, String> tp = test.getParameters();
      for (Map.Entry<String, String> entry : tp.entrySet()) {
        result.put(entry.getKey(), entry.getValue());
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
   * Returns the annotation type for the suite. If an annotation type has
   * not been explicitly set, the default annotation type is returned.
   * @return the annotation type for the suite.
   */
  public String getAnnotations() {
    return (m_annotations != null ? m_annotations : m_defaultAnnotations).toString();
  }

  /**
   * Sets the overall default annotation type (JDK5/javadoc).
   * It is used if the annotation attribute of the suite definition
   * does not specify an explicit value.
   * 
   * @param annotationType one of the two string constant JAVADOC_ANNOTATION_TYPE or 
   * JDK5_ANNOTATION_TYPE.
   * @see #JAVADOC_ANNOTATION_TYPE
   * @see #JDK5_ANNOTATION_TYPE
   */
  public void setDefaultAnnotations(String annotationType) {
    m_defaultAnnotations = AnnotationTypeEnum.valueOf(annotationType);
  }
  
  /**
   * Sets the annotation type for the suite. If this value is not explicitly set,
   * the suite annotation type defaults to the default annotation type. see
   * setDefaultAnnotations  
   * @param annotations one of the two string constant JAVADOC_ANNOTATION_TYPE or 
   * JDK5_ANNOTATION_TYPE.
   * @see #JAVADOC_ANNOTATION_TYPE
   * @see #JDK5_ANNOTATION_TYPE
   */
  public void setAnnotations(String annotations) {
    m_annotations = AnnotationTypeEnum.valueOf(annotations);
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

  public Boolean skipFailedInvocationCounts() {
    return m_skipFailedInvocationCounts;
  }

  public void setSkipFailedInvocationCounts(boolean skip) {
    m_skipFailedInvocationCounts = skip;
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
    xsb.setDocType("suite SYSTEM \"" + Parser.TESTNG_DTD_URL + '\"');
    Properties p = new Properties();
    p.setProperty("name", getName());
    if (getVerbose() != null) {
      p.setProperty("verbose", getVerbose().toString());
    }
    final String parallel= getParallel();
    if(null != parallel && !"".equals(parallel)) {
      p.setProperty("parallel", parallel);
    }
    p.setProperty("thread-count", String.valueOf(getThreadCount()));
    p.setProperty("data-provider-thread-count", String.valueOf(getDataProviderThreadCount()));
    p.setProperty("annotations", getAnnotations());
    p.setProperty("junit", m_isJUnit != null ? m_isJUnit.toString() : "false"); // TESTNG-141
    p.setProperty("skipfailedinvocationCounts", 
        m_skipFailedInvocationCounts != null
          ? m_skipFailedInvocationCounts.toString() : "false");
    if(null != m_objectFactory)
      p.setProperty("object-factory", m_objectFactory.getClass().getName());
    xsb.push("suite", p);

    for (String paramName : m_parameters.keySet()) {
      Properties paramProps = new Properties();
      paramProps.setProperty("name", paramName);
      paramProps.setProperty("value", m_parameters.get(paramName));

      xsb.addEmptyElement("parameter", paramProps);
    }

    if (null != m_listeners && !m_listeners.isEmpty()) {
      xsb.push("listeners");
      for (String listenerName: m_listeners) {
        Properties listenerProps = new Properties();
        listenerProps.setProperty("class-name", listenerName);
        xsb.addEmptyElement("listener", listenerProps);
      }
      xsb.pop("listeners");
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
    StringBuffer result = new StringBuffer("[Suite: \"").append( m_name).append( "\" ");

    for (XmlTest t : m_tests) {
      result.append("  ").append( t.toString()).append(' ');
    }

    result.append(']');

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
    result.setListeners(getListeners());
    result.setAnnotations(getAnnotations());
    result.setParallel(getParallel());
    result.setThreadCount(getThreadCount());
    result.setDataProviderThreadCount(getDataProviderThreadCount());
    result.setParameters(getAllParameters());
    result.setVerbose(getVerbose());
    result.setXmlPackages(getXmlPackages());
    result.setBeanShellExpression(getExpression());
    result.setMethodSelectors(getMethodSelectors());
    result.setJUnit(isJUnit()); // TESTNG-141
    result.setSkipFailedInvocationCounts(skipFailedInvocationCounts());
    result.setObjectFactory(getObjectFactory());
    return result;
  }

    /**
     * Sets the timeout.
     *
     * @param timeOut the timeout.
     */
    public void setTimeOut(String timeOut) {
        m_timeOut = timeOut;
    }

    /**
     * Returns the timeout.
     * @return the timeout.
     */
    public String getTimeOut() {
        return m_timeOut;
    }
    
    /**
     * Returns the timeout as a long value specifying the default value to be used if
     * no timeout was specified.
     *
     * @param def the the default value to be used if no timeout was specified.
     * @return the timeout as a long value specifying the default value to be used if
     * no timeout was specified.
     */
    public long getTimeOut(long def) {
        long result = def;
        if (m_timeOut != null) {
            result = new Long(m_timeOut).longValue();
        }
        
        return result;
    }

    /**
     * Sets the suite files.
     *
     * @param files the suite files.
     */
    public void setSuiteFiles(List<String> files) {
      m_suiteFiles = files;
    }
    
    /**
     * Returns the suite files.
     * @return the suite files.
     */
    public List<String> getSuiteFiles() {
      return m_suiteFiles;
    }

    public void setListeners(List<String> listeners) {
      m_listeners = listeners;
    }
    
    public List<String> getListeners() {
      return m_listeners;
    }

    public void setDataProviderThreadCount(int count) {
      m_dataProviderThreadCount = count;
    }

    public int getDataProviderThreadCount() {
      return m_dataProviderThreadCount;
    }
}
