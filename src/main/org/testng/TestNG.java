package org.testng;


import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.internal.HostFile;
import org.testng.internal.Invoker;
import org.testng.internal.Utils;
import org.testng.internal.annotations.AnnotationConfiguration;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.ITest;
import org.testng.internal.remote.SlavePool;
import org.testng.internal.thread.IPooledExecutor;
import org.testng.internal.thread.ThreadUtil;
import org.testng.remote.ConnectionInfo;
import org.testng.remote.RemoteSuiteWorker;
import org.testng.remote.RemoteTestWorker;
import org.testng.reporters.EmailableReporter;
import org.testng.reporters.FailedReporter;
import org.testng.reporters.SuiteHTMLReporter;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.xml.sax.SAXException;

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
  /** The default name for a suite launched from the command line */
  public static final String DEFAULT_SUITE_NAME = "Command line suite";

  /** The default name for a test launched from the command line */
  public static final String DEFAULT_TEST_NAME = "Command line test";

  /** The default name of the result's output directory. */
  public static final String DEFAULT_OUTPUTDIR = "test-output";

  /** A separator constant (semi-colon). */
  public static final String SRC_SEPARATOR = ";";
  
  /** The JDK50 annotation type ID ("JDK5").*/
  public static final String JDK5_ANNOTATION_TYPE = "JDK5";
  
  /** The JavaDoc annotation type ID ("javadoc"). */
  public static final String JAVADOC_ANNOTATION_TYPE = "javadoc";
  
  
  private static TestNG m_instance;

  /** Indicates the TestNG JAR version (JDK 1.4 or JDK 5.0+). */  
  private static boolean m_isJdk14;

  protected List<XmlSuite> m_suites = new ArrayList<XmlSuite>();
  protected XmlSuite[] m_cmdlineSuites;
  protected String m_outputDir = DEFAULT_OUTPUTDIR;
  protected String[] m_sourceDirs;
  
  /** The annotation type for suites/tests that have not explicitly set this attribute. */
  protected String m_target;
  
  protected String[] m_includedGroups;
  protected String[] m_excludedGroups;
  
  private Boolean m_isJUnit = Boolean.FALSE;
  protected boolean m_useDefaultListeners = true;

  protected ITestRunnerFactory m_testRunnerFactory;

  // These listeners can be overridden from the command line
  protected List<ITestListener> m_testListeners = new ArrayList<ITestListener>();
  protected List<ISuiteListener> m_suiteListeners = new ArrayList<ISuiteListener>();
  private List<IReporter> m_reporters = new ArrayList<IReporter>();

  public static final int HAS_FAILURE = 1;
  public static final int HAS_SKIPPED = 2;
  public static final int HAS_FSP = 4;
  public static final int HAS_NO_TEST = 8;

  protected int m_status;
  protected boolean m_hasTests= false;
  
  /** The port on which this client will listen. */
  private int m_clientPort = 0;

  /** The name of the file containing the list of hosts where distributed
   * tests will be dispatched. */
  private String m_hostFile;

  private SlavePool m_slavePool = new SlavePool();

  // Command line suite parameters
  private int m_threadCount;
  private boolean m_useThreadCount;
  private String m_parallelMode;
  private boolean m_useParallelMode;
  private Class[] m_commandLineTestClasses;

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
    setTestNGVersion();
    
    // Set the default target for this version of TestNG
    // TODO CQ Since we have two code bases shoulld'nt this simply be part 
    // of a target specific class.
    m_target = m_isJdk14 ? JAVADOC_ANNOTATION_TYPE : JDK5_ANNOTATION_TYPE;
    m_useDefaultListeners = useDefaultListeners;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public static TestNG getDefault() {
    return m_instance;
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
   * </ul>
   * 
   * @see org.testng.reporters.TestHTMLReporter
   * @see org.testng.reporters.JUnitXMLReporter
   */
  public void setUseDefaultListeners(boolean useDefaultListeners) {
    m_useDefaultListeners = useDefaultListeners;
  }
  
  /**
   * The default annotation type for suites that have not explicitly set the annotation property.
   * The target is used only in JDK5+.
   * @param target the default annotation type (JAVADOC_ANNOTATION_TYPE or JDK5_ANNOTATION_TYPE).
   * For backward compatibility reasons we accept "1.4", "1.5" and any value
   */
  public void setTarget(final String target) {
    // Target is used only in JDK 1.5 and may get null in JDK 1.4
    if (null == target) { 
      return;
    }
    
    // The following switch block could be simplified with three test but the intent
    // is to log at different levels when warning.
    if (target.equals(JAVADOC_ANNOTATION_TYPE)) {
      m_target = JAVADOC_ANNOTATION_TYPE;
    }
    else if (target.equals(JDK5_ANNOTATION_TYPE)) {
      m_target = JDK5_ANNOTATION_TYPE;
    }
    else if (target.equals("1.4") 
        || target.toLowerCase().equals(JAVADOC_ANNOTATION_TYPE.toLowerCase())) {
      // For backward compatibility only
      // Log at info level 
      m_target = JAVADOC_ANNOTATION_TYPE;
      log("Illegal target type " + target + " defaulting to " + JAVADOC_ANNOTATION_TYPE);
    }
    else if ("1.5".equals(target) 
        || target.toLowerCase().equals(JDK5_ANNOTATION_TYPE.toLowerCase())) {
      // For backward compatibility only
      // Log at info level 
      m_target = JDK5_ANNOTATION_TYPE;
      log("Illegal target type " + target + " defaulting to " + JDK5_ANNOTATION_TYPE);
    }
    else if (target.toLowerCase().equals("jdk15")) {
      // For backward compatibility only
      // Log at info level 
      m_target = JDK5_ANNOTATION_TYPE;
      log("Illegal target type " + target + " defaulting to " + JDK5_ANNOTATION_TYPE);
    }
    else {
      // For backward compatibility only
      // Log at warn level 
      // TODO should we make this an error?
      m_target = JDK5_ANNOTATION_TYPE;
      log("Illegal target type " + target + " defaulting to " + JDK5_ANNOTATION_TYPE);
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
   * Note that for the override to occur, this method must be called. i.e. it is not sufficient
   * to place "testng-sourcedir-override.properties" in the classpath.
   * 
   * @param sourcePaths a semi-colon separated list of source directories. 
   */
  public void setSourcePath(String sourcePaths) {
    // Start of patch specific code
    // This is an optimization to reduce the sourcePath scope
    // Is it OK to look only for the Thread context class loader?
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testng-sourcedir-override.properties");

    // Resource exists. Use override values and ignore given value
    if (is != null) {
      Properties props = new Properties();
      try {
        props.load(is);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
      sourcePaths = props.getProperty("sourcedir");
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
    if ((null == jarPath) || "".equals(jarPath)) {
      return;
    }

    File jarFile = new File(jarPath);
    try {
      URL jarfile = new URL("jar", "", "file:" + jarFile.getAbsolutePath() + "!/");
      URLClassLoader jarLoader = new URLClassLoader(new URL[] { jarfile });
      Thread.currentThread().setContextClassLoader(jarLoader);

      m_suites.add(new Parser().parse());
    }
    catch(MalformedURLException mfurle) {
      System.err.println("could not find jar file named: " + jarFile.getAbsolutePath());
    }
    catch(IOException ioe) {
      System.out.println("An exception occurred while trying to load testng.xml from within jar "
                         + jarFile.getAbsolutePath());
    }
    catch(SAXException saxe) {
      System.out.println("testng.xml from within jar "
                         + jarFile.getAbsolutePath()
                         + " is not well formatted");
      saxe.printStackTrace(System.out);
    }
    catch(ParserConfigurationException pce) {
      pce.printStackTrace(System.out);
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
    m_cmdlineSuites = new XmlSuite[] { suite };
    m_suites.add(m_cmdlineSuites[0]);
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
  
  private int getVersion() {
    return JDK5_ANNOTATION_TYPE.equals(m_target) 
        ? AnnotationConfiguration.JVM_15_CONFIG
        : AnnotationConfiguration.JVM_14_CONFIG;
  }
  
  private XmlSuite[] createCommandLineSuites(Class[] classes) {
    //
    // See if any of the classes has an xmlSuite or xmlTest attribute.
    // If it does, create the appropriate XmlSuite, otherwise, create
    // the default one
    //
    XmlClass[] xmlClasses = Utils.classesToXmlClasses(classes);
    Map<String, XmlSuite> suites = new HashMap<String, XmlSuite>();
    IAnnotationFinder finder = SuiteRunner.getAnnotationFinder(getVersion());
    
    for (int i = 0; i < classes.length; i++) {
      Class c = classes[i];
      ITest test = (ITest) finder.findAnnotation(c, ITest.class);
      String suiteName = DEFAULT_SUITE_NAME;
      String testName = DEFAULT_TEST_NAME;
      if (test != null) {
        suiteName = test.getSuiteName();
        testName = test.getTestName();    
      }  
      XmlSuite xmlSuite = suites.get(suiteName);
      if (xmlSuite == null) {
        xmlSuite = new XmlSuite();
        xmlSuite.setName(suiteName);
        suites.put(suiteName, xmlSuite);
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
    
    XmlSuite[] result = 
      (XmlSuite[]) suites.values().toArray(new XmlSuite[suites.size()]);
        
    return result;
  }
  

  /**
   * Set the suites file names to be run by this TestNG object. This method tries to load and
   * parse the specified TestNG suite xml files. If a file is missing, it is ignored.
   *
   * @param suites A list of paths to one more XML files defining the tests.  For example:
   *
   * <pre>
   * TestNG tng = new TestNG();
   * List<String> suites = new ArrayList<String>();
   * suites.add("c:/tests/testng1.xml");
   * suites.add("c:/tests/testng2.xml");
   * tng.setTestSuites(suites);
   * tng.run();
   * </pre>
   */
  public void setTestSuites(List<String> suites) {
    for (String suiteXmlPath : suites) {
      try {
        XmlSuite s = new Parser(suiteXmlPath).parse();
        m_suites.add(s);
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
  

  private void setTestRunnerFactoryClass(Class testRunnerFactoryClass) {
    setTestRunnerFactory((ITestRunnerFactory) newInstance(testRunnerFactoryClass));
  }
  
  
  private void setTestRunnerFactory(ITestRunnerFactory itrf) {
    m_testRunnerFactory= itrf;
  }
  
  
  /**
   * Define which listeners to user for this run.
   * 
   * @param classes A list of classes, which must be either ISuiteListener,
   * ITestListener or IReporter
   */
  public void setListenerClasses(List<Class> classes) {
    for (Class cls: classes) {
      addListener(newInstance(cls));
    }
  }
  
  private void setListeners(List<Object> itls) {
    for (Object obj: itls) {
      addListener(obj);
    }
  }
    
  public void addListener(Object listener) {
    if (! (listener instanceof ISuiteListener) 
        && ! (listener instanceof ITestListener)
        && ! (listener instanceof IReporter))
    {
      exitWithError("Listener " + listener + " is neither an ITestListener, ISuiteListener nor IReporter");
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
    }
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
  private Integer m_verbose;

  /**
   * Sets the level of verbosity. This value will override the value specified 
   * in the test suites.
   * 
   * @param verbose the verbosity level (0 to 10 where 10 is most detailed)
   * Actually, this is a lie:  you can specify -1 and this will put TestNG
   * in debug mode (no longer slicing off stack traces and all). 
   */
  public void setVerbose(int verbose) {
    m_verbose = new Integer(verbose);
  }

  private void initializeSources() {
    if(null != m_sourceDirs) {
      if(isJdk14() || JAVADOC_ANNOTATION_TYPE.equals(m_target)) {
        AnnotationConfiguration.getInstance().getAnnotationFinder().addSourceDirs(m_sourceDirs);
      }
    }
  }
  
  private void initializeCommandLineSuites() {
    if(null != m_commandLineTestClasses) {
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
      m_reporters.add(new EmailableReporter());
    }
  }
  
  /**
   * Run TestNG.
   */
  public void run() {
    initializeListeners();
    initializeSources();
    initializeCommandLineSuites();
    initializeCommandLineSuitesParams();
    initializeCommandLineSuitesGroups();
    
    List<ISuite> suiteRunners = null;
    
    //
    // Slave mode
    //
    if (m_clientPort != 0) {
      waitForSuites();
    }
    
    //
    // Regular mode
    //
    else if (m_hostFile == null) {
      suiteRunners = runSuitesLocally();
    }
    
    //
    // Master mode
    //
    else {
      suiteRunners = runSuitesRemotely();
    }
    
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
  
  private static ConnectionInfo resetSocket(int clientPort, ConnectionInfo oldCi) 
    throws IOException 
  {
    ConnectionInfo result = new ConnectionInfo();
    ServerSocket serverSocket = new ServerSocket(clientPort);
    serverSocket.setReuseAddress(true);
    log("Waiting for connections on port " + clientPort);
    Socket socket = serverSocket.accept();
    result.setSocket(socket);
    
    return result;
  }

  /**
   * Invoked in client mode.  In this case, wait for a connection
   * on the given port, run the XmlSuite we received and return the SuiteRunner
   * created to run it.
   * @throws IOException 
   */
  private void waitForSuites() {
    try {
      ConnectionInfo ci = resetSocket(m_clientPort, null);
      while (true) {
        try {
          XmlSuite s = (XmlSuite) ci.getOis().readObject();
          log("Processing " + s.getName());
          m_suites = new ArrayList<XmlSuite>();
          m_suites.add(s);
          List<ISuite> suiteRunners = runSuitesLocally();
          ISuite sr = suiteRunners.get(0);
          log("Done processing " + s.getName());
          ci.getOos().writeObject(sr);
        }
        catch (ClassNotFoundException e) {
          e.printStackTrace(System.out);
        }      
        catch(EOFException ex) {
          log("Connection closed " + ex.getMessage());
          ci = resetSocket(m_clientPort, ci);
        }
        catch(SocketException ex) {
          log("Connection closed " + ex.getMessage());
          ci = resetSocket(m_clientPort, ci);
        }
      }
    }
    catch(IOException ex) {
      ex.printStackTrace(System.out);
    }
  }

  private static void log(String string) {
    Utils.log("", 2, string);
  }

  private List<ISuite> runSuitesRemotely() {
    List<ISuite> result = new ArrayList<ISuite>();
    HostFile hostFile = new HostFile(m_hostFile);
    
    //
    // Create one socket per host found
    //
    String[] hosts = hostFile.getHosts();
    Socket[] sockets = new Socket[hosts.length];
    for (int i = 0; i < hosts.length; i++) {
      String host = hosts[i];
      String[] s = host.split(":");
      try {
        sockets[i] = new Socket(s[0], Integer.parseInt(s[1]));
      }
      catch (NumberFormatException e) {
        e.printStackTrace(System.out);
      }
      catch (UnknownHostException e) {
        e.printStackTrace(System.out);
      }
      catch (IOException e) {
        Utils.error("Couldn't connect to " + host + ": " + e.getMessage());
      }
    }
    
    //
    // Add these hosts to the pool
    //
    try {
      m_slavePool.addSlaves(sockets);
    }
    catch (IOException e1) {
      e1.printStackTrace(System.out);
    }

    //
    // Dispatch the suites/tests to each host
    //
    List<Runnable> workers = new ArrayList<Runnable>();
    //
    // Send one XmlTest at a time to remote hosts
    //
    if (hostFile.isStrategyTest()) {
      for (XmlSuite suite : m_suites) {
        suite.setVerbose(hostFile.getVerbose());
        SuiteRunner suiteRunner = new SuiteRunner(suite, m_outputDir);
        for (XmlTest test : suite.getTests()) {
          XmlSuite tmpSuite = new XmlSuite();
          tmpSuite.setXmlPackages(suite.getXmlPackages());
          tmpSuite.setAnnotations(suite.getAnnotations());
          tmpSuite.setJUnit(suite.isJUnit());
          tmpSuite.setName("Temporary suite for " + test.getName());
          tmpSuite.setParallel(suite.getParallel());
          tmpSuite.setParameters(suite.getParameters());
          tmpSuite.setThreadCount(suite.getThreadCount());
          tmpSuite.setVerbose(suite.getVerbose());
          XmlTest tmpTest = new XmlTest(tmpSuite);
          tmpTest.setAnnotations(test.getAnnotations());
          tmpTest.setBeanShellExpression(test.getExpression());
          tmpTest.setClassNames(test.getXmlClasses());
          tmpTest.setExcludedGroups(test.getExcludedGroups());
          tmpTest.setIncludedGroups(test.getIncludedGroups());
          tmpTest.setJUnit(test.isJUnit());
          tmpTest.setMethodSelectors(test.getMethodSelectors());
          tmpTest.setName(test.getName());
          tmpTest.setParallel(test.getParallel());
          tmpTest.setParameters(test.getParameters());
          tmpTest.setVerbose(test.getVerbose());
          tmpTest.setXmlClasses(test.getXmlClasses());
          tmpTest.setXmlPackages(test.getXmlPackages());
          
          workers.add(new RemoteTestWorker(tmpSuite, m_slavePool, suiteRunner, result));
        }
        result.add(suiteRunner);  
      }        
    }
    //
    // Send one XmlSuite at a time to remote hosts
    //
    else {
      for (XmlSuite suite : m_suites) {
        workers.add(new RemoteSuiteWorker(suite, m_slavePool, result));
      }
    }

    //
    // Launch all the workers
    //
    IPooledExecutor executor= ThreadUtil.createPooledExecutor(1);
    for (Runnable r : workers) {
      executor.execute(r);
    }

    //
    // Wait for completion
    //
    executor.shutdown();
    // TODO(cbeust)
    // Need to make this configurable
    long maxTimeOut= 10 * 1000; // 10 minutes
    try {
      executor.awaitTermination(maxTimeOut);
    }
    catch (InterruptedException e) {
      e.printStackTrace(System.out);
    }
    
    //
    // Run test listeners
    //
    for (ISuite suite : result) {
      for (ISuiteResult suiteResult : suite.getResults().values()) {
        Collection<ITestResult> allTests[] = new Collection[] {
            suiteResult.getTestContext().getPassedTests().getAllResults(),
            suiteResult.getTestContext().getFailedTests().getAllResults(),  
            suiteResult.getTestContext().getSkippedTests().getAllResults(),  
            suiteResult.getTestContext().getFailedButWithinSuccessPercentageTests().getAllResults(),  
        };
        for (Collection<ITestResult> all : allTests) {
          for (ITestResult tr : all) {
            Invoker.runTestListeners(tr, m_testListeners);
          }
        }
      }
    }
    
    return result;
  }

  /**
   * This needs to be public for maven2, for now..At least
   * until an alternative mechanism is found.
   * @return
   */
  public List<ISuite> runSuitesLocally() {
    List<ISuite> result = new ArrayList<ISuite>();
    if (m_suites.size() > 0) {
      for (XmlSuite xmlSuite : m_suites) {
        
        // TODO CQ remove the ClassSuite
//        if (xmlSuite instanceof ClassSuite) {
//          xmlSuite.setAnnotations(m_target);
//        }
        
        if (null != m_isJUnit) {
          xmlSuite.setJUnit(m_isJUnit);
        }
        
        // TODO CQ is this OK? Should the command line verbose flag override 
        // what is explicitly specified in the suite?
        if (null != m_verbose) {
          xmlSuite.setVerbose(m_verbose);
        }
        
        result.add(createAndRunSuiteRunners(xmlSuite));
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

  private SuiteRunner createAndRunSuiteRunners(XmlSuite xmlSuite) {
    SuiteRunner result = null;
    if (null != m_testRunnerFactory) {
      result = new SuiteRunner(xmlSuite, m_outputDir, m_testRunnerFactory, m_useDefaultListeners);
    }
    else {
      result = new SuiteRunner(xmlSuite, m_outputDir, m_useDefaultListeners);
    }
    
    for (ISuiteListener isl : m_suiteListeners) {
      result.addListener(isl);
    }
    
    result.setTestListeners(m_testListeners);
    // Set the hostname, if any
    if (m_clientPort != 0) {
      try {
        result.setHost(InetAddress.getLocalHost() + ":" + m_clientPort);
      }
      catch (UnknownHostException e) {
        e.printStackTrace(System.out);
      }
    }

    result.run();
    return result;
  }

  private Object newInstance(Class clazz) {
    try {
      Object instance = clazz.newInstance();

      return instance;
    }
    catch(IllegalAccessException iae) {
      throw new TestNGException("Class " 
          + clazz.getName()
          + " does not have a no-args constructor",
          iae);
    }
    catch(InstantiationException ie) {
      throw new TestNGException("Cannot instantiate class "
          + clazz.getName(),
          ie);
    }
    catch(ExceptionInInitializerError eiierr) {
      throw new TestNGException("An exception occurred in static initialization of class "
          + clazz.getName(),
          eiierr);
    }
    catch(SecurityException se) {
      throw new TestNGException(se);
    }
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
   * TODO cquezel JavaDoc.
   *
   * @param argv
   * @param listener
   * @return 
   */
  public static TestNG privateMain(String[] argv, ITestListener listener) {
    Map cmdLineArgs = TestNGCommandLineArgs.parseCommandLine(argv);

    setTestNGVersion();

    TestNG result = new TestNG();
    if (null != listener) {
      result.addListener(listener);
    }

    try {
      checkConditions(cmdLineArgs);
      
      {
        Integer verbose = (Integer) cmdLineArgs.get(TestNGCommandLineArgs.LOG);
        if (null != verbose) {
          result.setVerbose(verbose.intValue());
        }
      }
      
      result.setOutputDirectory((String) cmdLineArgs.get(TestNGCommandLineArgs.OUTDIR_COMMAND_OPT));
      result.setSourcePath((String) cmdLineArgs.get(TestNGCommandLineArgs.SRC_COMMAND_OPT));
      result.setTarget((String) cmdLineArgs.get(TestNGCommandLineArgs.TARGET_COMMAND_OPT));

      List<String> testClasses = (List<String>) cmdLineArgs.get(TestNGCommandLineArgs.TESTCLASS_COMMAND_OPT);
      if (null != testClasses) {
        Class[] classes = (Class[]) testClasses.toArray(new Class[testClasses.size()]);
        result.setTestClasses(classes);
      }

      List<String> testNgXml = (List<String>) cmdLineArgs.get(TestNGCommandLineArgs.SUITE_DEF_OPT);
      if (null != testNgXml) {
        result.setTestSuites(testNgXml);
      }
      
      String useDefaultListeners = (String) cmdLineArgs.get(TestNGCommandLineArgs.USE_DEFAULT_LISTENERS);
      if (null != useDefaultListeners) {
        result.setUseDefaultListeners("true".equalsIgnoreCase(useDefaultListeners));
      }
      
      result.setGroups((String) cmdLineArgs.get(TestNGCommandLineArgs.GROUPS_COMMAND_OPT));
      result.setExcludedGroups((String) cmdLineArgs.get(TestNGCommandLineArgs.EXCLUDED_GROUPS_COMMAND_OPT));      
      result.setTestJar((String) cmdLineArgs.get(TestNGCommandLineArgs.TESTJAR_COMMAND_OPT));
      result.setJUnit((Boolean) cmdLineArgs.get(TestNGCommandLineArgs.JUNIT_DEF_OPT));
      result.setHostFile((String) cmdLineArgs.get(TestNGCommandLineArgs.HOSTFILE_OPT));
      
      String threadCount = (String) cmdLineArgs.get(TestNGCommandLineArgs.THREAD_COUNT);
      if (threadCount != null) {
        result.setThreadCount(Integer.parseInt(threadCount));
      }
      
      String client = (String) cmdLineArgs.get(TestNGCommandLineArgs.SLAVE_OPT);
      if (client != null) {
        result.setClientPort(Integer.parseInt(client));
      }

      List<Class> listenerClasses = (List<Class>) cmdLineArgs.get(TestNGCommandLineArgs.LISTENER_COMMAND_OPT);
      if (null != listenerClasses) {
        result.setListenerClasses(listenerClasses);
      }
      
      result.run();
    }
    catch(TestNGException ex) {
      if (TestRunner.getVerbose() > 1) {
        ex.printStackTrace(System.out);
      }
      else {
        System.err.println("[ERROR]: " + ex.getMessage());
      }
      System.exit(1);
    }

    return result;
  }

  private void setClientPort(int clientPort) {
    m_clientPort = clientPort;
  }

  /**
   * Set the path to the file that contains the list of slaves.
   * @param hostFile
   */
  public void setHostFile(String hostFile) {
    m_hostFile = hostFile;
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
   * Establish if TestNG for JDK1.4 or JDK 5.0+.
   */
  public static void setTestNGVersion() {
    // TODO CQ why go through all this when there are two code bases 
    // and thus an easy way to get the version? 
    try {
      Class.forName("org.testng.annotations.Test");
      m_isJdk14 = false;
    }
    catch(ClassNotFoundException ex) {
      m_isJdk14 = true;
    }
  }

  /**
   * Returns true if this is the JDK 1.4 JAR version of TestNG, false otherwise.
   *
   * @return true if this is the JDK 1.4 JAR version of TestNG, false otherwise.
   */
  public static boolean isJdk14() {
    return m_isJdk14;
  }

  /**
   * Checks TestNG preconditions. For example, this method makes sure that if this is the
   * JDK 1.4 version of TestNG, a source directory has been specified. This method calls
   * System.exit(-1) or throws an exception if the preconditions are not satisfied.
   * 
   * @param params the parsed command line parameters.
   */
  private static void checkConditions(Map params) {
    // TODO CQ document why sometimes we throw exceptions and sometimes we exit. 
    List<String> testClasses = (List<String>) params.get(TestNGCommandLineArgs.TESTCLASS_COMMAND_OPT);
    List<String> testNgXml = (List<String>) params.get(TestNGCommandLineArgs.SUITE_DEF_OPT);
    Object testJar = params.get(TestNGCommandLineArgs.TESTJAR_COMMAND_OPT);
    Object port = params.get(TestNGCommandLineArgs.SLAVE_OPT);

    if (testClasses == null && testNgXml == null && port == null && testJar == null) {
      System.err.println("You need to specify at least one testng.xml or one class");
      usage();
      System.exit(-1);
    }

    if (isJdk14()) {
      String srcPath = (String) params.get(TestNGCommandLineArgs.SRC_COMMAND_OPT);

      if ((null == srcPath) || "".equals(srcPath)) {
        throw new TestNGException("No sourcedir was specified");
      }
    }
    
    String groups = (String) params.get(TestNGCommandLineArgs.GROUPS_COMMAND_OPT);
    String excludedGroups = (String) params.get(TestNGCommandLineArgs.EXCLUDED_GROUPS_COMMAND_OPT);
    
    if ((null != groups || null != excludedGroups) && null == testClasses) {
      throw new TestNGException("Groups option should be used with testclass option");
    }
  }

  private static void ppp(String s) {
    System.out.println("[TestNG] " + s);
  }

  /**
   * @return true if at least one test failed.
   */
  public boolean hasFailure() {
    return (getStatus() & HAS_FAILURE) == HAS_FAILURE;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public void setHasFailure(boolean hasFailure) {
    m_status |= HAS_FAILURE;
  }

  /**
   * @return true if at least one test failed within success percentage.
   */
  public boolean hasFailureWithinSuccessPercentage() {
    return (getStatus() & HAS_FSP) == HAS_FSP;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public void setHasFailureWithinSuccessPercentage(boolean hasFailureWithinSuccessPercentage) {
    m_status |= HAS_FSP;
  }

  /**
   * @return true if at least one test was skipped.
   */
  public boolean hasSkip() {
    return (getStatus() & HAS_SKIPPED) == HAS_SKIPPED;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public void setHasSkip(boolean hasSkip) {
    m_status |= HAS_SKIPPED;
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

  public static class ExitCodeListener implements ITestListener {
    protected TestNG m_mainRunner;
    
    public ExitCodeListener() {
      m_mainRunner = TestNG.m_instance;
    }

    public ExitCodeListener(TestNG runner) {
      m_mainRunner = runner;
    }
    
    public void onTestFailure(ITestResult result) {
      m_mainRunner.m_status |= HAS_FAILURE;
    }

    public void onTestSkipped(ITestResult result) {
      m_mainRunner.m_status |= HAS_SKIPPED;
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
      m_mainRunner.m_status |= HAS_FSP;
    }

    public void onTestSuccess(ITestResult result) {
    }

    public void onStart(ITestContext context) {
    }

    public void onFinish(ITestContext context) {
    }

    public void onTestStart(ITestResult result) {
      setHasRunTests();
    }
    
    private void setHasRunTests() {
      m_mainRunner.m_hasTests= true;
    }
  }
}
