package org.testng;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.internal.ClassHelper;
import org.testng.internal.DynamicGraph;
import org.testng.internal.IConfiguration;
import org.testng.internal.IResultListener;
import org.testng.internal.TestNGGuiceModule;
import org.testng.internal.Utils;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.Sets;
import org.testng.internal.thread.graph.GraphThreadPoolExecutor;
import org.testng.internal.thread.graph.IThreadWorkerFactory;
import org.testng.internal.thread.graph.SuiteWorkerFactory;
import org.testng.internal.version.VersionInfo;
import org.testng.log4testng.Logger;
import org.testng.remote.SuiteDispatcher;
import org.testng.remote.SuiteSlave;
import org.testng.reporters.EmailableReporter;
import org.testng.reporters.FailedReporter;
import org.testng.reporters.JUnitReportReporter;
import org.testng.reporters.SuiteHTMLReporter;
import org.testng.reporters.XMLReporter;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class is the main entry point for running tests in the TestNG framework.
 * Users can create their own TestNG object and invoke it in many different
 * ways:
 * <ul>
 * <li>On an existing testng.xml
 * <li>On a synthetic testng.xml, created entirely from Java
 * <li>By directly setting the test classes
 * </ul>
 * You can also define which groups to include or exclude, assign parameters, etc...
 * <P/>
 * The command line parameters are:
 * <UL>
 *  <LI>-d <TT>outputdir</TT>: specify the output directory</LI>
 *  <LI>-testclass <TT>class_name</TT>: specifies one or several class names </li>
 *  <LI>-testjar <TT>jar_name</TT>: specifies the jar containing the tests</LI>
 *  <LI>-sourcedir <TT>src1;src2</TT>: ; separated list of source directories
 *    (used only when javadoc annotations are used)</LI>
 *  <LI>-target</LI>
 *  <LI>-groups</LI>
 *  <LI>-testrunfactory</LI>
 *  <LI>-listener</LI>
 * </UL>
 * <P/>
 * Please consult documentation for more details.
 *
 * FIXME: should support more than simple paths for suite xmls
 *
 * @see #usage()
 *
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alex Popescu</a>
 */
public class TestNG {

  /** This class' log4testng Logger. */
  private static final Logger LOGGER = Logger.getLogger(TestNG.class);

  /** The default name for a suite launched from the command line */
  public static final String DEFAULT_COMMAND_LINE_SUITE_NAME = "Command line suite";

  /** The default name for a test launched from the command line */
  public static final String DEFAULT_COMMAND_LINE_TEST_NAME = "Command line test";

  /** The default name of the result's output directory. */
  public static final String DEFAULT_OUTPUTDIR = "test-output";

  /** A separator constant (semi-colon). */
  public static final String SRC_SEPARATOR = ";";

  /** The JDK50 annotation type ID ("JDK5").*/
  public static final String JDK_ANNOTATION_TYPE = AnnotationTypeEnum.JDK.getName();

  /** The JavaDoc annotation type ID ("javadoc"). */
  public static final String JAVADOC_ANNOTATION_TYPE = AnnotationTypeEnum.JAVADOC.getName();

  /** System properties */
  public static final String SHOW_TESTNG_STACK_FRAMES = "testng.show.stack.frames";
  public static final String TEST_CLASSPATH = "testng.test.classpath";

  private static TestNG m_instance;

  private static JCommander m_jCommander;

  private List<String> m_commandLineMethods;
  protected List<XmlSuite> m_suites = Lists.newArrayList();
  protected List<XmlSuite> m_cmdlineSuites;
  protected String m_outputDir = DEFAULT_OUTPUTDIR;

  /** The source directories as set by setSourcePath (or testng-sourcedir-override.properties). */
  protected String[] m_sourceDirs;

  /**
   * The annotation type for suites/tests that have not explicitly set this attribute.
   * This member used to be protected but has been changed to private use the sewtTarget
   * method instead.
   */
  private AnnotationTypeEnum m_defaultAnnotations = VersionInfo.getDefaultAnnotationType();

  protected String[] m_includedGroups;
  protected String[] m_excludedGroups;

  private Boolean m_isJUnit = Boolean.FALSE;
  protected boolean m_useDefaultListeners = true;

  protected ITestRunnerFactory m_testRunnerFactory;

  // These listeners can be overridden from the command line
  protected List<ITestListener> m_testListeners = Lists.newArrayList();
  protected List<ISuiteListener> m_suiteListeners = Lists.newArrayList();
  private Set<IReporter> m_reporters = Sets.newHashSet();

  public static final int HAS_FAILURE = 1;
  public static final int HAS_SKIPPED = 2;
  public static final int HAS_FSP = 4;
  public static final int HAS_NO_TEST = 8;

  protected int m_status;
  protected boolean m_hasTests= false;

  private String m_slavefileName = null;
  private String m_masterfileName = null;

  // Command line suite parameters
  private int m_threadCount;
  private boolean m_useThreadCount;
  private String m_parallelMode;
  private boolean m_useParallelMode;
  private String m_configFailurePolicy;
  private Class[] m_commandLineTestClasses;

  private String m_defaultSuiteName=DEFAULT_COMMAND_LINE_SUITE_NAME;
  private String m_defaultTestName=DEFAULT_COMMAND_LINE_TEST_NAME;

  private Map<String, Integer> m_methodDescriptors = Maps.newHashMap();

  private IObjectFactory m_objectFactory;

  protected List<IInvokedMethodListener> m_invokedMethodListeners = Lists.newArrayList();

  private Integer m_dataProviderThreadCount = null;

  private String m_jarPath;

  private List<String> m_stringSuites = Lists.newArrayList();

  private IHookable m_hookable;
  private IConfigurable m_configurable;

  /**
   * Default constructor. Setting also usage of default listeners/reporters.
   */
  public TestNG() {
    init(true);
  }

  /**
   * Used by maven2 to have 0 output of any kind come out
   * of testng.
   * @param useDefaultListeners Whether or not any default reports
   * should be added to tests.
   */
  public TestNG(boolean useDefaultListeners) {
    init(useDefaultListeners);
  }

  private void init(boolean useDefaultListeners) {
    m_instance = this;

    m_useDefaultListeners = useDefaultListeners;
  }

  public int getStatus() {
    return m_status;
  }

  protected void setStatus(int status) {
    m_status |= status;
  }

  /**
   * Sets the output directory where the reports will be created.
   * @param outputdir The directory.
   */
  public void setOutputDirectory(final String outputdir) {
    if ((null != outputdir) && !"".equals(outputdir)) {
      m_outputDir = outputdir;
    }
  }

  /**
   * If this method is passed true before run(), the default listeners
   * will not be used.
   * <ul>
   * <li>org.testng.reporters.TestHTMLReporter
   * <li>org.testng.reporters.JUnitXMLReporter
   * <li>org.testng.reporters.XMLReporter
   * </ul>
   *
   * @see org.testng.reporters.TestHTMLReporter
   * @see org.testng.reporters.JUnitXMLReporter
   * @see org.testng.reporters.XMLReporter
   */
  public void setUseDefaultListeners(boolean useDefaultListeners) {
    m_useDefaultListeners = useDefaultListeners;
  }

