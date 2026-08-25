package org.testng.xml;

import static org.testng.internal.Utils.isStringBlank;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.XMLConstants;
import org.jspecify.annotations.Nullable;
import org.testng.ITestObjectFactory;
import org.testng.TestNGException;
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
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Suite definition parser utility.
 *
 * @author Cedric Beust
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
// TODO move to internal
public class TestNGContentHandler extends DefaultHandler implements LexicalHandler {
  private static final int DTD_CONNECTION_TIMEOUT_MILLIS = 10_000;

  /** Only the end tag half of the {@code xml*} methods below is called without attributes. */
  private static final String START_TAG_ATTRIBUTES = "a start tag carries its attributes";

  private @Nullable XmlSuite m_currentSuite;
  private @Nullable XmlTest m_currentTest;
  private @Nullable XmlDefine m_currentDefine;
  private @Nullable XmlRun m_currentRun;
  private @Nullable List<XmlClass> m_currentClasses;
  private int m_currentTestIndex = 0;
  private int m_currentClassIndex = 0;
  private int m_currentIncludeIndex = 0;
  private @Nullable List<XmlPackage> m_currentPackages;
  private @Nullable XmlPackage m_currentPackage;
  private final List<XmlSuite> m_suites = new ArrayList<>();
  private @Nullable XmlGroups m_currentGroups;
  private @Nullable Map<String, String> m_currentTestParameters;
  private @Nullable Map<String, String> m_currentSuiteParameters;
  private @Nullable Map<String, String> m_currentClassParameters;
  private @Nullable Include m_currentInclude;

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
          // Buffer the remote DTD so this resolver owns and closes every connection stream.
          return new InputSource(new ByteArrayInputStream(readUrl(url, true)));
        }
        // Buffer the classpath DTD so this resolver, rather than SAX, owns and closes the stream.
        try (InputStream input = stream) {
          return new InputSource(new ByteArrayInputStream(input.readAllBytes()));
        }
      };

  static byte[] readUrl(URL url, boolean followRedirect) throws IOException {
    URLConnection connection = url.openConnection();
    configureConnection(connection);
    if (!(connection instanceof HttpURLConnection)) {
      try (InputStream input = connection.getInputStream()) {
        return input.readAllBytes();
      }
    }

    HttpURLConnection httpConnection = (HttpURLConnection) connection;
    httpConnection.setInstanceFollowRedirects(false);
    try {
      int status = httpConnection.getResponseCode();
      if (followRedirect
          && (status == HttpURLConnection.HTTP_MOVED_TEMP
              || status == HttpURLConnection.HTTP_MOVED_PERM
              || status == HttpURLConnection.HTTP_SEE_OTHER)) {
        return readUrl(new URL(url, httpConnection.getHeaderField("Location")), false);
      }
      try (InputStream input = httpConnection.getInputStream()) {
        return input.readAllBytes();
      }
    } finally {
      httpConnection.disconnect();
    }
  }

  static void configureConnection(URLConnection connection) {
    connection.setConnectTimeout(DTD_CONNECTION_TIMEOUT_MILLIS);
    connection.setReadTimeout(DTD_CONNECTION_TIMEOUT_MILLIS);
  }

  enum Location {
    SUITE,
    TEST,
    CLASS,
    INCLUDE,
    EXCLUDE
  }

  // Deque, not Stack: every use is a push, a pop or a peek, so the one thing the two disagree on --
  // Stack iterates bottom to top, a Deque used as a stack iterates top to bottom -- never comes up.
  private final Deque<Location> m_locations = new ArrayDeque<>();
  private boolean isSuiteFileTag = false;

  private @Nullable XmlClass m_currentClass;
  private @Nullable ArrayList<XmlInclude> m_currentIncludedMethods;
  private @Nullable List<String> m_currentExcludedMethods;
  private @Nullable ArrayList<XmlMethodSelector> m_currentSelectors;
  private @Nullable XmlMethodSelector m_currentSelector;
  private @Nullable String m_currentLanguage;
  private @Nullable String m_currentExpression;
  private final List<String> m_suiteFiles = new ArrayList<>();
  private boolean m_enabledTest;
  private @Nullable List<String> m_listeners;

  private final String m_fileName;
  private final boolean m_loadClasses;
  private boolean m_doctypeDeclared = false;

  /**
   * Whether the parser was given the bundled schema, which {@code XMLParser} decides before the
   * parse starts and only for a suite that declares no doctype.
   *
   * <p>Reporting is gated on having a grammar: without one a validating parser only ever complains
   * that none was found, which would turn the "declare a schema" hint into a hard failure.
   */
  private boolean m_schemaValidated = false;

  /**
   * Whether the root element has been looked at yet. The hint below is decided there and nowhere
   * else: a schema declaration deeper in the document is not one, and re-reading it on every
   * element would let a nested element decide whether the file declared a grammar.
   */
  private boolean m_rootInspected = false;

  /**
   * Resolved once per parse rather than per violation, so a malformed suite cannot re-read the
   * system property -- and re-log the "unknown value" warning -- for every error it produces.
   */
  private final XmlValidationMode m_validationMode = XmlValidationMode.current();

  public TestNGContentHandler(String fileName, boolean loadClasses) {
    m_fileName = fileName;
    m_loadClasses = loadClasses;
  }

  /**
   * Records that the document declares a doctype, whoever ends up providing the grammar.
   *
   * <p>{@link #resolveEntity} cannot be the only signal: it fires solely for an <em>external</em>
   * subset, so a suite declaring {@code <!DOCTYPE suite [ <!ENTITY ...> ]>} never reaches it and
   * used to be treated as having no doctype at all -- advised to add the one it had just written,
   * and with its validity errors discarded. {@code startDTD} covers both kinds, and always fires
   * before any content, so it is the exact signal. A document with no doctype does not trigger it,
   * which is what keeps "no grammar found" errors suppressed for those files.
   *
   * <p>The two overlap deliberately. The lexical handler is optional in SAX, so on a parser that
   * refuses it {@code resolveEntity} still catches the external case rather than losing detection
   * altogether.
   */
  @Override
  public void startDTD(String name, String publicId, String systemId) {
    m_doctypeDeclared = true;
  }

  @Override
  public void endDTD() {}

  @Override
  public void startEntity(String name) {}

  @Override
  public void endEntity(String name) {}

  @Override
  public void startCDATA() {}

  @Override
  public void endCDATA() {}

  @Override
  public void comment(char[] ch, int start, int length) {}

  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException, IOException {

    // Being asked to resolve the external subset is itself proof that a doctype was declared.
    // Redundant with startDTD when the lexical handler could be registered, and the only signal
    // left when it could not -- see XMLParser#registerLexicalHandler.
    m_doctypeDeclared = true;

    if (skipConsideringSystemId(systemId)) {
      InputStream stream = loadDtdUsingClassLoader();
      if (stream != null) {
        // Buffer the classpath DTD so this resolver, rather than SAX, owns and closes the stream.
        try (InputStream input = stream) {
          return new InputSource(new ByteArrayInputStream(input.readAllBytes()));
        }
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

      URL url = new URL(URLDecoder.decode(systemId, StandardCharsets.UTF_8).trim());
      if ("file".equals(url.getProtocol())) {
        File file = new File(url.getFile());
        boolean isDirectory = file.isDirectory();
        boolean fileExists = file.exists();
        return isDirectory || !fileExists;
      }
      return false;
    } catch (MalformedURLException e) {
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
    return "http".equals(uri.getScheme());
  }

  private InputStream loadDtdUsingClassLoader() {
    InputStream is = getClass().getClassLoader().getResourceAsStream(Parser.TESTNG_DTD);
    if (is != null) {
      return is;
    }
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(Parser.TESTNG_DTD);
  }

  // Every m_currentXxx field declared above is set when its element's start tag is seen and cleared
  // at the matching end tag, so it is non-null for the whole of that element's body. A null means
  // the document is not well formed, which SAX reports separately. The accessors below assert it so
  // the failure names the element instead of arriving as a bare NullPointerException.
  private XmlSuite currentSuite() {
    return Objects.requireNonNull(m_currentSuite, "no <suite> is being parsed");
  }

  private XmlTest currentTest() {
    return Objects.requireNonNull(m_currentTest, "no <test> is being parsed");
  }

  private XmlClass currentClass() {
    return Objects.requireNonNull(m_currentClass, "no <class> is being parsed");
  }

  private XmlGroups currentGroups() {
    return Objects.requireNonNull(m_currentGroups, "no <groups> is being parsed");
  }

  private XmlMethodSelector currentSelector() {
    return Objects.requireNonNull(m_currentSelector, "no <method-selector> is being parsed");
  }

  private List<XmlMethodSelector> currentSelectors() {
    return Objects.requireNonNull(m_currentSelectors, "no <method-selectors> is being parsed");
  }

  private Include currentInclude() {
    return Objects.requireNonNull(m_currentInclude, "no <include> is being parsed");
  }

  private Map<String, String> currentSuiteParameters() {
    return Objects.requireNonNull(m_currentSuiteParameters, "no <suite> is being parsed");
  }

  private Map<String, String> currentTestParameters() {
    return Objects.requireNonNull(m_currentTestParameters, "no <test> is being parsed");
  }

  private Map<String, String> currentClassParameters() {
    return Objects.requireNonNull(m_currentClassParameters, "no <class> is being parsed");
  }

  /** Parse <suite-file> */
  private void xmlSuiteFile(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      String path = Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES).getValue("path");
      pushLocation(Location.SUITE);
      m_suiteFiles.add(path);
      isSuiteFileTag = true;
    } else {
      currentSuite().setSuiteFiles(m_suiteFiles);
      popLocation();
      isSuiteFileTag = false;
    }
  }

  /** Parse <suite> */
  private void xmlSuite(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      pushLocation(Location.SUITE);
      Attributes attributes = Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES);
      String name = attributes.getValue("name");
      if (isStringBlank(name)) {
        throw new TestNGException("The <suite> tag must define the name attribute");
      }
      XmlSuite suite = new XmlSuite();
      m_currentSuite = suite;
      suite.setFileName(m_fileName);
      suite.setName(name);
      m_currentSuiteParameters = new HashMap<>();

      String verbose = attributes.getValue("verbose");
      if (null != verbose) {
        suite.setVerbose(Integer.parseInt(verbose));
      }
      String jUnit = attributes.getValue("junit");
      if (null != jUnit) {
        warnSinceJUnitDetected();
      }
      String parallel = attributes.getValue("parallel");
      if (parallel != null) {
        XmlSuite.ParallelMode mode = XmlSuite.ParallelMode.getValidParallel(parallel);
        if (mode != null) {
          suite.setParallel(mode);
        } else {
          Utils.log(
              "Parser",
              1,
              "[WARN] Unknown value of attribute 'parallel' at suite level: '" + parallel + "'.");
        }
      }
      String parentModule = attributes.getValue("parent-module");
      if (parentModule != null) {
        suite.setParentModule(parentModule);
      }
      String guiceStage = attributes.getValue("guice-stage");
      if (guiceStage != null) {
        suite.setGuiceStage(guiceStage);
      }
      XmlSuite.FailurePolicy configFailurePolicy =
          XmlSuite.FailurePolicy.getValidPolicy(attributes.getValue("configfailurepolicy"));
      if (null != configFailurePolicy) {
        suite.setConfigFailurePolicy(configFailurePolicy);
      }
      String groupByInstances = attributes.getValue("group-by-instances");
      if (groupByInstances != null) {
        suite.setGroupByInstances(Boolean.parseBoolean(groupByInstances));
      }
      String lazyFactory = attributes.getValue("lazy-factory");
      if (lazyFactory != null) {
        suite.setLazyFactory(Boolean.parseBoolean(lazyFactory));
      }
      String skip = attributes.getValue("skipfailedinvocationcounts");
      if (skip != null) {
        suite.setSkipFailedInvocationCounts(Boolean.parseBoolean(skip));
      }
      String threadCount = attributes.getValue("thread-count");
      if (null != threadCount) {
        suite.setThreadCount(Integer.parseInt(threadCount));
      }
      String dataProviderThreadCount = attributes.getValue("data-provider-thread-count");
      if (null != dataProviderThreadCount) {
        suite.setDataProviderThreadCount(Integer.parseInt(dataProviderThreadCount));
      }

      String shareThreadPoolForDataProviders =
          attributes.getValue("share-thread-pool-for-data-providers");
      Optional.ofNullable(shareThreadPoolForDataProviders)
          .ifPresent(
              it ->
                  suite.setShareThreadPoolForDataProviders(
                      Boolean.parseBoolean(shareThreadPoolForDataProviders)));

      String useGlobalThreadPool = attributes.getValue("use-global-thread-pool");
      Optional.ofNullable(useGlobalThreadPool)
          .ifPresent(
              it -> suite.shouldUseGlobalThreadPool(Boolean.parseBoolean(useGlobalThreadPool)));

      String timeOut = attributes.getValue("time-out");
      if (null != timeOut) {
        suite.setTimeOut(timeOut);
      }
      String objectFactory = attributes.getValue("object-factory");
      if (null != objectFactory && m_loadClasses) {
        try {
          suite.setObjectFactoryClass(
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
        suite.setPreserveOrder(Boolean.valueOf(preserveOrder));
      }
      String allowReturnValues = attributes.getValue("allow-return-values");
      if (allowReturnValues != null) {
        suite.setAllowReturnValues(Boolean.valueOf(allowReturnValues));
      }
    } else {
      XmlSuite suite = currentSuite();
      suite.setParameters(currentSuiteParameters());
      m_suites.add(suite);
      m_currentSuiteParameters = null;
      popLocation();
    }
  }

  /** Parse <define> */
  private void xmlDefine(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      String name = Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES).getValue("name");
      XmlDefine define = new XmlDefine();
      define.setName(name);
      m_currentDefine = define;
    } else {
      // define is only defined within the context of XmlGroups
      currentGroups()
          .addDefine(Objects.requireNonNull(m_currentDefine, "no <define> is being parsed"));
      m_currentDefine = null;
    }
  }

  /** Parse <script> */
  private void xmlScript(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      m_currentLanguage =
          Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES).getValue("language");
      m_currentExpression = "";
    } else {
      XmlScript script = new XmlScript();
      script.setExpression(
          Objects.requireNonNull(m_currentExpression, "no <script> is being parsed"));
      script.setLanguage(m_currentLanguage);
      currentSelector().setScript(script);
      if (m_locations.peek() == Location.TEST) {
        currentTest().setScript(script);
      }
      m_currentLanguage = null;
      m_currentExpression = null;
    }
  }

  /** Parse &lt;test&gt; */
  private void xmlTest(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      XmlTest test = new XmlTest(currentSuite(), m_currentTestIndex++);
      m_currentTest = test;
      pushLocation(Location.TEST);
      m_currentTestParameters = new HashMap<>();
      Attributes attributes = Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES);
      final String testName = attributes.getValue("name");
      if (isStringBlank(testName)) {
        throw new TestNGException("The <test> tag must define the name attribute");
      }
      test.setName(testName);
      String verbose = attributes.getValue("verbose");
      if (null != verbose) {
        test.setVerbose(Integer.parseInt(verbose));
      }
      String jUnit = attributes.getValue("junit");
      if (null != jUnit) {
        warnSinceJUnitDetected();
      }
      String skip = attributes.getValue("skipfailedinvocationcounts");
      if (skip != null) {
        test.setSkipFailedInvocationCounts(Boolean.parseBoolean(skip));
      }
      String groupByInstances = attributes.getValue("group-by-instances");
      if (groupByInstances != null) {
        test.setGroupByInstances(Boolean.parseBoolean(groupByInstances));
      }
      String preserveOrder = attributes.getValue("preserve-order");
      if (preserveOrder != null) {
        test.setPreserveOrder(Boolean.valueOf(preserveOrder));
      }
      String parallel = attributes.getValue("parallel");
      if (parallel != null) {
        XmlSuite.ParallelMode mode = XmlSuite.ParallelMode.getValidParallel(parallel);
        if (mode != null) {
          test.setParallel(mode);
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
        test.setThreadCount(Integer.parseInt(threadCount));
      }
      String timeOut = attributes.getValue("time-out");
      if (null != timeOut) {
        test.setTimeOut(Long.parseLong(timeOut));
      }
      m_enabledTest = true;
      String enabledTestString = attributes.getValue("enabled");
      if (null != enabledTestString) {
        m_enabledTest = Boolean.parseBoolean(enabledTestString);
      }
    } else {
      XmlTest test = currentTest();
      if (null != m_currentTestParameters && !m_currentTestParameters.isEmpty()) {
        test.setParameters(m_currentTestParameters);
      }
      if (null != m_currentClasses) {
        test.setXmlClasses(m_currentClasses);
      }
      m_currentClasses = null;
      m_currentTest = null;
      m_currentTestParameters = null;
      popLocation();
      if (!m_enabledTest) {
        List<XmlTest> tests = currentSuite().getTests();
        tests.remove(tests.size() - 1);
      }
    }
  }

  public void xmlClasses(boolean start) {
    if (start) {
      m_currentClasses = new ArrayList<>();
      m_currentClassIndex = 0;
    } else {
      currentTest()
          .setXmlClasses(Objects.requireNonNull(m_currentClasses, "no <classes> is being parsed"));
      m_currentClasses = null;
    }
  }

  public void xmlListeners(boolean start) {
    if (start) {
      m_listeners = new ArrayList<>();
    } else {
      if (null != m_listeners) {
        currentSuite().setListeners(m_listeners);
        m_listeners = null;
      }
    }
  }

  public void xmlListener(boolean start, Attributes attributes) {
    if (start) {
      String listener = attributes.getValue("class-name");
      Objects.requireNonNull(m_listeners, "no <listeners> is being parsed").add(listener);
    }
  }

  public void xmlPackages(boolean start) {
    if (start) {
      m_currentPackages = new ArrayList<>();
    } else {
      if (null != m_currentPackages) {
        Location location = m_locations.peek();
        switch (location) {
          case TEST:
            currentTest().setXmlPackages(m_currentPackages);
            break;
          case SUITE:
            currentSuite().setXmlPackages(m_currentPackages);
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
    List<XmlMethodSelector> selectors = currentSelectors();
    if (m_locations.peek() == Location.TEST) {
      currentTest().setMethodSelectors(selectors);
    } else {
      currentSuite().setMethodSelectors(selectors);
    }

    m_currentSelectors = null;
  }

  public void xmlSelectorClass(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      Attributes attributes = Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES);
      XmlMethodSelector selector = currentSelector();
      selector.setName(attributes.getValue("name"));
      String priority = attributes.getValue("priority");
      selector.setPriority(
          priority == null ? XmlMethodSelector.DEFAULT_PRIORITY : Integer.parseInt(priority));
    }
  }

  public void xmlMethodSelector(boolean start) {
    if (start) {
      m_currentSelector = new XmlMethodSelector();
    } else {
      currentSelectors().add(currentSelector());
      m_currentSelector = null;
    }
  }

  private void xmlMethod(boolean start) {
    if (start) {
      m_currentIncludedMethods = new ArrayList<>();
      m_currentExcludedMethods = new ArrayList<>();
      m_currentIncludeIndex = 0;
    } else {
      XmlClass xmlClass = currentClass();
      xmlClass.setIncludedMethods(
          Objects.requireNonNull(m_currentIncludedMethods, "no <methods> is being parsed"));
      xmlClass.setExcludedMethods(
          Objects.requireNonNull(m_currentExcludedMethods, "no <methods> is being parsed"));
      m_currentIncludedMethods = null;
      m_currentExcludedMethods = null;
    }
  }

  public void xmlRun(boolean start) {
    if (start) {
      m_currentRun = new XmlRun();
    } else {
      // Xml run is only defined in the context of groups
      currentGroups().setRun(Objects.requireNonNull(m_currentRun, "no <run> is being parsed"));
      m_currentRun = null;
    }
  }

  public void xmlGroup(boolean start, Attributes attributes) {
    if (start) {
      currentTest()
          .addXmlDependencyGroup(attributes.getValue("name"), attributes.getValue("depends-on"));
    }
  }

  public void xmlGroups(boolean start) {
    if (start) {
      m_currentGroups = new XmlGroups();
    } else {
      XmlGroups groups = currentGroups();
      if (m_currentTest == null) {
        currentSuite().setGroups(groups);
      } else {
        currentTest().setGroups(groups);
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
    if (!m_rootInspected) {
      m_rootInspected = true;
      warnIfNoGrammarIsDeclared(attributes);
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
        XmlClass xmlClass = new XmlClass(name, m_currentClassIndex++, m_loadClasses);
        m_currentClass = xmlClass;
        xmlClass.setXmlTest(currentTest());
        m_currentClassParameters = new HashMap<>();
        m_currentClasses.add(xmlClass);
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
      if (isSuiteFileTag) {
        // do-not process a <parameter> tag when it is specified inside <suite-files>
        Logger.getLogger(getClass())
            .warn(
                "Ignoring the <parameter> tag because it is specified inside a <suite-file> tag.");
        return;
      }
      String value = expandValue(attributes.getValue("value"));
      Location location = m_locations.peek();
      switch (location) {
        case TEST:
          currentTestParameters().put(name, value);
          break;
        case SUITE:
          currentSuiteParameters().put(name, value);
          break;
        case CLASS:
          currentClassParameters().put(name, value);
          break;
        case INCLUDE:
          currentInclude().parameters.put(name, value);
          break;
        default:
          throw new AssertionError("Unexpected value: " + location);
      }
    }
  }

  private static class Include {
    String name;
    String invocationNumbers;
    String factoryInstances;
    @Nullable String description;
    Map<String, String> parameters = new HashMap<>();

    Include(String name, String numbers, String factoryInstances) {
      this.name = name;
      this.invocationNumbers = numbers;
      this.factoryInstances = factoryInstances;
    }
  }

  private void xmlInclude(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      m_locations.push(Location.INCLUDE);
      Attributes attributes = Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES);
      Include current =
          new Include(
              attributes.getValue("name"),
              attributes.getValue("invocation-numbers"),
              attributes.getValue("factory-instances"));
      current.description = attributes.getValue("description");
      m_currentInclude = current;
    } else {
      Include current = currentInclude();
      String name = current.name;
      if (null != m_currentIncludedMethods) {
        String in = current.invocationNumbers;
        XmlInclude include;
        if (!Utils.isStringEmpty(in)) {
          include = new XmlInclude(name, stringToList(in), m_currentIncludeIndex++);
        } else {
          include = new XmlInclude(name, m_currentIncludeIndex++);
        }
        String factoryInstances = current.factoryInstances;
        if (!Utils.isStringEmpty(factoryInstances)) {
          include.addFactoryInstances(stringToList(factoryInstances));
        }
        for (Map.Entry<String, String> entry : current.parameters.entrySet()) {
          include.addParameter(entry.getKey(), entry.getValue());
        }

        include.setDescription(current.description);
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

  private void xmlExclude(boolean start, @Nullable Attributes startAttributes) {
    if (start) {
      m_locations.push(Location.EXCLUDE);
      String name = Objects.requireNonNull(startAttributes, START_TAG_ATTRIBUTES).getValue("name");
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
    List<Integer> result = new ArrayList<>();
    for (String n : Utils.splitOnLiteral(in, " ")) {
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
      currentClass().setParameters(currentClassParameters());
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

  /**
   * Told by {@code XMLParser} whether it attached the bundled schema, which is the only thing that
   * gives a doctype-less suite a grammar to be judged against.
   */
  void setSchemaValidated(boolean schemaValidated) {
    m_schemaValidated = schemaValidated;
  }

  /**
   * Records the doctype {@code XMLParser} saw in the prologue, before the parse began.
   *
   * <p>A second, independent source for what {@link #startDTD} reports. The lexical handler is
   * optional in SAX, and on a parser that refuses it a doctype with only an internal subset is
   * reported by nothing -- {@link #resolveEntity} fires solely for an external subset. Such a suite
   * was then advised to declare the grammar it had just declared, and had its violations discarded
   * even under strict. The prologue is read whatever the parser supports, so it closes that gap.
   */
  void doctypeDeclared() {
    m_doctypeDeclared = true;
  }

  /**
   * Advises declaring a grammar when the document has none, once, on the root element.
   *
   * <p>The schema comes first: it is what TestNG writes and what it recommends. The doctype is
   * still offered, because it is what every existing suite file carries and it stays supported.
   */
  private void warnIfNoGrammarIsDeclared(Attributes attributes) {
    if (m_doctypeDeclared || declaresASchema(attributes)) {
      return;
    }
    Logger.getLogger(TestNGContentHandler.class)
        .warn(
            String.format(
                "It is strongly recommended to declare a schema at the top of the suite file [%s],"
                    + " either xsi:noNamespaceSchemaLocation=\"%s\" on <suite> (recommended) or"
                    + " \"<!DOCTYPE suite SYSTEM \"%s\" >\", otherwise TestNG may fail or not work"
                    + " as expected.",
                this.m_fileName, XMLParser.HTTPS_TESTNG_XSD_URL, Parser.HTTPS_TESTNG_DTD_URL));
  }

  /**
   * Whether the element declares a schema, in either spelling and whether or not the parser was
   * namespace aware -- the DTD path leaves namespace awareness off, so the attribute arrives only
   * under its qualified name there.
   */
  private static boolean declaresASchema(Attributes attributes) {
    return attributes.getValue(
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "noNamespaceSchemaLocation")
            != null
        || attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation")
            != null
        || attributes.getValue("xsi:noNamespaceSchemaLocation") != null
        || attributes.getValue("xsi:schemaLocation") != null;
  }

  @Override
  public void error(SAXParseException e) throws SAXException {
    if (!m_doctypeDeclared && !m_schemaValidated) {
      // With no grammar a validating parser only ever complains that none was found, which would
      // turn the "declare a schema" hint into a hard failure.
      return;
    }
    switch (m_validationMode) {
      case STRICT:
        throw e;
      case WARN:
        Logger.getLogger(TestNGContentHandler.class)
            .warn(
                "The suite file ["
                    + m_fileName
                    + "] does not conform to "
                    + (m_schemaValidated ? XMLParser.TESTNG_XSD : Parser.TESTNG_DTD)
                    + ": "
                    + e.getMessage()
                    + ". Run with [-D"
                    + RuntimeBehavior.XML_VALIDATION_MODE
                    + "=strict] to turn this into a failure.");
        break;
      case OFF:
        break;
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

  public @Nullable XmlSuite getSuite() {
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

  private static void warnSinceJUnitDetected() {
    String msg =
        "Ability to run JUnit tests via TestNG was deprecated in v7.7.0 and is NOW being removed permanently."
            + " If you would like to run a mixture of TestNG and JUnit tests then please take a look at "
            + " https://github.com/junit-team/testng-engine which is now maintained by the JUnit team";
    // Intentionally logging this to the error console so that it's visible to the user.
    System.err.println(msg);
  }
}
