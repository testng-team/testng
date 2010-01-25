package org.testng.xml;

import org.testng.IObjectFactory;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.Utils;
import org.testng.internal.version.VersionInfo;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Suite definition parser utility.
 * 
 * @author Cedric Beust
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class TestNGContentHandler extends DefaultHandler {
  private XmlSuite m_currentSuite = null;
  private XmlTest m_currentTest = null;
  private List<String> m_currentDefines = null;
  private List<String> m_currentRuns = null;
  private List<String> m_currentGroups = null;
  private List<XmlClass> m_currentClasses = null;
  private List<XmlPackage> m_currentPackages = null;
  private XmlPackage m_currentPackage = null;
  private List<XmlSuite> m_suites = Lists.newArrayList();
  private List<String> m_currentIncludedGroups = null;
  private List<String> m_currentExcludedGroups = null;
  private Map<String, String> m_currentTestParameters = null;
  private Map<String, String> m_currentSuiteParameters = null;
  private List<String> m_currentMetaGroup = null;
  private String m_currentMetaGroupName;
  private boolean m_inTest = false;
  private XmlClass m_currentClass = null;
  private ArrayList<XmlInclude> m_currentIncludedMethods = null;
  private List<String> m_currentExcludedMethods = null;
  private ArrayList<XmlMethodSelector> m_currentSelectors = null;
  private XmlMethodSelector m_currentSelector = null;
  private String m_currentLanguage = null;
  private String m_currentExpression = null;
  private List<String> m_suiteFiles = Lists.newArrayList();
  private boolean m_enabledTest;
  private List<String> m_listeners;
  
  private String m_fileName;

  public TestNGContentHandler(String fileName) {
    m_fileName = fileName;
  }
  
  static private void ppp(String s) {
    System.out.println("[TestNGContentHandler] " + s);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
   *      java.lang.String)
   */
  @Override
  public InputSource resolveEntity(String systemId, String publicId) throws IOException, SAXException {
    InputSource result = null;
    if (Parser.DEPRECATED_TESTNG_DTD_URL.equals(publicId)
        || Parser.TESTNG_DTD_URL.equals(publicId)) {
      InputStream is = getClass().getClassLoader().getResourceAsStream(Parser.TESTNG_DTD);
      if (null == is) {
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(Parser.TESTNG_DTD);
        if (null == is) {
          System.out.println("WARNING: couldn't find in classpath " + publicId
              + "\n" + "Fetching it from the Web site.");
          result = super.resolveEntity(systemId, publicId);
        }
        else {
          result = new InputSource(is);
        }
      }
      else {
        result = new InputSource(is);
      }
    }
    else {
      result = super.resolveEntity(systemId, publicId);
    }

    return result;
  }
  
  /**
   * Parse <suite-file>
   */
  private void xmlSuiteFile(boolean start, Attributes attributes) {
    if (start) {
      String path = attributes.getValue("path");
      m_suiteFiles.add(path);
    }
    else {
      m_currentSuite.setSuiteFiles(m_suiteFiles);
    }
  }

  /**
   * Parse <suite>
   */
  private void xmlSuite(boolean start, Attributes attributes) {
    if (start) {
      String name = attributes.getValue("name");
      m_currentSuite = new XmlSuite();
      m_currentSuite.setFileName(m_fileName);
      m_currentSuite.setName(name);
      m_currentSuiteParameters = Maps.newHashMap();
      
      String verbose = attributes.getValue("verbose");
      if (null != verbose) {
        m_currentSuite.setVerbose(new Integer(verbose));
      }
      String jUnit = attributes.getValue("junit");
      if (null != jUnit) {
        m_currentSuite.setJUnit( Boolean.valueOf(jUnit).booleanValue());
      }
      String parallel = attributes.getValue("parallel");
      if (null != parallel) {
        if(XmlSuite.PARALLEL_METHODS.equals(parallel)
            || XmlSuite.PARALLEL_TESTS.equals(parallel)
            || XmlSuite.PARALLEL_NONE.equals(parallel)
            || XmlSuite.PARALLEL_CLASSES.equals(parallel)
            || "true".equals(parallel)
            || "false".equals(parallel)) {
          m_currentSuite.setParallel(parallel);
        }
        else {
          Utils.log("Parser", 1, "[WARN] Unknown value of attribute 'parallel' at suite level: '" + parallel + "'.");
        }
      }
      String skip = attributes.getValue("skipfailedinvocationcounts");
      if (skip != null) {
        m_currentSuite.setSkipFailedInvocationCounts(Boolean.valueOf(skip));
      }
      String threadCount = attributes.getValue("thread-count");
      if (null != threadCount) {
        m_currentSuite.setThreadCount(Integer.parseInt(threadCount));
      }
      String dataProviderThreadCount = attributes.getValue("data-provider-thread-count");
      if (null != dataProviderThreadCount) {
        m_currentSuite.setDataProviderThreadCount(Integer.parseInt(dataProviderThreadCount));
      }
      String annotations = attributes.getValue("annotations");
      if (null != annotations) {
        m_currentSuite.setAnnotations(annotations);
      }
      else if (VersionInfo.IS_JDK14) {
        m_currentSuite.setAnnotations(XmlSuite.JAVADOC_ANNOTATION_TYPE);
      }
      String timeOut = attributes.getValue("time-out");
      if (null != timeOut) {
        m_currentSuite.setTimeOut(timeOut);
      }
      String objectFactory = attributes.getValue("object-factory");
      if(null != objectFactory) {
        try {
          m_currentSuite.setObjectFactory((IObjectFactory)Class.forName(objectFactory).newInstance());
        }
        catch(Exception e) {
          Utils.log("Parser", 1, "[ERROR] Unable to create custom object factory '" + objectFactory + "' :" + e);
        }
      }
    }
    else {
      m_currentSuite.setParameters(m_currentSuiteParameters);
      m_suites.add(m_currentSuite);
      m_currentSuiteParameters = null;
    }
  }

  /**
   * Parse <define>
   */
  private void xmlDefine(boolean start, Attributes attributes) {
    if (start) {
      String name = attributes.getValue("name");
      m_currentDefines = Lists.newArrayList();
      m_currentMetaGroup = Lists.newArrayList();
      m_currentMetaGroupName = name;
    }
    else {
      m_currentTest.addMetaGroup(m_currentMetaGroupName, m_currentMetaGroup);
      m_currentDefines = null;
    }
  }

  /**
   * Parse <script>
   */
  private void xmlScript(boolean start, Attributes attributes) {
    if (start) {
//      ppp("OPEN SCRIPT");
      m_currentLanguage = attributes.getValue("language");
      m_currentExpression = "";
    }
    else {
//      ppp("CLOSE SCRIPT:@@" + m_currentExpression + "@@");
      m_currentSelector.setExpression(m_currentExpression);
      m_currentSelector.setLanguage(m_currentLanguage);
      if (m_inTest) {
        m_currentTest.setBeanShellExpression(m_currentExpression);
      }
      else {
        m_currentSuite.setBeanShellExpression(m_currentExpression);
      }
      m_currentLanguage = null;
      m_currentExpression = null;
    }
  }

  /**
   * Parse <test>
   */
  private void xmlTest(boolean start, Attributes attributes) {
    if (start) {
      m_currentTest = new XmlTest(m_currentSuite);
      m_currentTestParameters = Maps.newHashMap();
      final String testName= attributes.getValue("name");
      if(null == testName || "".equals(testName.trim())) {
        throw new TestNGException("Test <test> element must define the name attribute");
      }
      m_currentTest.setName(attributes.getValue("name"));
      String verbose = attributes.getValue("verbose");
      if (null != verbose) {
        m_currentTest.setVerbose(Integer.parseInt(verbose));
      }
      String jUnit = attributes.getValue("junit");
      if (null != jUnit) {
        m_currentTest.setJUnit( Boolean.valueOf(jUnit).booleanValue());
      }
      String skip = attributes.getValue("skipfailedinvocationcounts");
      if (skip != null) {
        m_currentTest.setSkipFailedInvocationCounts(Boolean.valueOf(skip).booleanValue());
      }
      String parallel = attributes.getValue("parallel");
      if (null != parallel) {
        if(XmlSuite.PARALLEL_METHODS.equals(parallel)
            || XmlSuite.PARALLEL_NONE.equals(parallel)
            || XmlSuite.PARALLEL_CLASSES.equals(parallel)
            || "true".equals(parallel)
            || "false".equals(parallel)) {
          m_currentTest.setParallel(parallel);
        }
        else {
          Utils.log("Parser", 1, "[WARN] Unknown value of attribute 'parallel' for test '" + m_currentTest.getName() + "': '" + parallel + "'");
        }
      }
      String threadCount = attributes.getValue("thread-count");
      if(null != threadCount) {
        m_currentTest.setThreadCount(Integer.parseInt(threadCount));
      }
      String timeOut = attributes.getValue("time-out");
      if (null != timeOut) {
        m_currentTest.setTimeOut(Long.parseLong(timeOut));
      }
      String annotations = attributes.getValue("annotations");
      if (null != annotations) {
        m_currentTest.setAnnotations(annotations);
      }
      m_inTest = true;
      m_enabledTest= true;
      String enabledTestString = attributes.getValue("enabled");
      if(null != enabledTestString) {
        m_enabledTest = Boolean.valueOf(enabledTestString);
      }
    }
    else {
      if (null != m_currentTestParameters && m_currentTestParameters.size() > 0) {
        m_currentTest.setParameters(m_currentTestParameters);
      }
      if (null != m_currentClasses) {
        m_currentTest.setXmlClasses(m_currentClasses);
      }
      m_currentClasses = null;
      m_currentTest = null;
      m_currentTestParameters = null;
      m_inTest = false;
      if(!m_enabledTest) {
        List<XmlTest> tests= m_currentSuite.getTests();
        tests.remove(tests.size() - 1);
      }
    }
  }

  /**
   * Parse <classes>
   */
  public void xmlClasses(boolean start, Attributes attributes) {
    if (start) {
      m_currentClasses = Lists.newArrayList();
    }
    else {
      m_currentTest.setXmlClasses(m_currentClasses);
      m_currentClasses = null;
    }
  }
  
  /**
   * Parse <listeners>
   */
  public void xmlListeners(boolean start, Attributes attributes) {
    if (start) {
      m_listeners = Lists.newArrayList();
    }
    else {
      if (null != m_listeners) {
        m_currentSuite.setListeners(m_listeners);
        m_listeners = null;
      }
    }
  }
  
  /**
   * Parse <listener>
   */
  public void xmlListener(boolean start, Attributes attributes) {
    if (start) {
      String listener = attributes.getValue("class-name");
      m_listeners.add(listener);
    }
  }

  /**
   * Parse <packages>
   */
  public void xmlPackages(boolean start, Attributes attributes) {
    if (start) {
      m_currentPackages = Lists.newArrayList();
    }
    else {
      if (null != m_currentPackages) {
        if(m_inTest) {
          m_currentTest.setXmlPackages(m_currentPackages);
        }
        else {
          m_currentSuite.setXmlPackages(m_currentPackages);
        }
      }
      
      m_currentPackages = null;
      m_currentPackage = null;
    }
  }

  /**
   * Parse <method-selectors>
   */
  public void xmlMethodSelectors(boolean start, Attributes attributes) {
    if (start) {
      m_currentSelectors = new ArrayList<XmlMethodSelector>();
    }
    else {
      if (m_inTest) {
        m_currentTest.setMethodSelectors(m_currentSelectors);
      }
      else {
        m_currentSuite.setMethodSelectors(m_currentSelectors);
      }
      
      m_currentSelectors = null;
    }
  }

  /**
   * Parse <selector-class>
   */
  public void xmlSelectorClass(boolean start, Attributes attributes) {
    if (start) {
      m_currentSelector.setName(attributes.getValue("name"));
      String priority = attributes.getValue("priority");
      if (priority == null) {
        priority = "0";
      }
      m_currentSelector.setPriority(new Integer(priority));
    }
    else {
      // do nothing
    }
  }
  
  /**
   * Parse <method-selector>
   */
  public void xmlMethodSelector(boolean start, Attributes attributes) {
    if (start) {
      m_currentSelector = new XmlMethodSelector();
    }
    else {
      m_currentSelectors.add(m_currentSelector);
      m_currentSelector = null;
    }
  }

  private void xmlMethod(boolean start, Attributes attributes) {
    if (start) {
      m_currentIncludedMethods = new ArrayList<XmlInclude>();
      m_currentExcludedMethods = Lists.newArrayList();
    }
    else {
      m_currentClass.setIncludedMethods(m_currentIncludedMethods);
      m_currentClass.setExcludedMethods(m_currentExcludedMethods);
      m_currentIncludedMethods = null;
      m_currentExcludedMethods = null;
    }
  }

  /**
   * Parse <run>
   */
  public void xmlRun(boolean start, Attributes attributes) {
    if (start) {
      m_currentRuns = Lists.newArrayList();
    }
    else {
      m_currentTest.setIncludedGroups(m_currentIncludedGroups);
      m_currentTest.setExcludedGroups(m_currentExcludedGroups);
    }
  }

  /**
   * NOTE: I only invoke xml*methods (e.g. xmlSuite()) if I am acting on both
   * the start and the end of the tag. This way I can keep the treatment of
   * this tag in one place. If I am only doing something when the tag opens,
   * the code is inlined below in the startElement() method.
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    String name = attributes.getValue("name");

    // ppp("START ELEMENT uri:" + uri + " sName:" + localName + " qName:" + qName +
    // " " + attributes);
    if ("suite".equals(qName)) {
      xmlSuite(true, attributes);
    }
    else if ("suite-file".equals(qName)) {
      xmlSuiteFile(true, attributes);
    }
    else if ("test".equals(qName)) {
      xmlTest(true, attributes);
    }
    else if ("script".equals(qName)) {
      xmlScript(true, attributes);
    }
    else if ("method-selector".equals(qName)) {
      xmlMethodSelector(true, attributes);
    }
    else if ("method-selectors".equals(qName)) {
      xmlMethodSelectors(true, attributes);
    }
    else if ("selector-class".equals(qName)) {
      xmlSelectorClass(true, attributes);
    }
    else if ("classes".equals(qName)) {
      xmlClasses(true, attributes);
    }
    else if ("packages".equals(qName)) {
      xmlPackages(true, attributes);
    }
    else if ("listeners".equals(qName)) {
      xmlListeners(true, attributes);
    }
    else if ("listener".equals(qName)) {
      xmlListener(true, attributes);
    }
    else if ("class".equals(qName)) {
      // If m_currentClasses is null, the XML is invalid and SAX
      // will complain, but in the meantime, dodge the NPE so SAX
      // can finish parsing the file.
      if (null != m_currentClasses) {
        m_currentClass = new XmlClass(name, Boolean.TRUE);
        m_currentClasses.add(m_currentClass);
      }
    }
    else if ("package".equals(qName)) {
      if (null != m_currentPackages) {
        m_currentPackage = new XmlPackage();
        m_currentPackage.setName(name);
        m_currentPackages.add(m_currentPackage);
      }
    }
    else if ("define".equals(qName)) {
      xmlDefine(true, attributes);
    }
    else if ("run".equals(qName)) {
      xmlRun(true, attributes);
    }
    else if ("groups".equals(qName)) {
      m_currentIncludedGroups = Lists.newArrayList();
      m_currentExcludedGroups = Lists.newArrayList();
    }
    else if ("methods".equals(qName)) {
      xmlMethod(true, attributes);
    }
    else if ("include".equals(qName)) {
      if (null != m_currentIncludedMethods) {
        String in = attributes.getValue("invocationNumbers");
        if (!Utils.isStringEmpty(in)) {
          m_currentIncludedMethods.add(new XmlInclude(name, stringToList(in)));
        } else {
          m_currentIncludedMethods.add(new XmlInclude(name));
        }
      }
      else if (null != m_currentDefines) {
        m_currentMetaGroup.add(name);
      }
      else if (null != m_currentRuns) {
        m_currentIncludedGroups.add(name);
      }
      else if (null != m_currentPackage) {
        m_currentPackage.getInclude().add(name);
      }
    }
    else if ("exclude".equals(qName)) {
      if (null != m_currentExcludedMethods) {
        m_currentExcludedMethods.add(name);
      }
      else if (null != m_currentRuns) {
        m_currentExcludedGroups.add(name);
      }
      else if (null != m_currentPackage) {
        m_currentPackage.getExclude().add(name);
      }
    }
    else if ("parameter".equals(qName)) {
      String value = attributes.getValue("value");
      if (m_inTest) {
        m_currentTestParameters.put(name, value);
      }
      else {
        m_currentSuiteParameters.put(name, value);
      }
    }
  }

  private List<Integer> stringToList(String in) {
    String[] numbers = in.split(" ");
    List<Integer> result = Lists.newArrayList();
    for (String n : numbers) {
      result.add(Integer.parseInt(n));
    }
    return result;
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("suite".equals(qName)) {
      xmlSuite(false, null);
    }
    else if ("suite-file".equals(qName)) {
      xmlSuiteFile(false, null);
    }
    else if ("test".equals(qName)) {
      xmlTest(false, null);
    }
    else if ("define".equals(qName)) {
      xmlDefine(false, null);
    }
    else if ("run".equals(qName)) {
      xmlRun(false, null);
    }
    else if ("methods".equals(qName)) {
      xmlMethod(false, null);
    }
    else if ("classes".equals(qName)) {
      xmlClasses(false, null);
    }
    else if ("classes".equals(qName)) {
      xmlPackages(false, null);
    }
    else if ("listeners".equals(qName)) {
      xmlListeners(false, null);
    }
    else if ("method-selector".equals(qName)) {
      xmlMethodSelector(false, null);
    }
    else if ("method-selectors".equals(qName)) {
      xmlMethodSelectors(false, null);
    }
    else if ("selector-class".equals(qName)) {
      xmlSelectorClass(false, null);
    }
    else if ("script".equals(qName)) {
      xmlScript(false, null);
    }
    else if ("packages".equals(qName)) {
      xmlPackages(false, null);
    }
  }

  @Override
  public void error(SAXParseException e) throws SAXException {
    throw e;
  }

  private boolean areWhiteSpaces(char[] ch, int start, int length) {
    for (int i = start; i < start + length; i++) {
      char c = ch[i];
      if (c != '\n' && c != '\t' && c != ' ') return false;
    }    
    
    return true;
  }
  
  @Override
  public void characters(char ch[], int start, int length) {
    if (null != m_currentLanguage && ! areWhiteSpaces(ch, start, length)) {
      m_currentExpression += new String(ch, start, length);
    }
  }

  public XmlSuite getSuite() {
    return m_currentSuite;
  }
}