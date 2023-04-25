package org.testng.xml;

import static org.testng.internal.Utils.isStringBlank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.util.Strings;
import org.testng.xml.internal.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Suite definition parser utility.
 *
 * @author Cedric Beust
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
// TODO move to internal
public class TestNGContentHandler extends DefaultHandler {
  private XmlSuite m_currentSuite = null;
  private XmlTest m_currentTest = null;
  private XmlDefine m_currentDefine = null;
  private XmlRun m_currentRun = null;
  private List<XmlClass> m_currentClasses = null;
  private int m_currentTestIndex = 0;
  private int m_currentClassIndex = 0;
  private int m_currentIncludeIndex = 0;
  private List<XmlPackage> m_currentPackages = null;
  private XmlPackage m_currentPackage = null;
  private final List<XmlSuite> m_suites = Lists.newArrayList();
  private XmlGroups m_currentGroups = null;
  private Map<String, String> m_currentTestParameters = null;
  private Map<String, String> m_currentSuiteParameters = null;
  private Map<String, String> m_currentClassParameters = null;
  private Include m_currentInclude;

  // Borrowed this implementation from this SO post : https://stackoverflow.com/a/29751441/679824
  private final EntityResolver m_redirectionAwareResolver =
      (publicId, systemId) -> {
        URL url = new URL(systemId);
        InputStream stream = getClass().getResourceAsStream(url.getPath());
        if (stream == null) {
          String msg =
              String.format(
                  "Failed to read [%s] from CLASSPATH. " + "Attempting to read from [%s].",
                  url.getPath(), systemId);
          Logger.getLogger(getClass()).warn(msg);
          URLConnection urlConnection = url.openConnection();
          if (urlConnection instanceof HttpURLConnection) {
            HttpURLConnection conn = (HttpURLConnection) urlConnection;

            int status = conn.getResponseCode();
            if ((status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_SEE_OTHER)) {

              String newUrl = conn.getHeaderField("Location");
              conn = (HttpURLConnection) new URL(newUrl).openConnection();
            }
            stream = conn.getInputStream();
          } else {
            stream = urlConnection.getInputStream();
          }
        }
        return new InputSource(
            Objects.requireNonNull(stream, "Failed to load DTD from " + systemId));
      };

  enum Location {
    SUITE,
    TEST,
    CLASS,
    INCLUDE,
    EXCLUDE
  }

  private final Stack<Location> m_locations = new Stack<>();

  private XmlClass m_currentClass = null;
  private ArrayList<XmlInclude> m_currentIncludedMethods = null;
  private List<String> m_currentExcludedMethods = null;
  private ArrayList<XmlMethodSelector> m_currentSelectors = null;
  private XmlMethodSelector m_currentSelector = null;
  private String m_currentLanguage = null;
  private String m_currentExpression = null;
  private final List<String> m_suiteFiles = Lists.newArrayList();
  private boolean m_enabledTest;
  private List<String> m_listeners;

  private final String m_fileName;
  private final boolean m_loadClasses;
  private boolean m_validate = false;
  private boolean m_hasWarn = false;