  /**
   * Sets the default annotation type for suites that have not explicitly set the
   * annotation property. The target is used only in JDK5+.
   * @param target the default annotation type. This is one of the two constants
   * (TestNG.JAVADOC_ANNOTATION_TYPE or TestNG.JDK_ANNOTATION_TYPE).
   * For backward compatibility reasons we accept "1.4", "1.5". Any other value will
   * default to TestNG.JDK_ANNOTATION_TYPE.
   *
   * @deprecated use the setDefaultAnnotationType replacement method.
   */
//  @Deprecated
//  public void setTarget(String target) {
//    // Target is used only in JDK 1.5 and may get null in JDK 1.4
//    LOGGER.warn("The usage of " + CommandLineArgs.TARGET_COMMAND + " option is deprecated." +
//            " Please use " + CommandLineArgs.ANNOTATIONS_COMMAND + " instead.");
//    if (null == target) {
//      return;
//    }
//    setAnnotations(target);
//  }

  /**
   * Sets the default annotation type for suites that have not explicitly set the
   * annotation property. The target is used only in JDK5+.
   * @param annotationType the default annotation type. This is one of the two constants
   * (TestNG.JAVADOC_ANNOTATION_TYPE or TestNG.JDK_ANNOTATION_TYPE).
   * For backward compatibility reasons we accept "1.4", "1.5". Any other value will
   * default to TestNG.JDK_ANNOTATION_TYPE.
   */
  public void setAnnotations(String annotationType) {
    if(null != annotationType && !"".equals(annotationType)) {
      setAnnotations(AnnotationTypeEnum.valueOf(annotationType));
    }
  }

  private void setAnnotations(AnnotationTypeEnum annotationType) {
    if(null != annotationType) {
      m_defaultAnnotations= annotationType;
    }
  }

  /**
   * Sets the ; separated path of source directories. This is used only with JavaDoc type
   * annotations. The directories do not have to be the root of a class hierarchy. For
   * example, "c:\java\src\org\testng" is a valid directory.
   *
   * If a resource named "testng-sourcedir-override.properties" is found in the classpath,
   * it will override this call. "testng-sourcedir-override.properties" must contain a
   * sourcedir property initialized with a semi-colon list of directories. For example:
   *
   * sourcedir=c:\java\src\org\testng;D:/dir2
   *
   * Considering the syntax of a properties file, you must escape the usage of : and = in
   * your paths.
   * Note that for the override to occur, this method must be called. i.e. it is not sufficient
   * to place "testng-sourcedir-override.properties" in the classpath.
   *
   * @param sourcePaths a semi-colon separated list of source directories.
   */
  public void setSourcePath(String sourcePaths) {
    LOGGER.debug("setSourcePath: \"" + sourcePaths + "\"");

    // This is an optimization to reduce the sourcePath scope
    // Is it OK to look only for the Thread context class loader?
    InputStream is = Thread.currentThread().getContextClassLoader()
      .getResourceAsStream("testng-sourcedir-override.properties");

    // Resource exists. Use override values and ignore given value
    if (is != null) {
      Properties props = new Properties();
      try {
        props.load(is);
      }
      catch (IOException e) {
        throw new RuntimeException("Error loading testng-sourcedir-override.properties", e);
      }
      sourcePaths = props.getProperty("sourcedir");
      LOGGER.debug("setSourcePath ignoring sourcepath parameter and "
          + "using testng-sourcedir-override.properties: \"" + sourcePaths + "\"");

    }
    if (null == sourcePaths || "".equals(sourcePaths.trim())) {
      return;
    }

    m_sourceDirs = Utils.split(sourcePaths, SRC_SEPARATOR);
  }

  /**
   * Sets a jar containing a testng.xml file.
   *
   * @param jarPath
   */
  public void setTestJar(String jarPath) {
    m_jarPath = jarPath;
  }

