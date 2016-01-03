package org.testng.xml;

import static org.testng.internal.Utils.isStringBlank;

import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
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
import java.util.Stack;

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
  private List<XmlClass> m_currentClasses = null;
  private int m_currentTestIndex = 0;
  private int m_currentClassIndex = 0;
  private int m_currentIncludeIndex = 0;
  private List<XmlPackage> m_currentPackages = null;
  private XmlPackage m_currentPackage = null;
  private List<XmlSuite> m_suites = Lists.newArrayList();
  private List<String> m_currentIncludedGroups = null;
  private List<String> m_currentExcludedGroups = null;
  private Map<String, String> m_currentTestParameters = null;
  private Map<String, String> m_currentSuiteParameters = null;
  private Map<String, String> m_currentClassParameters = null;
  private Include m_currentInclude;
  private List<String> m_currentMetaGroup = null;
  private String m_currentMetaGroupName;

  enum Location {
    SUITE,
    TEST,
    CLASS,
    INCLUDE,
    EXCLUDE
  }
  private Stack<Location> m_locations = new Stack<>();

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
  private boolean m_loadClasses;
  private boolean m_validate = false;
  private boolean m_hasWarn = false;

  public TestNGContentHandler(String fileName, boolean loadClasses) {
    m_fileName = fileName;
    m_loadClasses = loadClasses;
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
  public InputSource resolveEntity(String systemId, String publicId)
      throws IOException, SAXException {
    InputSource result = null;
    if (Parser.DEPRECATED_TESTNG_DTD_URL.equals(publicId)
        || Parser.TESTNG_DTD_URL.equals(publicId)) {
      m_validate = true;
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
      pushLocation(Location.SUITE);
      m_suiteFiles.add(path);
    }
    else {
      m_currentSuite.setSuiteFiles(m_suiteFiles);
      popLocation(Location.SUITE);
    }
  }

  /**
   * Parse <suite>
   */
  private void xmlSuite(boolean start, Attributes attributes) {
    if (start) {
      pushLocation(Location.SUITE);
      String name = attributes.getValue("name");
      if (isStringBlank(name)) {
        throw new TestNGException("The <suite> tag must define the name attribute");
      }
      m_currentSuite = new XmlSuite();
      m_currentSuite.setFileName(m_fileName);
      m_currentSuite.setName(name);
      m_currentSuiteParameters = Maps.newHashMap();

      String verbose = attributes.getValue("verbose");
      if (null != verbose) {
        m_currentSuite.setVerbose(Integer.parseInt(verbose));
      }
      String jUnit = attributes.getValue("junit");
      if (null != jUnit) {
        m_currentSuite.setJUnit(Boolean.valueOf(jUnit));
      }
      String parallel = attributes.getValue("parallel");
      if (parallel != null) {
        XmlSuite.ParallelMode mode = XmlSuite.ParallelMode.getValidParallel(parallel);
        if (mode != null) {
          m_currentSuite.setParallel(mode);
        } else {
          Utils.log("Parser", 1, "[WARN] Unknown value of attribute 'parallel' at suite level: '" + parallel + "'.");
        }
      }
      String parentModule = attributes.getValue("parent-module");
      if (parentModule != null) {
        m_currentSuite.setParentModule(parentModule);
      }
      String guiceStage = attributes.getValue("guice-stage");
      if (guiceStage != null) {
        m_currentSuite.setGuiceStage(guiceStage);
      }
      String configFailurePolicy = attributes.getValue("configfailurepolicy");
      if (null != configFailurePolicy) {
        if (XmlSuite.SKIP.equals(configFailurePolicy) || XmlSuite.CONTINUE.equals(configFailurePolicy)) {
          m_currentSuite.setConfigFailurePolicy(configFailurePolicy);
        }
      }
      String groupByInstances = attributes.getValue("group-by-instances");
      if (groupByInstances!= null) {
        m_currentSuite.setGroupByInstances(Boolean.valueOf(groupByInstances));
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
      String timeOut = attributes.getValue("time-out");
      if (null != timeOut) {
        m_currentSuite.setTimeOut(timeOut);
      }
      String objectFactory = attributes.getValue("object-factory");
      if (null != objectFactory && m_loadClasses) {
        try {
          m_currentSuite.setObjectFactory((ITestObjectFactory)Class.forName(objectFactory).newInstance());
        }
        catch(Exception e) {
          Utils.log("Parser", 1, "[ERROR] Unable to create custom object factory '" + objectFactory + "' :" + e);
        }
      }
      String preserveOrder = attributes.getValue("preserve-order");
      if (preserveOrder != null) {
        m_currentSuite.setPreserveOrder(preserveOrder);
      }
      String allowReturnValues = attributes.getValue("allow-return-values");
      if (allowReturnValues != null) {
        m_currentSuite.setAllowReturnValues(Boolean.valueOf(allowReturnValues));
      }
    }
    else {
      m_currentSuite.setParameters(m_currentSuiteParameters);
      m_suites.add(m_currentSuite);
      m_currentSuiteParameters = null;
      popLocation(Location.SUITE);
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
      if (m_locations.peek() == Location.TEST) {
        m_currentTest.setBeanShellExpression(m_currentExpression);
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
      m_currentTest = new XmlTest(m_currentSuite, m_currentTestIndex++);
      pushLocation(Location.TEST);
      m_currentTestParameters = Maps.newHashMap();
      final String testName= attributes.getValue("name");
      if(isStringBlank(testName)) {
        throw new TestNGException("The <test> tag must define the name attribute");
      }
      m_currentTest.setName(attributes.getValue("name"));
      String verbose = attributes.getValue("verbose");
      if (null != verbose) {
        m_currentTest.setVerbose(Integer.parseInt(verbose));
      }
      String jUnit = attributes.getValue("junit");
      if (null != jUnit) {
        m_currentTest.setJUnit(Boolean.valueOf(jUnit));
      }
      String skip = attributes.getValue("skipfailedinvocationcounts");
      if (skip != null) {
        m_currentTest.setSkipFailedInvocationCounts(Boolean.valueOf(skip));
      }
      String groupByInstances = attributes.getValue("group-by-instances");
      if (groupByInstances!= null) {
        m_currentTest.setGroupByInstances(Boolean.valueOf(groupByInstances));
      }
      String preserveOrder = attributes.getValue("preserve-order");
      if (preserveOrder != null) {
        m_currentTest.setPreserveOrder(preserveOrder);
      }
      String parallel = attributes.getValue("parallel");
      if (parallel != null) {
        XmlSuite.ParallelMode mode = XmlSuite.ParallelMode.getValidParallel(parallel);
        if (mode != null) {
          m_currentTest.setParallel(mode);
        } else {
          Utils.log("Parser", 1, "[WARN] Unknown value of attribute 'parallel' for test '"
            + m_currentTest.getName() + "': '" + parallel + "'");
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
      popLocation(Location.TEST);
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
      m_currentClassIndex = 0;
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
        switch(m_locations.peek()) {
          case TEST:
            m_currentTest.setXmlPackages(m_currentPackages);
            break;
          case SUITE:
            m_currentSuite.setXmlPackages(m_currentPackages);
            break;
          case CLASS:
            throw new UnsupportedOperationException("CLASS");
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
      m_currentSelectors = new ArrayList<>();
    }
    else {
      switch(m_locations.peek()) {
        case TEST:
          m_currentTest.setMethodSelectors(m_currentSelectors);
          break;
        default:
          m_currentSuite.setMethodSelectors(m_currentSelectors);
          break;
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
      m_currentSelector.setPriority(Integer.parseInt(priority));
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
      m_currentIncludedMethods = new ArrayList<>();
      m_currentExcludedMethods = Lists.newArrayList();
      m_currentIncludeIndex = 0;
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
  public void xmlRun(boolean start, Attributes attributes) throws SAXException {
    if (start) {
      m_currentRuns = Lists.newArrayList();
    }
    else {
      if (m_currentTest != null) {
        m_currentTest.setIncludedGroups(m_currentIncludedGroups);
        m_currentTest.setExcludedGroups(m_currentExcludedGroups);
      } else {
        m_currentSuite.setIncludedGroups(m_currentIncludedGroups);
        m_currentSuite.setExcludedGroups(m_currentExcludedGroups);
      }
      m_currentRuns = null;
    }
  }


  /**
   * Parse <group>
   */
  public void xmlGroup(boolean start, Attributes attributes) throws SAXException {
    if (start) {
      m_currentTest.addXmlDependencyGroup(attributes.getValue("name"),
          attributes.getValue("depends-on"));
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
    if (!m_validate && !m_hasWarn) {
      Logger.getLogger(TestNGContentHandler.class).warn("It is strongly recommended to add " +
              "\"<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\" >\" at the top of your file, " +
              "otherwise TestNG may fail or not work as expected.");
      m_hasWarn = true;
    }
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
        m_currentClass = new XmlClass(name, m_currentClassIndex++, m_loadClasses);
        m_currentClass.setXmlTest(m_currentTest);
        m_currentClassParameters = Maps.newHashMap();
        m_currentClasses.add(m_currentClass);
        pushLocation(Location.CLASS);
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
    else if ("group".equals(qName)) {
      xmlGroup(true, attributes);
    }
    else if ("groups".equals(qName)) {
      m_currentIncludedGroups = Lists.newArrayList();
      m_currentExcludedGroups = Lists.newArrayList();
    }
    else if ("methods".equals(qName)) {
      xmlMethod(true, attributes);
    }
    else if ("include".equals(qName)) {
      xmlInclude(true, attributes);
    }
    else if ("exclude".equals(qName)) {
      xmlExclude(true, attributes);
    }
    else if ("parameter".equals(qName)) {
      String value = expandValue(attributes.getValue("value"));
      switch(m_locations.peek()) {
        case TEST:
          m_currentTestParameters.put(name, value);
          break;
        case SUITE:
          m_currentSuiteParameters.put(name, value);
          break;
        case CLASS:
          m_currentClassParameters.put(name, value);
          break;
        case INCLUDE:
          m_currentInclude.parameters.put(name, value);
          break;
      }
    }
  }

  private static class Include {
    String name;
    String invocationNumbers;
    String description;
    Map<String, String> parameters = Maps.newHashMap();

    public Include(String name, String numbers) {
      this.name = name;
      this.invocationNumbers = numbers;
    }
  }

  private void xmlInclude(boolean start, Attributes attributes) {
    if (start) {
      m_locations.push(Location.INCLUDE);
      m_currentInclude = new Include(attributes.getValue("name"),
          attributes.getValue("invocation-numbers"));
    } else {
      String name = m_currentInclude.name;
      if (null != m_currentIncludedMethods) {
        String in = m_currentInclude.invocationNumbers;
        XmlInclude include;
        if (!Utils.isStringEmpty(in)) {
          include = new XmlInclude(name, stringToList(in), m_currentIncludeIndex++);
        } else {
          include = new XmlInclude(name, m_currentIncludeIndex++);
        }
        for (Map.Entry<String, String> entry : m_currentInclude.parameters.entrySet()) {
          include.addParameter(entry.getKey(), entry.getValue());
        }

        include.setDescription(m_currentInclude.description);
        m_currentIncludedMethods.add(include);
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

      popLocation(Location.INCLUDE);
      m_currentInclude = null;
    }
  }

  private void xmlExclude(boolean start, Attributes attributes) {
    if (start) {
      m_locations.push(Location.EXCLUDE);
      String name = attributes.getValue("name");
      if (null != m_currentExcludedMethods) {
        m_currentExcludedMethods.add(name);
      }
      else if (null != m_currentRuns) {
        m_currentExcludedGroups.add(name);
      }
      else if (null != m_currentPackage) {
        m_currentPackage.getExclude().add(name);
      }
    } else {
      popLocation(Location.EXCLUDE);
    }
  }

  private void pushLocation(Location l) {
    m_locations.push(l);
  }

  private Location popLocation(Location location) {
    return m_locations.pop();
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
    else if ("packages".equals(qName)) {
      xmlPackages(false, null);
    }
    else if ("class".equals(qName)) {
      m_currentClass.setParameters(m_currentClassParameters);
      m_currentClassParameters = null;
      popLocation(Location.CLASS);
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
    else if ("include".equals(qName)) {
      xmlInclude(false, null);
    } else if ("exclude".equals(qName)){
      xmlExclude(false, null);
    }
  }

  @Override
  public void error(SAXParseException e) throws SAXException {
    if (m_validate) {
      throw e;
    }
  }

  private boolean areWhiteSpaces(char[] ch, int start, int length) {
    for (int i = start; i < start + length; i++) {
      char c = ch[i];
      if (c != '\n' && c != '\t' && c != ' ') {
        return false;
      }
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

  private static String expandValue(String value)
  {
    StringBuffer result = null;
    int startIndex = 0;
    int endIndex = 0;
    int startPosition = 0;
    String property = null;
    while ((startIndex = value.indexOf("${", startPosition)) > -1 && (endIndex = value.indexOf("}", startIndex + 3)) > -1) {
      property = value.substring(startIndex + 2, endIndex);
      if (result == null) {
        result = new StringBuffer(value.substring(startPosition, startIndex));
      } else {
        result.append(value.substring(startPosition, startIndex));
      }
      String propertyValue = System.getProperty(property);
      if (propertyValue == null) {
        propertyValue = System.getenv(property);
      }
      if (propertyValue != null) {
        result.append(propertyValue);
      } else {
        result.append("${");
        result.append(property);
        result.append("}");
      }
      startPosition = startIndex + 3 + property.length();
    }
    if (result != null) {
      result.append(value.substring(startPosition));
      return result.toString();
    } else {
      return value;
    }
  }
}
