package org.testng;


import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.AnnotationTypeEnum;
import org.testng.internal.ClassHelper;
import org.testng.internal.IResultListener;
import org.testng.internal.PoolService;
import org.testng.internal.Utils;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.version.VersionInfo;
import org.testng.log4testng.Logger;
import org.testng.remote.SuiteDispatcher;
import org.testng.remote.SuiteSlave;
import org.testng.reporters.EmailableReporter;
import org.testng.reporters.FailedReporter;
import org.testng.reporters.SuiteHTMLReporter;
import org.testng.reporters.XMLReporter;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
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
  
  private static TestNG m_instance;

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
  
  protected IAnnotationFinder m_jdkAnnotationFinder;
  
  protected String[] m_includedGroups;
  protected String[] m_excludedGroups;
  
  private Boolean m_isJUnit = Boolean.FALSE;
  protected boolean m_useDefaultListeners = true;

  protected ITestRunnerFactory m_testRunnerFactory;

  // These listeners can be overridden from the command line
  protected List<ITestListener> m_testListeners = Lists.newArrayList();
  protected List<ISuiteListener> m_suiteListeners = Lists.newArrayList();
  private List<IReporter> m_reporters = Lists.newArrayList();

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
  private Class[] m_commandLineTestClasses;
  
  private String m_defaultSuiteName=DEFAULT_COMMAND_LINE_SUITE_NAME;
  private String m_defaultTestName=DEFAULT_COMMAND_LINE_TEST_NAME;
  
  private Map<String, Integer> m_methodDescriptors = Maps.newHashMap();

  private IObjectFactory m_objectFactory;
  
  protected List<IInvokedMethodListener> m_invokedMethodListeners = Lists.newArrayList();

  private Integer m_dataProviderThreadCount = null;

  private String m_jarPath;

  private List<String> m_stringSuites = Lists.newArrayList();
  
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
  @Deprecated
  public void setTarget(String target) {
    // Target is used only in JDK 1.5 and may get null in JDK 1.4
    LOGGER.warn("The usage of " + TestNGCommandLineArgs.TARGET_COMMAND_OPT + " option is deprecated." +
            " Please use " + TestNGCommandLineArgs.ANNOTATIONS_COMMAND_OPT + " instead.");
    if (null == target) {
      return;
    }
    setAnnotations(target);
  }

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
          m_suites.add(s);
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

      JarFile jf = new JarFile(jarFile);
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
    m_commandLineTestClasses = classes;
  }
  
  /**
   * TODO CQ m_defaultAnnotations is only a default, how can we commit to a specific 
   * annotation finder?
   */
  private IAnnotationFinder getAnnotationFinder() {
    return m_jdkAnnotationFinder;
  }
  
  private List<XmlSuite> createCommandLineSuites(Class[] classes) {
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

      List<XmlMethodSelector> selectors = xmlTest.getMethodSelectors();
      for (String name : m_methodDescriptors.keySet()) {
        XmlMethodSelector xms = new XmlMethodSelector();
        xms.setName(name);
        xms.setPriority(m_methodDescriptors.get(name));
        selectors.add(xms);
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
    }
  }

  public void addListener(IInvokedMethodListener listener) {
    m_invokedMethodListeners.add((IInvokedMethodListener) listener);
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
  
  public List<IReporter> getReporters() {
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
    if(null != m_commandLineTestClasses) {
      initializeAnnotationFinders();
      m_cmdlineSuites = createCommandLineSuites(m_commandLineTestClasses);
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
  
  private void initializeListeners() {
    m_testListeners.add(new ExitCodeListener(this));
    
    if(m_useDefaultListeners) {
      m_reporters.add(new SuiteHTMLReporter());
      m_reporters.add(new FailedReporter());
      m_reporters.add(new XMLReporter());
      m_reporters.add(new EmailableReporter());
    }
  }
  
  private void initializeAnnotationFinders() {
    if (!VersionInfo.IS_JDK14) {
      m_jdkAnnotationFinder= ClassHelper.createJdkAnnotationFinder(getAnnotationTransformer());
    }
  }
  
  /**
   * Run TestNG.
   */
  public void run() {
    initializeSuitesAndJarFile();
    initializeListeners();
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
   	 suiteRunners = dispatcher.dispatch(m_suites, getOutputDirectory(),
   	     m_jdkAnnotationFinder, getTestListeners());
    }
    
    initializeAnnotationFinders();

    if(null != suiteRunners) {
      generateReports(suiteRunners);
    }
    
    if(!m_hasTests) {
      setStatus(HAS_NO_TEST);
      if (TestRunner.getVerbose() > 1) {
        System.err.println("[TestNG] No tests found. Nothing was run");
      }
    }
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
   * @return
   */
  public List<ISuite> runSuitesLocally() {
    List<ISuite> result = Lists.newArrayList();
    
    if (m_verbose > 0) {
      StringBuffer allFiles = new StringBuffer();
      for (XmlSuite s : m_suites) {
        allFiles.append("  ").append(s.getFileName() != null ? s.getFileName() : getDefaultSuiteName()).append('\n');
      }
      Utils.log("Parser", 0, "Running:\n" + allFiles.toString());
    }

    if (m_suites.size() > 0) {
      for (XmlSuite xmlSuite : m_suites) {
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
        else {
          m_skipFailedInvocationCounts = xmlSuite.skipFailedInvocationCounts();
        }
        
        if (xmlSuite.getVerbose() == null) {
          xmlSuite.setVerbose(m_verbose);
        }
        
        PoolService.initialize(xmlSuite.getDataProviderThreadCount());
        result.add(createAndRunSuiteRunners(xmlSuite));
        PoolService.getInstance().shutdown();
      }
    }
    else {
      setStatus(HAS_NO_TEST);
      System.err.println("[ERROR]: No test suite found.  Nothing to run");
    }
    
    //
    // Generate the suites report
    //
    return result;
  }

  protected SuiteRunner createAndRunSuiteRunners(XmlSuite xmlSuite) {
    initializeAnnotationFinders();
    SuiteRunner result = new SuiteRunner(xmlSuite, 
        m_outputDir, 
        m_testRunnerFactory, 
        m_useDefaultListeners, 
        m_jdkAnnotationFinder,
        m_objectFactory,
        m_methodInterceptor,
        m_invokedMethodListeners);
    result.setSkipFailedInvocationCounts(m_skipFailedInvocationCounts);

    for (ISuiteListener isl : m_suiteListeners) {
      result.addListener(isl);
    }
    
    result.setTestListeners(m_testListeners);

    result.run();
    return result;
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
   * @return
   */
  public static TestNG privateMain(String[] argv, ITestListener listener) {
    Map arguments= checkConditions(TestNGCommandLineArgs.parseCommandLine(argv));
    
    TestNG result = new TestNG();
    if (null != listener) {
      result.addListener(listener);
    }

    result.configure(arguments);
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
   * Configure the TestNG instance by reading the settings provided in the map.
   * 
   * @param cmdLineArgs map of settings
   * @see TestNGCommandLineArgs for setting keys
   */
  @SuppressWarnings({"unchecked"})
  public void configure(Map cmdLineArgs) {
    {
      Integer verbose = (Integer) cmdLineArgs.get(TestNGCommandLineArgs.LOG);
      if (null != verbose) {
        setVerbose(verbose.intValue());
      }
    }
    
    setOutputDirectory((String) cmdLineArgs.get(TestNGCommandLineArgs.OUTDIR_COMMAND_OPT));
    setSourcePath((String) cmdLineArgs.get(TestNGCommandLineArgs.SRC_COMMAND_OPT));
    setAnnotations(((AnnotationTypeEnum) cmdLineArgs.get(TestNGCommandLineArgs.ANNOTATIONS_COMMAND_OPT)));

    List<Class> testClasses = (List<Class>) cmdLineArgs.get(TestNGCommandLineArgs.TESTCLASS_COMMAND_OPT);
    if (null != testClasses) {
      Class[] classes = (Class[]) testClasses.toArray(new Class[testClasses.size()]);
      setTestClasses(classes);
    }

    List<String> testNgXml = (List<String>) cmdLineArgs.get(TestNGCommandLineArgs.SUITE_DEF_OPT);
    if (null != testNgXml) {
      setTestSuites(testNgXml);
    }
    
    String useDefaultListeners = (String) cmdLineArgs.get(TestNGCommandLineArgs.USE_DEFAULT_LISTENERS);
    if (null != useDefaultListeners) {
      setUseDefaultListeners("true".equalsIgnoreCase(useDefaultListeners));
    }
    
    setGroups((String) cmdLineArgs.get(TestNGCommandLineArgs.GROUPS_COMMAND_OPT));
    setExcludedGroups((String) cmdLineArgs.get(TestNGCommandLineArgs.EXCLUDED_GROUPS_COMMAND_OPT));      
    setTestJar((String) cmdLineArgs.get(TestNGCommandLineArgs.TESTJAR_COMMAND_OPT));
    setJUnit((Boolean) cmdLineArgs.get(TestNGCommandLineArgs.JUNIT_DEF_OPT));
    setMaster( (String)cmdLineArgs.get(TestNGCommandLineArgs.MASTER_OPT));
    setSlave( (String)cmdLineArgs.get(TestNGCommandLineArgs.SLAVE_OPT));
    setSkipFailedInvocationCounts(
      (Boolean) cmdLineArgs.get(
        TestNGCommandLineArgs.SKIP_FAILED_INVOCATION_COUNT_OPT));
    
    String parallelMode = (String) cmdLineArgs.get(TestNGCommandLineArgs.PARALLEL_MODE);
    if (parallelMode != null) {
      setParallel(parallelMode);
    }
    
    String threadCount = (String) cmdLineArgs.get(TestNGCommandLineArgs.THREAD_COUNT);
    if (threadCount != null) {
      setThreadCount(Integer.parseInt(threadCount));
    }
    String dataProviderThreadCount = (String) cmdLineArgs.get(TestNGCommandLineArgs.DATA_PROVIDER_THREAD_COUNT);
    if (dataProviderThreadCount != null) {
      setDataProviderThreadCount(Integer.parseInt(dataProviderThreadCount));
    }
    String defaultSuiteName = (String) cmdLineArgs.get(TestNGCommandLineArgs.SUITE_NAME_OPT);
    if (defaultSuiteName != null) {
      setDefaultSuiteName(defaultSuiteName);
    }

    String defaultTestName = (String) cmdLineArgs.get(TestNGCommandLineArgs.TEST_NAME_OPT);
    if (defaultTestName != null) {
      setDefaultTestName(defaultTestName);
    }

    List<Class> listenerClasses = (List<Class>) cmdLineArgs.get(TestNGCommandLineArgs.LISTENER_COMMAND_OPT);
    if (null != listenerClasses) {
      setListenerClasses(listenerClasses);
    }
    
    Class objectFactory = (Class) cmdLineArgs.get(TestNGCommandLineArgs.OBJECT_FACTORY_COMMAND_OPT);
    if(null != objectFactory) {
      setObjectFactory(objectFactory);
    }

    Class runnerFactory = (Class) cmdLineArgs.get(TestNGCommandLineArgs.TESTRUNNER_FACTORY_COMMAND_OPT);
    if (null != runnerFactory) {
      setTestRunnerFactoryClass(runnerFactory);
    }

    List<ReporterConfig> reporterConfigs =
            (List<ReporterConfig>) cmdLineArgs.get(TestNGCommandLineArgs.REPORTERS_LIST);
    if (reporterConfigs != null) {
      for (ReporterConfig reporterConfig : reporterConfigs) {
        addReporter(reporterConfig);
      }
    }
  }

  private void setSkipFailedInvocationCounts(Boolean skip) {
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
  @SuppressWarnings({"unchecked"})
  protected static Map checkConditions(Map params) {
    // TODO CQ document why sometimes we throw exceptions and sometimes we exit. 
    List<String> testClasses = (List<String>) params.get(TestNGCommandLineArgs.TESTCLASS_COMMAND_OPT);
    List<String> testNgXml = (List<String>) params.get(TestNGCommandLineArgs.SUITE_DEF_OPT);
    Object testJar = params.get(TestNGCommandLineArgs.TESTJAR_COMMAND_OPT);
    Object slave = params.get(TestNGCommandLineArgs.SLAVE_OPT);

    if (testClasses == null && testNgXml == null && slave == null && testJar == null) {
      System.err.println("You need to specify at least one testng.xml or one class");
      usage();
      System.exit(-1);
    }

    if (VersionInfo.IS_JDK14) {
      String srcPath = (String) params.get(TestNGCommandLineArgs.SRC_COMMAND_OPT);

      if ((null == srcPath) || "".equals(srcPath)) {
        throw new TestNGException("No sourcedir was specified");
      }
    }
    
    String groups = (String) params.get(TestNGCommandLineArgs.GROUPS_COMMAND_OPT);
    String excludedGroups = (String) params.get(TestNGCommandLineArgs.EXCLUDED_GROUPS_COMMAND_OPT);
    
    if (testJar == null &&
        (null != groups || null != excludedGroups) && testClasses == null && testNgXml == null) {
      throw new TestNGException("Groups option should be used with testclass option");
    }
    
    // -slave & -master can't be set together
    if (params.containsKey(TestNGCommandLineArgs.SLAVE_OPT) && 
   		 params.containsKey(TestNGCommandLineArgs.MASTER_OPT)) {
   	 throw new TestNGException(TestNGCommandLineArgs.SLAVE_OPT + " can't be combined with " +
   	                           TestNGCommandLineArgs.MASTER_OPT);
    }
    
    return params;
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
  
  /**
   * Prints the usage message to System.out. This message describes all the command line
   * options.
   */
  public static void usage() {
    TestNGCommandLineArgs.usage();
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
  
  public boolean getSkipFailedInvocationCounts() {
    return m_skipFailedInvocationCounts;
  }
  
  public void setSkipFailedInvocationCounts(boolean skip) {
    m_skipFailedInvocationCounts = skip;
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
    
    public void onTestFailure(ITestResult result) {
      setHasRunTests();
      m_mainRunner.setStatus(HAS_FAILURE);
    }

    public void onTestSkipped(ITestResult result) {
      setHasRunTests();
      m_mainRunner.setStatus(HAS_SKIPPED);
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
      setHasRunTests();
      m_mainRunner.setStatus(HAS_FSP);
    }

    public void onTestSuccess(ITestResult result) {
      setHasRunTests();
    }

    public void onStart(ITestContext context) {
      setHasRunTests();
    }

    public void onFinish(ITestContext context) {
    }

    public void onTestStart(ITestResult result) {
      setHasRunTests();
    }
    
    private void setHasRunTests() {
      m_mainRunner.m_hasTests= true;
    }

    /**
     * @see org.testng.internal.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
     */
    public void onConfigurationFailure(ITestResult itr) {
      m_mainRunner.setStatus(HAS_FAILURE);
    }

    /**
     * @see org.testng.internal.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
     */
    public void onConfigurationSkip(ITestResult itr) {
      m_mainRunner.setStatus(HAS_SKIPPED);
    }

    /**
     * @see org.testng.internal.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
     */
    public void onConfigurationSuccess(ITestResult itr) {
    }
  }

  public void setMethodInterceptor(IMethodInterceptor methodInterceptor) {
    m_methodInterceptor = methodInterceptor;
  }

  public void setDataProviderThreadCount(int count) {
    m_dataProviderThreadCount = count;
  }
}