  public void initializeSuitesAndJarFile() {
    // The Eclipse plug-in (RemoteTestNG) might have invoked this method already
    // so don't initialize suites twice.
    if (m_suites.size() > 0) return;

    //
    // Parse the suites that were passed on the command line
    //
    for (String suiteXmlPath : m_stringSuites) {
      if(LOGGER.isDebugEnabled()) {
        LOGGER.debug("suiteXmlPath: \"" + suiteXmlPath + "\"");
      }
      try {
        Collection<XmlSuite> allSuites = new Parser(suiteXmlPath).parse();
        for (XmlSuite s : allSuites) {
          // If test names were specified, only run these test names
          if (m_testNames != null) {
            m_suites.add(extractTestNames(s, m_testNames));
          }
          else {
            m_suites.add(s);
          }
        }
      }
      catch(FileNotFoundException e) {
        e.printStackTrace(System.out);
      }
      catch(IOException e) {
        e.printStackTrace(System.out);
      }
      catch(ParserConfigurationException e) {
        e.printStackTrace(System.out);
      }
      catch(SAXException e) {
        e.printStackTrace(System.out);
      }
    }

    //
    // jar path
    //
    // If suites were passed on the command line, they take precedence over the suite file
    // inside that jar path
    if (m_jarPath != null && m_stringSuites.size() > 0) {
      StringBuilder suites = new StringBuilder();
      for (String s : m_stringSuites) {
        suites.append(s);
      }
      Utils.log("TestNG", 2, "Ignoring the XML file inside " + m_jarPath + " and using "
          + suites + " instead");
      return;
    }
    if ((null == m_jarPath) || "".equals(m_jarPath)) return;

    // We have a jar file and no XML file was specified: try to find an XML file inside the jar
    File jarFile = new File(m_jarPath);

    try {
      URL jarfileUrl = jarFile.getCanonicalFile().toURI().toURL();
      URLClassLoader jarLoader = new URLClassLoader(new URL[] { jarfileUrl });
      Thread.currentThread().setContextClassLoader(jarLoader);

      Utils.log("TestNG", 2, "Trying to open jar file:" + jarFile);

      JarFile jf = new JarFile(jarFile);
//      System.out.println("   result: " + jf);
      Enumeration<JarEntry> entries = jf.entries();
      List<String> classes = Lists.newArrayList();
      boolean foundTestngXml = false;
      while (entries.hasMoreElements()) {
        JarEntry je = entries.nextElement();
        if (je.getName().equals("testng.xml")) {
          Parser parser = new Parser(jf.getInputStream(je));
          m_suites.addAll(parser.parse());
          foundTestngXml = true;
          break;
        }
        else if (je.getName().endsWith(".class")) {
          int n = je.getName().length() - ".class".length();
          classes.add(je.getName().replace("/", ".").substring(0, n));
        }
      }
      if (! foundTestngXml) {
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setVerbose(0);
        xmlSuite.setName("Jar suite");
        XmlTest xmlTest = new XmlTest(xmlSuite);
        List<XmlClass> xmlClasses = Lists.newArrayList();
        for (String cls : classes) {
          XmlClass xmlClass = new XmlClass(cls);
          xmlClasses.add(xmlClass);
        }
        xmlTest.setXmlClasses(xmlClasses);
        m_suites.add(xmlSuite);
      }
    }
    catch(ParserConfigurationException ex) {
      ex.printStackTrace();
    }
    catch(SAXException ex) {
      ex.printStackTrace();
    }
    catch(MalformedURLException ex) {
      ex.printStackTrace();
    }
    catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * If the XmlSuite contains at least one test named as testNames, return
   * an XmlSuite that's made only of these tests, otherwise, return the
   * original suite.
   */
  private static XmlSuite extractTestNames(XmlSuite s, List<String> testNames) {
    List<XmlTest> tests = Lists.newArrayList();
    for (XmlTest xt : s.getTests()) {
      for (String tn : testNames) {
        if (xt.getName().equals(tn)) {
          tests.add(xt);
        }
      }
    }

    if (tests.size() == 0) {
      return s;
    }
    else {
      XmlSuite result = (XmlSuite) s.clone();
      result.getTests().clear();
      result.getTests().addAll(tests);
      return result;
    }
  }

  /**
   * Define the number of threads in the thread pool.
   */
  public void setThreadCount(int threadCount) {
    if(threadCount < 1) {
      exitWithError("Cannot use a threadCount parameter less than 1; 1 > " + threadCount);
    }

    m_threadCount = threadCount;
    m_useThreadCount = true;
  }

  /**
   * Define whether this run will be run in parallel mode.
   */
  public void setParallel(String parallel) {
    m_parallelMode = parallel;
    m_useParallelMode = true;
  }

  public void setCommandLineSuite(XmlSuite suite) {
    m_cmdlineSuites = Lists.newArrayList();
    m_cmdlineSuites.add(suite);
    m_suites.add(suite);
  }

  /**
   * Set the test classes to be run by this TestNG object.  This method
   * will create a dummy suite that will wrap these classes called
   * "Command Line Test".
   * <p/>
   * If used together with threadCount, parallel, groups, excludedGroups than this one must be set first.
   *
   * @param classes An array of classes that contain TestNG annotations.
   */
  public void setTestClasses(Class[] classes) {
    m_suites.clear();
    m_commandLineTestClasses = classes;
  }

  private String[] splitMethod(String m) {
    int index = m.lastIndexOf(".");
    if (index < 0) {
      throw new TestNGException("Bad format for command line method:" + m);
    }

    return new String[] { m.substring(0, index), m.substring(index + 1) };
  }

  private List<XmlSuite> createCommandLineSuitesForMethods(List<String> commandLineMethods) {
    //
    // Create the <classes> tag
    //
    Set<Class> classes = Sets.newHashSet();
    for (String m : commandLineMethods) {
      classes.add(ClassHelper.forName(splitMethod(m)[0]));
    }

    List<XmlSuite> result = createCommandLineSuitesForClasses(classes.toArray(new Class[0]));

    //
    // Add the method tags
    //
    List<XmlClass> xmlClasses = result.get(0).getTests().get(0).getXmlClasses();
    for (XmlClass xc : xmlClasses) {
      for (String m : commandLineMethods) {
        String[] split = splitMethod(m);
        String className = split[0];
        if (xc.getName().equals(className)) {
          XmlInclude includedMethod = new XmlInclude(split[1]);
          xc.getIncludedMethods().add(includedMethod);
        }
      }
    }

    return result;
  }

  private List<XmlSuite> createCommandLineSuitesForClasses(Class[] classes) {
    //
    // See if any of the classes has an xmlSuite or xmlTest attribute.
    // If it does, create the appropriate XmlSuite, otherwise, create
    // the default one
    //
    XmlClass[] xmlClasses = Utils.classesToXmlClasses(classes);
    Map<String, XmlSuite> suites = Maps.newHashMap();
    IAnnotationFinder finder = getAnnotationFinder();

    for (int i = 0; i < classes.length; i++) {
      Class c = classes[i];
      ITestAnnotation test = (ITestAnnotation) finder.findAnnotation(c, ITestAnnotation.class);
      String suiteName = getDefaultSuiteName();
      String testName = getDefaultTestName();
      if (test != null) {
        final String candidateSuiteName = test.getSuiteName();
        if (candidateSuiteName != null && !"".equals(candidateSuiteName)) {
          suiteName = candidateSuiteName;
        }
        final String candidateTestName = test.getTestName();
        if (candidateTestName != null && !"".equals(candidateTestName)) {
            testName = candidateTestName;
        }
      }
      XmlSuite xmlSuite = suites.get(suiteName);
      if (xmlSuite == null) {
        xmlSuite = new XmlSuite();
        xmlSuite.setName(suiteName);
        suites.put(suiteName, xmlSuite);
      }

      if (m_dataProviderThreadCount != null) {
        xmlSuite.setDataProviderThreadCount(m_dataProviderThreadCount);
      }
      XmlTest xmlTest = null;
      for (XmlTest xt  : xmlSuite.getTests()) {
        if (xt.getName().equals(testName)) {
          xmlTest = xt;
          break;
        }
      }

      if (xmlTest == null) {
        xmlTest = new XmlTest(xmlSuite);
        xmlTest.setName(testName);
      }

      xmlTest.getXmlClasses().add(xmlClasses[i]);
    }

    return new ArrayList<XmlSuite>(suites.values());
  }

  public void addMethodSelector(String className, int priority) {
    m_methodDescriptors.put(className, priority);
  }

  /**
   * Set the suites file names to be run by this TestNG object. This method tries to load and
   * parse the specified TestNG suite xml files. If a file is missing, it is ignored.
   *
   * @param suites A list of paths to one more XML files defining the tests.  For example:
   *
   * <pre>
   * TestNG tng = new TestNG();
   * List<String> suites = Lists.newArrayList();
   * suites.add("c:/tests/testng1.xml");
   * suites.add("c:/tests/testng2.xml");
   * tng.setTestSuites(suites);
   * tng.run();
   * </pre>
   */
  public void setTestSuites(List<String> suites) {
    m_stringSuites = suites;
  }

  /**
   * Specifies the XmlSuite objects to run.
   * @param suites
   * @see org.testng.xml.XmlSuite
   */
  public void setXmlSuites(List<XmlSuite> suites) {
    m_suites = suites;
  }

  /**
   * Define which groups will be excluded from this run.
   *
   * @param groups A list of group names separated by a comma.
   */
  public void setExcludedGroups(String groups) {
    m_excludedGroups = Utils.split(groups, ",");
  }


  /**
   * Define which groups will be included from this run.
   *
   * @param groups A list of group names separated by a comma.
   */
  public void setGroups(String groups) {
    m_includedGroups = Utils.split(groups, ",");
  }


  protected void setTestRunnerFactoryClass(Class testRunnerFactoryClass) {
    setTestRunnerFactory((ITestRunnerFactory) ClassHelper.newInstance(testRunnerFactoryClass));
  }


  protected void setTestRunnerFactory(ITestRunnerFactory itrf) {
    m_testRunnerFactory= itrf;
  }

  public void setObjectFactory(Class c) {
    m_objectFactory = (IObjectFactory)ClassHelper.newInstance(c);
  }

  public void setObjectFactory(IObjectFactory factory) {
    m_objectFactory = factory;
  }

  /**
   * Define which listeners to user for this run.
   *
   * @param classes A list of classes, which must be either ISuiteListener,
   * ITestListener or IReporter
   */
  public void setListenerClasses(List<Class> classes) {
    for (Class cls: classes) {
      addListener(ClassHelper.newInstance(cls));
    }
  }

  public void addListener(Object listener) {
    if (! (listener instanceof ITestNGListener))
    {
      exitWithError("Listener " + listener
          + " must be one of ITestListener, ISuiteListener, IReporter, "
          + " IAnnotationTransformer, IMethodInterceptor or IInvokedMethodListener");
    }
    else {
      if (listener instanceof ISuiteListener) {
        addListener((ISuiteListener) listener);
      }
      if (listener instanceof ITestListener) {
        addListener((ITestListener) listener);
      }
      if (listener instanceof IReporter) {
        addListener((IReporter) listener);
      }
      if (listener instanceof IAnnotationTransformer) {
        setAnnotationTransformer((IAnnotationTransformer) listener);
      }
      if (listener instanceof IMethodInterceptor) {
        m_methodInterceptor = (IMethodInterceptor) listener;
      }
      if (listener instanceof IInvokedMethodListener) {
        addInvokedMethodListener((IInvokedMethodListener) listener);
      }
      if (listener instanceof IHookable) {
        m_hookable = (IHookable) listener;
      }
      if (listener instanceof IConfigurable) {
        m_configurable = (IConfigurable) listener;
      }
    }
  }

  public void addListener(IInvokedMethodListener listener) {
    m_invokedMethodListeners.add(listener);
  }

  public void addListener(ISuiteListener listener) {
    if (null != listener) {
      m_suiteListeners.add(listener);
    }
  }

  public void addListener(ITestListener listener) {
    if (null != listener) {
      m_testListeners.add(listener);
    }
  }

  public void addListener(IReporter listener) {
    if (null != listener) {
      m_reporters.add(listener);
    }
  }

  public void addInvokedMethodListener(IInvokedMethodListener listener) {
    m_invokedMethodListeners.add(listener);
  }

  public Set<IReporter> getReporters() {
    return m_reporters;
  }

  public List<ITestListener> getTestListeners() {
    return m_testListeners;
  }

  public List<ISuiteListener> getSuiteListeners() {
    return m_suiteListeners;
  }

  /** The verbosity level. TODO why not a simple int? */
  private int m_verbose = 1;

  private IAnnotationTransformer m_annotationTransformer = new DefaultAnnotationTransformer();

  private Boolean m_skipFailedInvocationCounts = false;

  private IMethodInterceptor m_methodInterceptor = null;

  private Injector m_injector;

  /** The list of test names to run from the given suite */
  private List<String> m_testNames;

  private Integer m_suiteThreadPoolSize = CommandLineArgs.SUITE_THREAD_POOL_SIZE_DEFAULT;

  private boolean m_randomizeSuites = Boolean.valueOf(CommandLineArgs.RANDOMIZE_SUITES);

  /**
   * Sets the level of verbosity. This value will override the value specified
   * in the test suites.
   *
   * @param verbose the verbosity level (0 to 10 where 10 is most detailed)
   * Actually, this is a lie:  you can specify -1 and this will put TestNG
   * in debug mode (no longer slicing off stack traces and all).
   */
  public void setVerbose(int verbose) {
    m_verbose = verbose;
  }

  private void initializeCommandLineSuites() {
    if (m_commandLineTestClasses != null || m_commandLineMethods != null) {
      initializeInjector();
      if (null != m_commandLineMethods) {
        m_cmdlineSuites = createCommandLineSuitesForMethods(m_commandLineMethods);
      }
      else {
        m_cmdlineSuites = createCommandLineSuitesForClasses(m_commandLineTestClasses);
      }

      for (XmlSuite s : m_cmdlineSuites) {
        m_suites.add(s);
      }
    }
  }

  private void initializeCommandLineSuitesParams() {
    if(null == m_cmdlineSuites) {
      return;
    }

    for (XmlSuite s : m_cmdlineSuites) {
      if(m_useThreadCount) {
        s.setThreadCount(m_threadCount);
      }
      if(m_useParallelMode) {
        s.setParallel(m_parallelMode);
      }
      if(m_configFailurePolicy != null) {
        s.setConfigFailurePolicy(m_configFailurePolicy.toString());
      }
    }

  }

  private void initializeCommandLineSuitesGroups() {
    if (null != m_cmdlineSuites) {
      for (XmlSuite s : m_cmdlineSuites) {
        if(null != m_includedGroups && m_includedGroups.length > 0) {
          s.getTests().get(0).setIncludedGroups(Arrays.asList(m_includedGroups));
        }
        if(null != m_excludedGroups && m_excludedGroups.length > 0) {
          s.getTests().get(0).setExcludedGroups(Arrays.asList(m_excludedGroups));
        }
      }
    }
  }
  private void addReporter(Class<? extends IReporter> r) {
    if (! m_reporters.contains(r)) {
      m_reporters.add(ClassHelper.newInstance(r));
    }
  }

  private void initializeDefaultListeners() {
    m_testListeners.add(new ExitCodeListener(this));

    if (m_useDefaultListeners) {
      addReporter(SuiteHTMLReporter.class);
      addReporter(FailedReporter.class);
      addReporter(XMLReporter.class);
      addReporter(EmailableReporter.class);
      addReporter(JUnitReportReporter.class);
    }
  }

  private void initializeInjector() {
    TestNGGuiceModule module = new TestNGGuiceModule(getAnnotationTransformer(), m_objectFactory);
    module.setHookable(m_hookable);
    module.setConfigurable(m_configurable);
    m_injector = Guice.createInjector(module);
  }

  /**
   * Run TestNG.
   */
  public void run() {
    initializeSuitesAndJarFile();
    initializeDefaultListeners();
    initializeCommandLineSuites();
    initializeCommandLineSuitesParams();
    initializeCommandLineSuitesGroups();

    List<ISuite> suiteRunners = null;

    //
    // Slave mode
    //
    if (m_slavefileName != null) {
       SuiteSlave slave = new SuiteSlave( m_slavefileName, this );
       slave.waitForSuites();
    }

    //
    // Regular mode
    //
    else if (m_masterfileName == null) {
      suiteRunners = runSuitesLocally();
    }

    //
    // Master mode
    //
    else {
       SuiteDispatcher dispatcher = new SuiteDispatcher(m_masterfileName);
       suiteRunners = dispatcher.dispatch(getConfiguration(),
           m_suites, getOutputDirectory(),
           getTestListeners());
    }

    initializeInjector();

    if(null != suiteRunners) {
      generateReports(suiteRunners);
    }

    if(!m_hasTests) {
      setStatus(HAS_NO_TEST);
      if (TestRunner.getVerbose() > 1) {
        System.err.println("[TestNG] No tests found. Nothing was run");
        usage();
      }
    }
  }

  private static void usage() {
    m_jCommander.usage();
  }

  private void generateReports(List<ISuite> suiteRunners) {
    for (IReporter reporter : m_reporters) {
      try {
        reporter.generateReport(m_suites, suiteRunners, m_outputDir);
      }
      catch(Exception ex) {
        System.err.println("[TestNG] Reporter " + reporter + " failed");
        ex.printStackTrace(System.err);
      }
    }
  }

  /**
   * This needs to be public for maven2, for now..At least
   * until an alternative mechanism is found.
   */
  public List<ISuite> runSuitesLocally() {
    Map<XmlSuite, ISuite> suiteRunnerMap = Maps.newHashMap();
    if (m_suites.size() > 0) {
      /*
       * first initialize the suite runners to ensure there are no configuration issues.
       * Create a map with XmlSuite as key and corresponding SuiteRunner as value
       */
      for (XmlSuite xmlSuite : m_suites) {
        createSuiteRunners(suiteRunnerMap, xmlSuite);
      }

      /*
       * Run suites
       */
      if (m_suiteThreadPoolSize == 1 && !m_randomizeSuites) {
        /*
         * Only of we want suites to run in order specified in XML and the suite thread pool
         * size is 1, we run this block
         */
        for (XmlSuite xmlSuite : m_suites) {
          runSuitesSequentially(xmlSuite, suiteRunnerMap, m_verbose, getDefaultSuiteName());
        }
      } else {
        /*
         * Generate a dynamic graph that stores the suite hierarchy. This is then
         * used to run related suites in specific order. Parent suites are run only
         * once all the child suites have completed execution
         */
        DynamicGraph<ISuite> suiteGraph = new DynamicGraph<ISuite>();
        for (XmlSuite xmlSuite : m_suites) {
          populateSuiteGraph(suiteGraph, suiteRunnerMap, xmlSuite);
        }

        IThreadWorkerFactory<ISuite> factory = new SuiteWorkerFactory(suiteRunnerMap,
          m_verbose, getDefaultSuiteName());
        GraphThreadPoolExecutor<ISuite> pooledExecutor =
          new GraphThreadPoolExecutor<ISuite>(suiteGraph, factory, m_suiteThreadPoolSize,
          m_suiteThreadPoolSize, Integer.MAX_VALUE, TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>());

        Utils.log("TestNG", 2, "Starting executor for all suites");
        //run all suites in parallel
        pooledExecutor.run();
        try {
          //TODO: Setting timeout to Long.MAX_VALUE. Is it correct/ok?
          pooledExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
          pooledExecutor.shutdownNow();
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          LOGGER.error("Error waiting for concurrent executors to finish " + e.getMessage());
        }
      }
    }
    else {
      setStatus(HAS_NO_TEST);
      System.err.println("[ERROR]: No test suite found. Nothing to run");
      usage();
    }

    //
    // Generate the suites report
    //
    return Lists.newArrayList(suiteRunnerMap.values());
  }

  /**
   * Recursively runs suites. Runs the children suites before running the parent
   * suite. This is done so that the results for parent suite can reflect the
   * combined results of the children suites.
   *
   * @param xmlSuite XML Suite to be executed
   * @param suiteRunnerMap Maps {@code XmlSuite}s to respective {@code ISuite}
   * @param verbose verbose level
   * @param defaultSuiteName default suite name
   */
  private void runSuitesSequentially(XmlSuite xmlSuite,
      Map<XmlSuite, ISuite> suiteRunnerMap, int verbose, String defaultSuiteName) {
    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      runSuitesSequentially(childSuite, suiteRunnerMap, verbose, defaultSuiteName);
    }
    SuiteRunnerWorker srw = new SuiteRunnerWorker(suiteRunnerMap.get(xmlSuite), suiteRunnerMap,
      verbose, defaultSuiteName);
    srw.run();
  }