  public TestNGContentHandler(String fileName, boolean loadClasses) {
    m_fileName = fileName;
    m_loadClasses = loadClasses;
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException, IOException {

    if (skipConsideringSystemId(systemId)) {
      m_validate = true;
      InputStream is = loadDtdUsingClassLoader();
      if (is != null) {
        return new InputSource(is);
      }
      // If the classpath loading of DTD fails, then we try to load it from "https" TestNG site.
      System.out.println(
          "WARNING: couldn't find in classpath "
              + systemId
              + "\n"
              + "Fetching it from "
              + Parser.HTTPS_TESTNG_DTD_URL);
      return m_redirectionAwareResolver.resolveEntity(publicId, Parser.HTTPS_TESTNG_DTD_URL);
    }
    // If we are here, then we don't know the host from which user is trying to load the dtd
    if (RuntimeBehavior.useSecuredUrlForDtd() && isUnsecuredUrl(systemId)) {
      throw new TestNGException(RuntimeBehavior.unsecuredUrlDocumentation());
    }
    return m_redirectionAwareResolver.resolveEntity(publicId, systemId);
  }

  private static boolean skipConsideringSystemId(String systemId) {
    return Strings.isNullOrEmpty(systemId)
        || TestNGURLs.isDTDDomainInternallyKnownToTestNG(systemId)
        || isMalformedFileSystemBasedSystemId(systemId);
  }

  private static boolean isMalformedFileSystemBasedSystemId(String systemId) {
    try {

      URL url = new URL(URLDecoder.decode(systemId, StandardCharsets.UTF_8.name()).trim());
      if (url.getProtocol().equals("file")) {
        File file = new File(url.getFile());
        boolean isDirectory = file.isDirectory();
        boolean fileExists = file.exists();
        return isDirectory || !fileExists;
      }
      return false;
    } catch (MalformedURLException | UnsupportedEncodingException e) {
      return true;
    }
  }

  private static boolean isUnsecuredUrl(String str) {
    URI uri;
    try {
      uri = new URI(str);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    // scheme is null for local uri
    return uri.getScheme() != null && uri.getScheme().equals("http");
  }

  private InputStream loadDtdUsingClassLoader() {
    InputStream is = getClass().getClassLoader().getResourceAsStream(Parser.TESTNG_DTD);
    if (is != null) {
      return is;
    }
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(Parser.TESTNG_DTD);
  }

  /** Parse <suite-file> */
  private void xmlSuiteFile(boolean start, Attributes attributes) {
    if (start) {
      String path = attributes.getValue("path");
      pushLocation(Location.SUITE);
      m_suiteFiles.add(path);
    } else {
      m_currentSuite.setSuiteFiles(m_suiteFiles);
      popLocation();
    }
  }

  /** Parse <suite> */
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
          Utils.log(
              "Parser",
              1,
              "[WARN] Unknown value of attribute 'parallel' at suite level: '" + parallel + "'.");
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
      XmlSuite.FailurePolicy configFailurePolicy =
          XmlSuite.FailurePolicy.getValidPolicy(attributes.getValue("configfailurepolicy"));
      if (null != configFailurePolicy) {
        m_currentSuite.setConfigFailurePolicy(configFailurePolicy);
      }
      String groupByInstances = attributes.getValue("group-by-instances");
      if (groupByInstances != null) {
        m_currentSuite.setGroupByInstances(Boolean.parseBoolean(groupByInstances));
      }
      String skip = attributes.getValue("skipfailedinvocationcounts");
      if (skip != null) {
        m_currentSuite.setSkipFailedInvocationCounts(Boolean.parseBoolean(skip));
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
          m_currentSuite.setObjectFactoryClass(
              (Class<? extends ITestObjectFactory>) Class.forName(objectFactory));
        } catch (Exception e) {
          Utils.log(
              "Parser",
              1,
              "[ERROR] Unable to create custom object factory '" + objectFactory + "' :" + e);
        }
      }
      String preserveOrder = attributes.getValue("preserve-order");
      if (preserveOrder != null) {
        m_currentSuite.setPreserveOrder(Boolean.valueOf(preserveOrder));
      }
      String allowReturnValues = attributes.getValue("allow-return-values");
      if (allowReturnValues != null) {
        m_currentSuite.setAllowReturnValues(Boolean.valueOf(allowReturnValues));
      }
    } else {
      m_currentSuite.setParameters(m_currentSuiteParameters);
      m_suites.add(m_currentSuite);
      m_currentSuiteParameters = null;
      popLocation();
    }
  }

  /** Parse <define> */
  private void xmlDefine(boolean start, Attributes attributes) {
    if (start) {
      String name = attributes.getValue("name");
      m_currentDefine = new XmlDefine();
      m_currentDefine.setName(name);
    } else {
      // define is only defined within the context of XmlGroups
      m_currentGroups.addDefine(m_currentDefine);
      m_currentDefine = null;
    }
  }

  /** Parse <script> */
  private void xmlScript(boolean start, Attributes attributes) {
    if (start) {
      m_currentLanguage = attributes.getValue("language");
      m_currentExpression = "";
    } else {
      XmlScript script = new XmlScript();
      script.setExpression(m_currentExpression);
      script.setLanguage(m_currentLanguage);
      m_currentSelector.setScript(script);
      if (m_locations.peek() == Location.TEST) {
        m_currentTest.setScript(script);
      }
      m_currentLanguage = null;
      m_currentExpression = null;
    }
  }

  /** Parse &lt;test&gt; */
  private void xmlTest(boolean start, Attributes attributes) {
    if (start) {
      m_currentTest = new XmlTest(m_currentSuite, m_currentTestIndex++);
      pushLocation(Location.TEST);
      m_currentTestParameters = Maps.newHashMap();
      final String testName = attributes.getValue("name");
      if (isStringBlank(testName)) {
        throw new TestNGException("The <test> tag must define the name attribute");
      }
      m_currentTest.setName(attributes.getValue("name"));
      String verbose = attributes.getValue("verbose");
      if (null != verbose) {
        m_currentTest.setVerbose(Integer.parseInt(verbose));
      }
      String jUnit = attributes.getValue("junit");
      if (null != jUnit) {
        m_currentTest.setJUnit(Boolean.parseBoolean(jUnit));
      }
      String skip = attributes.getValue("skipfailedinvocationcounts");
      if (skip != null) {
        m_currentTest.setSkipFailedInvocationCounts(Boolean.parseBoolean(skip));
      }
      String groupByInstances = attributes.getValue("group-by-instances");
      if (groupByInstances != null) {
        m_currentTest.setGroupByInstances(Boolean.parseBoolean(groupByInstances));
      }
      String preserveOrder = attributes.getValue("preserve-order");
      if (preserveOrder != null) {
        m_currentTest.setPreserveOrder(Boolean.valueOf(preserveOrder));
      }
      String parallel = attributes.getValue("parallel");
      if (parallel != null) {
        XmlSuite.ParallelMode mode = XmlSuite.ParallelMode.getValidParallel(parallel);
        if (mode != null) {
          m_currentTest.setParallel(mode);
        } else {
          Utils.log(
              "Parser",
              1,
              "[WARN] Unknown value of attribute 'parallel' for test '"
                  + m_currentTest.getName()
                  + "': '"
                  + parallel
                  + "'");
        }
      }
      String threadCount = attributes.getValue("thread-count");
      if (null != threadCount) {
        m_currentTest.setThreadCount(Integer.parseInt(threadCount));
      }
      String timeOut = attributes.getValue("time-out");
      if (null != timeOut) {
        m_currentTest.setTimeOut(Long.parseLong(timeOut));
      }
      m_enabledTest = true;
      String enabledTestString = attributes.getValue("enabled");
      if (null != enabledTestString) {
        m_enabledTest = Boolean.parseBoolean(enabledTestString);
      }
    } else {
      if (null != m_currentTestParameters && m_currentTestParameters.size() > 0) {
        m_currentTest.setParameters(m_currentTestParameters);
      }
      if (null != m_currentClasses) {
        m_currentTest.setXmlClasses(m_currentClasses);
      }
      m_currentClasses = null;
      m_currentTest = null;
      m_currentTestParameters = null;
      popLocation();
      if (!m_enabledTest) {
        List<XmlTest> tests = m_currentSuite.getTests();
        tests.remove(tests.size() - 1);
      }
    }
  }

  public void xmlClasses(boolean start) {
    if (start) {
      m_currentClasses = Lists.newArrayList();
      m_currentClassIndex = 0;
    } else {
      m_currentTest.setXmlClasses(m_currentClasses);
      m_currentClasses = null;
    }
  }

  public void xmlListeners(boolean start) {
    if (start) {
      m_listeners = Lists.newArrayList();
    } else {
      if (null != m_listeners) {
        m_currentSuite.setListeners(m_listeners);
        m_listeners = null;
      }
    }
  }

  public void xmlListener(boolean start, Attributes attributes) {
    if (start) {
      String listener = attributes.getValue("class-name");
      m_listeners.add(listener);
    }
  }

  public void xmlPackages(boolean start) {
    if (start) {
      m_currentPackages = Lists.newArrayList();
    } else {
      if (null != m_currentPackages) {
        Location location = m_locations.peek();
        switch (location) {
          case TEST:
            m_currentTest.setXmlPackages(m_currentPackages);
            break;
          case SUITE:
            m_currentSuite.setXmlPackages(m_currentPackages);
            break;
          case CLASS:
            throw new UnsupportedOperationException("CLASS");
          default:
            throw new AssertionError("Unexpected value: " + location);
        }
      }

      m_currentPackages = null;
      m_currentPackage = null;
    }
  }

  public void xmlMethodSelectors(boolean start) {
    if (start) {
      m_currentSelectors = new ArrayList<>();
      return;
    }
    if (m_locations.peek() == Location.TEST) {
      m_currentTest.setMethodSelectors(m_currentSelectors);
    } else {
      m_currentSuite.setMethodSelectors(m_currentSelectors);
    }

    m_currentSelectors = null;
  }

  public void xmlSelectorClass(boolean start, Attributes attributes) {
    if (start) {
      m_currentSelector.setName(attributes.getValue("name"));
      String priority = attributes.getValue("priority");
      if (priority == null) {
        priority = "0";
      }
      m_currentSelector.setPriority(Integer.parseInt(priority));
    }
  }

  public void xmlMethodSelector(boolean start) {
    if (start) {
      m_currentSelector = new XmlMethodSelector();
    } else {
      m_currentSelectors.add(m_currentSelector);
      m_currentSelector = null;
    }
  }

  private void xmlMethod(boolean start) {
    if (start) {
      m_currentIncludedMethods = new ArrayList<>();
      m_currentExcludedMethods = Lists.newArrayList();
      m_currentIncludeIndex = 0;
    } else {
      m_currentClass.setIncludedMethods(m_currentIncludedMethods);
      m_currentClass.setExcludedMethods(m_currentExcludedMethods);
      m_currentIncludedMethods = null;
      m_currentExcludedMethods = null;
    }
  }

  public void xmlRun(boolean start) {
    if (start) {
      m_currentRun = new XmlRun();
    } else {
      // Xml run is only defined in the context of groups
      m_currentGroups.setRun(m_currentRun);
      m_currentRun = null;
    }
  }

  public void xmlGroup(boolean start, Attributes attributes) {
    if (start) {
      m_currentTest.addXmlDependencyGroup(
          attributes.getValue("name"), attributes.getValue("depends-on"));
    }
  }

  public void xmlGroups(boolean start) {
    if (start) {
      m_currentGroups = new XmlGroups();
    } else {
      if (m_currentTest == null) {
        m_currentSuite.setGroups(m_currentGroups);
      } else {
        m_currentTest.setGroups(m_currentGroups);
      }

      m_currentGroups = null;
    }
  }

  /**
   * NOTE: I only invoke xml*methods (e.g. xmlSuite()) if I am acting on both the start and the end
   * of the tag. This way I can keep the treatment of this tag in one place. If I am only doing
   * something when the tag opens, the code is inlined below in the startElement() method.
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (!m_validate && !m_hasWarn) {
      String msg =
          String.format(
              "It is strongly recommended to add "
                  + "\"<!DOCTYPE suite SYSTEM \"%s\" >\" at the top of the suite file [%s]"
                  + " otherwise TestNG may fail or not work as expected.",
              Parser.HTTPS_TESTNG_DTD_URL, this.m_fileName);
      Logger.getLogger(TestNGContentHandler.class).warn(msg);
      m_hasWarn = true;
    }
    String name = attributes.getValue("name");

    // ppp("START ELEMENT uri:" + uri + " sName:" + localName + " qName:" + qName +
    // " " + attributes);
    if ("suite".equals(qName)) {
      xmlSuite(true, attributes);
    } else if ("suite-file".equals(qName)) {
      xmlSuiteFile(true, attributes);
    } else if ("test".equals(qName)) {
      xmlTest(true, attributes);
    } else if ("script".equals(qName)) {
      xmlScript(true, attributes);
    } else if ("method-selector".equals(qName)) {
      xmlMethodSelector(true);
    } else if ("method-selectors".equals(qName)) {
      xmlMethodSelectors(true);
    } else if ("selector-class".equals(qName)) {
      xmlSelectorClass(true, attributes);
    } else if ("classes".equals(qName)) {
      xmlClasses(true);
    } else if ("packages".equals(qName)) {
      xmlPackages(true);
    } else if ("listeners".equals(qName)) {
      xmlListeners(true);
    } else if ("listener".equals(qName)) {
      xmlListener(true, attributes);
    } else if ("class".equals(qName)) {
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
    } else if ("package".equals(qName)) {
      if (null != m_currentPackages) {
        m_currentPackage = new XmlPackage();
        m_currentPackage.setName(name);
        m_currentPackages.add(m_currentPackage);
      }
    } else if ("define".equals(qName)) {
      xmlDefine(true, attributes);
    } else if ("run".equals(qName)) {
      xmlRun(true);
    } else if ("group".equals(qName)) {
      xmlGroup(true, attributes);
    } else if ("groups".equals(qName)) {
      xmlGroups(true);
    } else if ("methods".equals(qName)) {
      xmlMethod(true);
    } else if ("include".equals(qName)) {
      xmlInclude(true, attributes);
    } else if ("exclude".equals(qName)) {
      xmlExclude(true, attributes);
    } else if ("parameter".equals(qName)) {
      String value = expandValue(attributes.getValue("value"));
      Location location = m_locations.peek();
      switch (location) {
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
        default:
          throw new AssertionError("Unexpected value: " + location);
      }
    }
  }

  private static class Include {
    String name;
    String invocationNumbers;
    String description;
    Map<String, String> parameters = Maps.newHashMap();

    Include(String name, String numbers) {
      this.name = name;
      this.invocationNumbers = numbers;
    }
  }

  private void xmlInclude(boolean start, Attributes attributes) {
    if (start) {
      m_locations.push(Location.INCLUDE);
      m_currentInclude =
          new Include(attributes.getValue("name"), attributes.getValue("invocation-numbers"));
      m_currentInclude.description = attributes.getValue("description");
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
      } else if (null != m_currentDefine) {
        m_currentDefine.onElement(name);
      } else if (null != m_currentRun) {
        m_currentRun.onInclude(name);
      } else if (null != m_currentPackage) {
        m_currentPackage.getInclude().add(name);
      }

      popLocation();
      m_currentInclude = null;
    }
  }

  private void xmlExclude(boolean start, Attributes attributes) {
    if (start) {
      m_locations.push(Location.EXCLUDE);
      String name = attributes.getValue("name");
      if (null != m_currentExcludedMethods) {
        m_currentExcludedMethods.add(name);
      } else if (null != m_currentRun) {
        m_currentRun.onExclude(name);
      } else if (null != m_currentPackage) {
        m_currentPackage.getExclude().add(name);
      }
    } else {
      popLocation();
    }
  }

  private void pushLocation(Location l) {
    m_locations.push(l);
  }

  private void popLocation() {
    m_locations.pop();
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
  public void endElement(String uri, String localName, String qName) {
    if ("suite".equals(qName)) {
      xmlSuite(false, null);
    } else if ("suite-file".equals(qName)) {
      xmlSuiteFile(false, null);
    } else if ("test".equals(qName)) {
      xmlTest(false, null);
    } else if ("define".equals(qName)) {
      xmlDefine(false, null);
    } else if ("run".equals(qName)) {
      xmlRun(false);
    } else if ("groups".equals(qName)) {
      xmlGroups(false);
    } else if ("methods".equals(qName)) {
      xmlMethod(false);
    } else if ("classes".equals(qName)) {
      xmlClasses(false);
    } else if ("packages".equals(qName)) {
      xmlPackages(false);
    } else if ("class".equals(qName)) {
      m_currentClass.setParameters(m_currentClassParameters);
      m_currentClassParameters = null;
      popLocation();
    } else if ("listeners".equals(qName)) {
      xmlListeners(false);
    } else if ("method-selector".equals(qName)) {
      xmlMethodSelector(false);
    } else if ("method-selectors".equals(qName)) {
      xmlMethodSelectors(false);
    } else if ("selector-class".equals(qName)) {
      xmlSelectorClass(false, null);
    } else if ("script".equals(qName)) {
      xmlScript(false, null);
    } else if ("include".equals(qName)) {
      xmlInclude(false, null);
    } else if ("exclude".equals(qName)) {
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
  public void characters(char[] ch, int start, int length) {
    if (null != m_currentLanguage && !areWhiteSpaces(ch, start, length)) {
      m_currentExpression += new String(ch, start, length);
    }
  }

  public XmlSuite getSuite() {
    return m_currentSuite;
  }

  private static String expandValue(String value) {
    StringBuilder result = null;
    int startIndex;
    int endIndex;
    int startPosition = 0;
    String property;
    while ((startIndex = value.indexOf("${", startPosition)) > -1
        && (endIndex = value.indexOf("}", startIndex + 3)) > -1) {
      property = value.substring(startIndex + 2, endIndex);
      if (result == null) {
        result = new StringBuilder(value.substring(startPosition, startIndex));
      } else {
        result.append(value, startPosition, startIndex);
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