  /**
   * Populates the dynamic graph with the reverse hierarchy of suites. Edges are
   * added pointing from child suite runners to parent suite runners, hence making
   * parent suite runners dependent on all the child suite runners
   *
   * @param suiteGraph dynamic graph representing the reverse hierarchy of SuiteRunners
   * @param suiteRunnerMap Map with XMLSuite as key and its respective SuiteRunner as value
   * @param xmlSuite XML Suite
   */
  private void populateSuiteGraph(DynamicGraph<ISuite> suiteGraph /* OUT */, 
      Map<XmlSuite, ISuite> suiteRunnerMap, XmlSuite xmlSuite) {
    ISuite parentSuiteRunner = suiteRunnerMap.get(xmlSuite);
    if (xmlSuite.getChildSuites().isEmpty()) {
      suiteGraph.addNode(parentSuiteRunner);
    }
    else {
      for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
        suiteGraph.addEdge(parentSuiteRunner, suiteRunnerMap.get(childSuite));
        populateSuiteGraph(suiteGraph, suiteRunnerMap, childSuite);
      }
    }
  }

  /**
   * Creates the {@code SuiteRunner}s and populates the suite runner map with 
   * this information
   * @param suiteRunnerMap Map with XMLSuite as key and it's respective 
   *   SuiteRunner as value. This is updated as part of this method call
   * @param xmlSuite Xml Suite (and it's children) for which {@code SuiteRunner}s are created
   */
  private void createSuiteRunners(Map<XmlSuite, ISuite> suiteRunnerMap /* OUT */, XmlSuite xmlSuite) {
    xmlSuite.setDefaultAnnotations(m_defaultAnnotations.toString());

    if (null != m_isJUnit) {
      xmlSuite.setJUnit(m_isJUnit);
    }

    //
    // Install the listeners
    //
    for (String listenerName : xmlSuite.getListeners()) {
      Class<?> listenerClass = ClassHelper.forName(listenerName);

      // If specified listener does not exist, a TestNGException will be thrown
      if(listenerClass == null) {
        throw new TestNGException("Listener " + listenerName
            + " was not found in project's classpath");
      }

      Object listener = ClassHelper.newInstance(listenerClass);
      addListener(listener);
    }

    // If the skip flag was invoked on the command line, it
    // takes precedence
    if (null != m_skipFailedInvocationCounts) {
      xmlSuite.setSkipFailedInvocationCounts(m_skipFailedInvocationCounts);
    }

    if (xmlSuite.getVerbose() == null) {
      xmlSuite.setVerbose(m_verbose);
    }

    if (null != m_configFailurePolicy) {
      xmlSuite.setConfigFailurePolicy(m_configFailurePolicy);
    }

    for (XmlTest t : xmlSuite.getTests()) {
      for (Map.Entry<String, Integer> ms : m_methodDescriptors.entrySet()) {
        XmlMethodSelector xms = new XmlMethodSelector();
        xms.setName(ms.getKey());
        xms.setPriority(ms.getValue());
        t.getMethodSelectors().add(xms);
      }
    }

    suiteRunnerMap.put(xmlSuite, createSuiteRunner(xmlSuite));

    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      createSuiteRunners(suiteRunnerMap, childSuite);
    }
  }

  /**
   * Creates a suite runner and configures its initial state
   * @param xmlSuite
   * @return returns the newly created suite runner
   */
  protected SuiteRunner createSuiteRunner(XmlSuite xmlSuite) {
    initializeInjector();
    SuiteRunner result = new SuiteRunner(getConfiguration(), xmlSuite,
        m_outputDir,
        m_testRunnerFactory,
        m_useDefaultListeners,
        m_methodInterceptor,
        m_invokedMethodListeners,
        m_testListeners);

    for (ISuiteListener isl : m_suiteListeners) {
      result.addListener(isl);
    }

    for (IReporter r : result.getReporters()) {
      addListener(r);
    }

    return result;
  }

  private IAnnotationFinder getAnnotationFinder() {
    return m_injector.getInstance(IAnnotationFinder.class);
  }

  protected IConfiguration getConfiguration() {
    return m_injector.getInstance(IConfiguration.class);
  }

  /**
   * The TestNG entry point for command line execution.
   *
   * @param argv the TestNG command line parameters.
   */
  public static void main(String[] argv) {
    TestNG testng = privateMain(argv, null);
    System.exit(testng.getStatus());
  }

  /**
   * <B>Note</B>: this method is not part of the public API and is meant for internal usage only.
   * TODO  JavaDoc.
   *
   * @param argv
   * @param listener
   * @return
   */
  /**
   * @param argv
   * @param listener
   */
  public static TestNG privateMain(String[] argv, ITestListener listener) {
    TestNG result = new TestNG();

    if (null != listener) {
      result.addListener(listener);
    }

    //
    // Parse the arguments
    //
    try {
      CommandLineArgs cla = new CommandLineArgs();
      m_jCommander = new JCommander(cla, argv);
      validateCommandLineParameters(cla);
      result.configure(cla);
    }
    catch(ParameterException ex) {
      exitWithError(ex.getMessage());
    }

    //
    // Run
    //
    try {
      result.run();
    }
    catch(TestNGException ex) {
      if (TestRunner.getVerbose() > 1) {
        ex.printStackTrace(System.out);
      }
      else {
        System.err.println("[ERROR]: " + ex.getMessage());
      }
      result.setStatus(HAS_FAILURE);
    }

    return result;
  }

  /**
   * Configure the TestNG instance based on the command line parameters.
   */
  protected void configure(CommandLineArgs cla) {
    if (cla.verbose != null) setVerbose(cla.verbose);
    setOutputDirectory(cla.outputDirectory);

    String testClasses = cla.testClass;
    if (null != testClasses) {
      String[] strClasses = testClasses.split(",");
      List<Class> classes = Lists.newArrayList();
      for (String c : strClasses) {
        classes.add(ClassHelper.fileToClass(c));
      }

      setTestClasses(classes.toArray(new Class[classes.size()]));
    }

    setOutputDirectory(cla.outputDirectory);

    if (cla.testNames != null) {
      setTestNames(Arrays.asList(cla.testNames.split(",")));
    }

//    List<String> testNgXml = (List<String>) cmdLineArgs.get(CommandLineArgs.SUITE_DEF);
//    if (null != testNgXml) {
//      setTestSuites(testNgXml);
//    }

    // Note: can't use a Boolean field here because we are allowing a boolean
    // parameter with an arity of 1 ("-usedefaultlisteners false")
    if (cla.useDefaultListeners != null) {
      setUseDefaultListeners("true".equalsIgnoreCase(cla.useDefaultListeners));
    }

    setGroups(cla.groups);
    setExcludedGroups(cla.excludedGroups);
    setTestJar(cla.testJar);
    setJUnit(cla.junit);
    setMaster(cla.master);
    setSlave(cla.slave);
    setSkipFailedInvocationCounts(cla.skipFailedInvocationCounts);
    if (cla.parallelMode != null) setParallel(cla.parallelMode);
    if (cla.configFailurePolicy != null) setConfigFailurePolicy(cla.configFailurePolicy);
    if (cla.threadCount != null) setThreadCount(cla.threadCount);
    if (cla.dataProviderThreadCount != null) {
      setDataProviderThreadCount(cla.dataProviderThreadCount);
    }
    if (cla.suiteName != null) setDefaultSuiteName(cla.suiteName);
    if (cla.testName != null) setDefaultTestName(cla.testName);
    if (cla.listener != null) {
      String sep = ";";
      if (cla.listener.indexOf(",") >= 0) {
        sep = ",";
      }
      String[] strs = Utils.split(cla.listener, sep);
      List<Class> classes = Lists.newArrayList();

      for (String cls : strs) {
        classes.add(ClassHelper.fileToClass(cls));
      }

      setListenerClasses(classes);
    }

    if (null != cla.methodSelectors) {
      String[] strs = Utils.split(cla.methodSelectors, ",");
      for (String cls : strs) {
        String[] sel = Utils.split(cls, ":");
        try {
          if (sel.length == 2) {
            addMethodSelector(sel[0], Integer.valueOf(sel[1]));
          } else {
            LOGGER.error("ERROR: method selector value was not in the format" +
                  " org.example.Selector:4");
          }
        }
        catch (NumberFormatException nfe) {
          LOGGER.error("ERROR: method selector value was not in the format org.example.Selector:4");
        }
      }
    }

    if (cla.objectFactory != null) setObjectFactory(ClassHelper.fileToClass(cla.objectFactory));
    if (cla.testRunnerFactory != null) setTestRunnerFactoryClass(
        ClassHelper.fileToClass(cla.testRunnerFactory));

    if (cla.reportersList != null) {
      ReporterConfig reporterConfig = ReporterConfig.deserialize(cla.reportersList);
      addReporter(reporterConfig);
    }

    if (cla.commandLineMethods.size() > 0) {
      m_commandLineMethods = cla.commandLineMethods;
    }

    if (cla.suiteFiles != null) setTestSuites(cla.suiteFiles);

    setSuiteThreadPoolSize(cla.suiteThreadPoolSize);
    setRandomizeSuites(Boolean.valueOf(cla.randomizeSuites));
  }

  public void setSuiteThreadPoolSize(Integer suiteThreadPoolSize) {
    m_suiteThreadPoolSize = suiteThreadPoolSize;
  }

  public Integer getSuiteThreadPoolSize() {
    return m_suiteThreadPoolSize;
  }

   public void setRandomizeSuites(boolean randomizeSuites) {
     m_randomizeSuites = randomizeSuites;
   }

  /**
   * This method is invoked by Maven's Surefire to configure the runner,
   * do not remove unless you know for sure that Surefire has been updated
   * to use the new configure(CommandLineArgs) method.
   */
  @SuppressWarnings({"unchecked"})
  public void configure(Map cmdLineArgs) {
    CommandLineArgs result = new CommandLineArgs();

    Integer verbose = (Integer) cmdLineArgs.get(CommandLineArgs.LOG);
    if (null != verbose) {
      result.verbose = verbose;
    }
    result.outputDirectory = (String) cmdLineArgs.get(CommandLineArgs.OUTPUT_DIRECTORY);

    String testClasses = (String) cmdLineArgs.get(CommandLineArgs.TEST_CLASS);
    if (null != testClasses) {
      result.testClass = testClasses;
    }

    String testNames = (String) cmdLineArgs.get(CommandLineArgs.TEST_NAMES);
    if (testNames != null) {
      result.testNames = testNames;
    }

//    List<String> testNgXml = (List<String>) cmdLineArgs.get(CommandLineArgs.SUITE_DEF);
//    if (null != testNgXml) {
//      setTestSuites(testNgXml);
//    }

    String useDefaultListeners = (String) cmdLineArgs.get(CommandLineArgs.USE_DEFAULT_LISTENERS);
    if (null != useDefaultListeners) {
      result.useDefaultListeners = useDefaultListeners;
    }

    result.groups = (String) cmdLineArgs.get(CommandLineArgs.GROUPS);
    result.excludedGroups = (String) cmdLineArgs.get(CommandLineArgs.EXCLUDED_GROUPS);
    result.testJar = (String) cmdLineArgs.get(CommandLineArgs.TEST_JAR);
    result.junit = (Boolean) cmdLineArgs.get(CommandLineArgs.JUNIT);
    result.master = (String) cmdLineArgs.get(CommandLineArgs.MASTER);
    result.slave = (String) cmdLineArgs.get(CommandLineArgs.SLAVE);
    result.skipFailedInvocationCounts = (Boolean) cmdLineArgs.get(
        CommandLineArgs.SKIP_FAILED_INVOCATION_COUNTS);
    String parallelMode = (String) cmdLineArgs.get(CommandLineArgs.PARALLEL);
    if (parallelMode != null) {
      result.parallelMode = parallelMode;
    }

    // TODO: verify that Surefire is passing an Integer here
    Integer threadCount = (Integer) cmdLineArgs.get(CommandLineArgs.THREAD_COUNT);
    if (threadCount != null) {
      result.threadCount = threadCount;
    }
    // TODO: verify that Surefire is passing an Integer here
    Integer dptc = (Integer) cmdLineArgs.get(CommandLineArgs.DATA_PROVIDER_THREAD_COUNT);
    if (dptc != null) {
      result.dataProviderThreadCount = dptc;
    }
    String defaultSuiteName = (String) cmdLineArgs.get(CommandLineArgs.SUITE_NAME);
    if (defaultSuiteName != null) {
      result.suiteName = defaultSuiteName;
    }

    String defaultTestName = (String) cmdLineArgs.get(CommandLineArgs.TEST_NAME);
    if (defaultTestName != null) {
      result.testName = defaultTestName;
    }

    String strClass = (String) cmdLineArgs.get(CommandLineArgs.LISTENER);
    if (null != strClass) {
      result.listener = strClass;
    }

    String ms = (String) cmdLineArgs.get(CommandLineArgs.METHOD_SELECTORS);
    if (null != ms) {
      result.methodSelectors = ms;
    }

    String objectFactory = (String) cmdLineArgs.get(CommandLineArgs.OBJECT_FACTORY);
    if(null != objectFactory) {
      result.objectFactory = objectFactory;
    }

    String runnerFactory = (String) cmdLineArgs.get(CommandLineArgs.TEST_RUNNER_FACTORY);
    if (null != runnerFactory) {
      result.testRunnerFactory = runnerFactory;
    }

    String reporterConfigs = (String) cmdLineArgs.get(CommandLineArgs.REPORTERS_LIST);
    if (reporterConfigs != null) {
      result.reportersList = reporterConfigs;
    }

    String failurePolicy = (String)cmdLineArgs.get(CommandLineArgs.CONFIG_FAILURE_POLICY);
    if (failurePolicy != null) {
      result.configFailurePolicy = failurePolicy;
    }
  }

  private void setTestNames(List<String> testNames) {
    m_testNames = testNames;
  }

  public void setSkipFailedInvocationCounts(Boolean skip) {
    m_skipFailedInvocationCounts = skip;
  }

  private void addReporter(ReporterConfig reporterConfig) {
    Object instance = reporterConfig.newReporterInstance();
    if (instance != null) {
      addListener(instance);
    } else {
      LOGGER.warn("Could not find reporte class : " + reporterConfig.getClassName());
    }
  }

  /**
   * Specify if this run should be in Master-Slave mode as Master
   *
   * @param fileName remote.properties path
   */
  public void setMaster(String fileName) {
     m_masterfileName = fileName;
  }

  /**
   * Specify if this run should be in Master-Slave mode as slave
   *
   * @param fileName remote.properties path
   */
  public void setSlave(String fileName) {
     m_slavefileName = fileName;
  }

  /**
   * Specify if this run should be made in JUnit mode
   *
   * @param isJUnit
   */
  public void setJUnit(Boolean isJUnit) {
    m_isJUnit = isJUnit;
  }

  /**
   * @deprecated The TestNG version is now established at load time. This
   * method is not required anymore and is now a no-op.
   */
  @Deprecated
  public static void setTestNGVersion() {
    LOGGER.info("setTestNGVersion has been deprecated.");
  }

  /**
   * Returns true if this is the JDK 1.4 JAR version of TestNG, false otherwise.
   *
   * @return true if this is the JDK 1.4 JAR version of TestNG, false otherwise.
   */
  public static boolean isJdk14() {
    return VersionInfo.IS_JDK14;
  }

  /**
   * Checks TestNG preconditions. For example, this method makes sure that if this is the
   * JDK 1.4 version of TestNG, a source directory has been specified. This method calls
   * System.exit(-1) or throws an exception if the preconditions are not satisfied.
   *
   * @param params the parsed command line parameters.
   */
//  @SuppressWarnings({"unchecked"})
//  protected static Map checkConditions(Map params) {
//    // TODO CQ document why sometimes we throw exceptions and sometimes we exit.
//    String testClasses = (String) params.get(CommandLineArgs.TESTCLASS_COMMAND);
//    List<String> testNgXml = (List<String>) params.get(CommandLineArgs.SUITE_DEF);
//    Object testJar = params.get(CommandLineArgs.TESTJAR_COMMAND);
//    Object slave = params.get(CommandLineArgs.SLAVE);
//
//    if (testClasses == null && testNgXml == null && slave == null && testJar == null) {
//      System.err.println("You need to specify at least one testng.xml or one class");
//      usage();
//      System.exit(-1);
//    }
//
//    if (VersionInfo.IS_JDK14) {
//      String srcPath = (String) params.get(CommandLineArgs.SRC_COMMAND);
//
//      if ((null == srcPath) || "".equals(srcPath)) {
//        throw new TestNGException("No sourcedir was specified");
//      }
//    }
//
//    String groups = (String) params.get(CommandLineArgs.GROUPS_COMMAND);
//    String excludedGroups = (String) params.get(CommandLineArgs.EXCLUDED_GROUPS_COMMAND);
//
//    if (testJar == null &&
//        (null != groups || null != excludedGroups) && testClasses == null && testNgXml == null) {
//      throw new TestNGException("Groups option should be used with testclass option");
//    }
//
//    // -slave & -master can't be set together
//    if (params.containsKey(CommandLineArgs.SLAVE) &&
//   		 params.containsKey(CommandLineArgs.MASTER)) {
//   	 throw new TestNGException(CommandLineArgs.SLAVE + " can't be combined with " +
//   	                           CommandLineArgs.MASTER);
//    }
//
//    return params;
//  }

  /**
   * Double check that the command line parameters are valid.
   */
  protected static void validateCommandLineParameters(CommandLineArgs args) {
    String testClasses = args.testClass;
    List<String> testNgXml = args.suiteFiles;
    String testJar = args.testJar;
    String slave = args.slave;
    List<String> methods = args.commandLineMethods;

    if (testClasses == null && slave == null && testJar == null
        && (testNgXml == null || testNgXml.isEmpty())
        && (methods == null || methods.isEmpty())) {
      throw new ParameterException("You need to specify at least one testng.xml, one class"
          + " or one method");
    }

    String groups = args.groups;
    String excludedGroups = args.excludedGroups;

    if (testJar == null &&
        (null != groups || null != excludedGroups) && testClasses == null
        && (testNgXml == null || testNgXml.isEmpty())) {
      throw new ParameterException("Groups option should be used with testclass option");
    }

    if (args.slave != null && args.master != null) {
     throw new ParameterException(CommandLineArgs.SLAVE + " can't be combined with "
         + CommandLineArgs.MASTER);
    }
  }

  /**
   * @return true if at least one test failed.
   */
  public boolean hasFailure() {
    return (getStatus() & HAS_FAILURE) == HAS_FAILURE;
  }

  /**
   * @return true if at least one test failed within success percentage.
   */
  public boolean hasFailureWithinSuccessPercentage() {
    return (getStatus() & HAS_FSP) == HAS_FSP;
  }

  /**
   * @return true if at least one test was skipped.
   */
  public boolean hasSkip() {
    return (getStatus() & HAS_SKIPPED) == HAS_SKIPPED;
  }

  static void exitWithError(String msg) {
    System.err.println(msg);
    usage();
    System.exit(1);
  }

  public String getOutputDirectory() {
    return m_outputDir;
  }

  public IAnnotationTransformer getAnnotationTransformer() {
    return m_annotationTransformer;
  }

  public void setAnnotationTransformer(IAnnotationTransformer t) {
    m_annotationTransformer = t;
  }

  /**
   * @return the defaultSuiteName
   */
  public String getDefaultSuiteName() {
    return m_defaultSuiteName;
  }

  /**
   * @param defaultSuiteName the defaultSuiteName to set
   */
  public void setDefaultSuiteName(String defaultSuiteName) {
    m_defaultSuiteName = defaultSuiteName;
  }

  /**
   * @return the defaultTestName
   */
  public String getDefaultTestName() {
    return m_defaultTestName;
  }

  /**
   * @param defaultTestName the defaultTestName to set
   */
  public void setDefaultTestName(String defaultTestName) {
    m_defaultTestName = defaultTestName;
  }

  /**
   * Sets the policy for whether or not to ever invoke a configuration method again after
   * it has failed once. Possible values are defined in {@link XmlSuite}.  The default
   * value is {@link XmlSuite#SKIP}.
   * @param failurePolicy the configuration failure policy
   */
  public void setConfigFailurePolicy(String failurePolicy) {
    m_configFailurePolicy = failurePolicy;
  }

  /**
   * Returns the configuration failure policy.
   * @return config failure policy
   */
  public String getConfigFailurePolicy() {
    return m_configFailurePolicy;
  }

  // DEPRECATED: to be removed after a major version change
  /**
   * @deprecated since 5.1
   */
  @Deprecated
  public static TestNG getDefault() {
    return m_instance;
  }

  /**
   * @deprecated since 5.1
   */
  @Deprecated
  public void setHasFailure(boolean hasFailure) {
    m_status |= HAS_FAILURE;
  }

  /**
   * @deprecated since 5.1
   */
  @Deprecated
  public void setHasFailureWithinSuccessPercentage(boolean hasFailureWithinSuccessPercentage) {
    m_status |= HAS_FSP;
  }

  /**
   * @deprecated since 5.1
   */
  @Deprecated
  public void setHasSkip(boolean hasSkip) {
    m_status |= HAS_SKIPPED;
  }

  public static class ExitCodeListener implements IResultListener {
    protected TestNG m_mainRunner;

    public ExitCodeListener() {
      m_mainRunner = TestNG.m_instance;
    }

    public ExitCodeListener(TestNG runner) {
      m_mainRunner = runner;
    }

    @Override
    public void onTestFailure(ITestResult result) {
      setHasRunTests();
      m_mainRunner.setStatus(HAS_FAILURE);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
      setHasRunTests();
      m_mainRunner.setStatus(HAS_SKIPPED);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
      setHasRunTests();
      m_mainRunner.setStatus(HAS_FSP);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
      setHasRunTests();
    }

    @Override
    public void onStart(ITestContext context) {
      setHasRunTests();
    }

    @Override
    public void onFinish(ITestContext context) {
    }

    @Override
    public void onTestStart(ITestResult result) {
      setHasRunTests();
    }

    private void setHasRunTests() {
      m_mainRunner.m_hasTests= true;
    }

    /**
     * @see org.testng.internal.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
     */
    @Override
    public void onConfigurationFailure(ITestResult itr) {
      m_mainRunner.setStatus(HAS_FAILURE);
    }

    /**
     * @see org.testng.internal.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
     */
    @Override
    public void onConfigurationSkip(ITestResult itr) {
      m_mainRunner.setStatus(HAS_SKIPPED);
    }

    /**
     * @see org.testng.internal.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
     */
    @Override
    public void onConfigurationSuccess(ITestResult itr) {
    }
  }

  public void setMethodInterceptor(IMethodInterceptor methodInterceptor) {
    m_methodInterceptor = methodInterceptor;
  }

  public void setDataProviderThreadCount(int count) {
    m_dataProviderThreadCount = count;
  }

  /** Add a class loader to the searchable loaders. */
  public void addClassLoader(final ClassLoader loader) {
    if (loader != null) {
      ClassHelper.addClassLoader(loader);
    }
  }
}
