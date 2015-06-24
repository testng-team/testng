package org.testng;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.reporters.VerboseReporter;

import static java.lang.Boolean.TRUE;
import static org.testng.internal.Utils.isStringNotBlank;

/**
 * TestNG settings:
 * <ul>
 * <li>classfileset (inner)</li>
 * <li>classfilesetref (attribute)</li>
 * <li>xmlfileset (inner)</li>
 * <li>xmlfilesetref (attribute)</li>
 * <li>enableAssert (attribute)</li>
 * <li>excludedGroups (attribute)</li>
 * <li>groups (attribute)</li>
 * <li>junit (attribute)</li>
 * <li>listener (attribute)</li>
 * <li>outputdir (attribute)</li>
 * <li>parallel (attribute)</li>
 * <li>reporter (attribute)</li>
 * <li>sourcedir (attribute)</li>
 * <li>sourcedirref (attribute)</li>
 * <li>suitename (attribute)</li>
 * <li>suiterunnerclass (attribute)</li>
 * <li>target (attribute)</li>
 * <li>testjar (attribute)</li>
 * <li>testname (attribute)</li>
 * <li>threadcount (attribute)</li>
 * <li>dataproviderthreadcount (attribute)</li>
 * <li>verbose (attribute)</li>
 * <li>testrunfactory (attribute)</li>
 * <li>configFailurepolicy (attribute)</li>
 * <li>randomizeSuites (attribute)</li>
 * <li>methodselectors (attribute)</li>
 * </ul>
 *
 * Ant settings:
 * <ul>
 * <li>classpath (inner)</li>
 * <li>classpathref (attribute)</li>
 * <li>jvm (attribute)</li>
 * <li>workingDir (attribute)</li>
 * <li>env (inner)</li>
 * <li>sysproperty (inner)</li>
 * <li>propertyset (inner)</li>
 * <li>jvmarg (inner)</li>
 * <li>timeout (attribute)</li>
 * <li>haltonfailure (attribute)</li>
 * <li>onHaltTarget (attribute)</li>
 * <li>failureProperty (attribute)</li>
 * <li>haltonFSP (attribute)</li>
 * <li>FSPproperty (attribute)</li>
 * <li>haltonskipped (attribute)</li>
 * <li>skippedProperty (attribute)</li>
 * <li>testRunnerFactory (attribute)</li>
 * </ul>
 *
 * Debug information:
 * <ul>
 * <li>dumpCommand (boolean)</li>
 * <li>dumpEnv (boolean)</li>
 * <li>dumpSys (boolean)</li>
 * </ul>
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alexandru Popescu</a>
 * @author Cedric Beust
 * @author Lukas Jungmann
 */
public class TestNGAntTask extends Task {

  protected CommandlineJava m_javaCommand;

  protected List<ResourceCollection> m_xmlFilesets= Lists.newArrayList();
  protected List<ResourceCollection> m_classFilesets= Lists.newArrayList();
  protected File m_outputDir;
  protected File m_testjar;
  protected File m_workingDir;
  private Integer m_timeout;
  private List<String> m_listeners= Lists.newArrayList();
  private List<String> m_methodselectors= Lists.newArrayList();
  private String m_objectFactory;
  protected String m_testRunnerFactory;
  private boolean m_delegateCommandSystemProperties = false;

  protected Environment m_environment= new Environment();

  /** The suite runner name (defaults to TestNG.class.getName(). */
  protected String m_mainClass = TestNG.class.getName();

  /** True if the temporary file created by the Ant Task for command line parameters
   * to TestNG should be preserved after execution. */
  protected boolean m_dump;
  private boolean m_dumpEnv;
  private boolean m_dumpSys;

  protected boolean m_assertEnabled= true;
  protected boolean m_haltOnFailure;
  protected String m_onHaltTarget;
  protected String m_failurePropertyName;
  protected boolean m_haltOnSkipped;
  protected String m_skippedPropertyName;
  protected boolean m_haltOnFSP;
  protected String m_fspPropertyName;
  protected String m_includedGroups;
  protected String m_excludedGroups;
  protected String m_parallelMode;
  protected String m_threadCount;
  protected String m_dataproviderthreadCount;
  protected String m_configFailurePolicy;
  protected Boolean m_randomizeSuites;
  public String m_useDefaultListeners;
  private String m_suiteName="Ant suite";
  private String m_testName="Ant test";
  private Boolean m_skipFailedInvocationCounts;
  private String m_methods;
  private Mode mode = Mode.testng;

  public enum Mode {
      //lower-case to better look in build scripts
      testng, junit, mixed;
  }
  
  /**
   * The list of report listeners added via &lt;reporter&gt; sub-element of the Ant task
   */
  private List<ReporterConfig> reporterConfigs = Lists.newArrayList();

  private String m_testNames = "";

  public void setParallel(String parallel) {
    m_parallelMode= parallel;
  }

  public void setThreadCount(String threadCount) {
    m_threadCount= threadCount;
  }

  public void setDataProviderThreadCount(String dataproviderthreadCount) {
    m_dataproviderthreadCount = dataproviderthreadCount;
  }

  public void setUseDefaultListeners(String f) {
    m_useDefaultListeners= f;
  }

  // Ant task settings
  public void setHaltonfailure(boolean value) {
    m_haltOnFailure= value;
  }

  public void setOnHaltTarget(String targetName) {
    m_onHaltTarget= targetName;
  }

  public void setFailureProperty(String propertyName) {
    m_failurePropertyName= propertyName;
  }

  public void setHaltonskipped(boolean value) {
    m_haltOnSkipped= value;
  }

  public void setSkippedProperty(String propertyName) {
    m_skippedPropertyName= propertyName;
  }

  public void setHaltonFSP(boolean value) {
    m_haltOnFSP= value;
  }

  public void setFSPProperty(String propertyName) {
    m_fspPropertyName= propertyName;
  }

  public void setDelegateCommandSystemProperties(boolean value){
    m_delegateCommandSystemProperties = value;
  }

  /**
   * Sets the flag to log the command line. When verbose is set to true
   * the command line parameters are stored in a temporary file stored
   * in the user's default temporary file directory. The file created is
   * prefixed with "testng".
   */
  public void setDumpCommand(boolean verbose) {
    m_dump = verbose;
  }

  /**
   * Sets the flag to write on <code>System.out</code> the Ant
   * Environment properties.
   *
   * @param verbose <tt>true</tt> for printing
   */
  public void setDumpEnv(boolean verbose) {
    m_dumpEnv= verbose;
  }

  /**
   * Sets te flag to write on <code>System.out</code> the system properties.
   * @param verbose <tt>true</tt> for dumping the info
   */
  public void setDumpSys(boolean verbose) {
    m_dumpSys= verbose;
  }

  public void setEnableAssert(boolean flag) {
    m_assertEnabled= flag;
  }

  /**
   * The directory to invoke the VM in.
   * @param workingDir the directory to invoke the JVM from.
   */
  public void setWorkingDir(File workingDir) {
    m_workingDir= workingDir;
  }

  /**
   * Sets a particular JVM to be used. Default is 'java' and is solved
   * by <code>Runtime.exec()</code>.
   *
   * @param jvm the new jvm
   */
  public void setJvm(String jvm) {
    getJavaCommand().setVm(jvm);
  }

  /**
   * Set the timeout value (in milliseconds).
   *
   * <p>If the tests are running for more than this value, the tests
   * will be canceled.
   *
   * </p>
   * @param value the maximum time (in milliseconds) allowed before declaring the test as 'timed-out'
   */
  public void setTimeout(Integer value) {
    m_timeout= value;
  }

  public Commandline.Argument createJvmarg() {
    return getJavaCommand().createVmArgument();
  }

  public void addSysproperty(Environment.Variable sysp) {
    getJavaCommand().addSysproperty(sysp);
  }

  /**
   * Adds an environment variable; used when forking.
   */
  public void addEnv(Environment.Variable var) {
    m_environment.addVariable(var);
  }

  /**
   * Adds path to classpath used for tests.
   *
   * @return reference to the classpath in the embedded java command line
   */
  public Path createClasspath() {
    return getJavaCommand().createClasspath(getProject()).createPath();
  }

  /**
   * Adds a path to the bootclasspath.
   * @return reference to the bootclasspath in the embedded java command line
   */
  public Path createBootclasspath() {
    return getJavaCommand().createBootclasspath(getProject()).createPath();
  }

  /**
   * Set the classpath to be used when running the Java class
   *
   * @param s an Ant Path object containing the classpath.
   */
  public void setClasspath(Path s) {
    createClasspath().append(s);
  }

  /**
   * Classpath to use, by reference.
   *
   * @param r a reference to an existing classpath
   */
  public void setClasspathRef(Reference r) {
    createClasspath().setRefid(r);
  }

  public void addXmlfileset(FileSet fs) {
    m_xmlFilesets.add(fs);
  }

  public void setXmlfilesetRef(Reference ref) {
    m_xmlFilesets.add(createResourceCollection(ref));
  }

  public void addClassfileset(FileSet fs) {
    m_classFilesets.add(appendClassSelector(fs));
  }

  public void setClassfilesetRef(Reference ref) {
    m_classFilesets.add(createResourceCollection(ref));
  }

  public void setTestNames(String testNames) {
    m_testNames = testNames;
  }

  /**
   * Sets the suite runner class to invoke
   * @param s the name of the suite runner class
   */
  public void setSuiteRunnerClass(String s) {
    m_mainClass= s;
  }

  /**
   * Sets the suite name
   * @param s the name of the suite
   */
  public void setSuiteName(String s) {
    m_suiteName= s;
  }

  /**
   * Sets the test name
   * @param s the name of the test
   */
  public void setTestName(String s) {
    m_testName= s;
  }

  // TestNG settings
  public void setJUnit(boolean value) {
    mode = value ? Mode.junit : Mode.testng;
  }

  // TestNG settings
  public void setMode(Mode mode) {
    this.mode = mode;
  }

  /**
   * Sets the test output directory
   * @param dir the name of directory
   */
  public void setOutputDir(File dir) {
    m_outputDir= dir;
  }

  /**
   * Sets the test jar
   * @param s the name of test jar
   */
  public void setTestJar(File s) {
    m_testjar= s;
  }

  public void setGroups(String groups) {
    m_includedGroups= groups;
  }

  public void setExcludedGroups(String groups) {
    m_excludedGroups= groups;
  }

  private Integer m_verbose= null;

  private Integer m_suiteThreadPoolSize;

  private String m_xmlPathInJar;

  public void setVerbose(Integer verbose) {
    m_verbose= verbose;
  }

  public void setReporter(String listener) {
    m_listeners.add(listener);
  }

  public void setObjectFactory(String className) {
    m_objectFactory = className;
  }

  public void setTestRunnerFactory(String testRunnerFactory) {
    m_testRunnerFactory = testRunnerFactory;
  }

  public void setSuiteThreadPoolSize(Integer n) {
    m_suiteThreadPoolSize = n;
  }

  /**
   * @deprecated Use "listeners"
   */
  @Deprecated
  public void setListener(String listener) {
    m_listeners.add(listener);
  }

  public void setListeners(String listeners) {
    StringTokenizer st= new StringTokenizer(listeners, " ,");
    while(st.hasMoreTokens()) {
      m_listeners.add(st.nextToken());
    }
  }

  public void setMethodSelectors(String methodSelectors) {
	StringTokenizer st= new StringTokenizer(methodSelectors, " ,");
	while(st.hasMoreTokens()) {
	 m_methodselectors.add(st.nextToken());
	}
  }

  public void setConfigFailurePolicy(String failurePolicy) {
    m_configFailurePolicy = failurePolicy;
  }

  public void setRandomizeSuites(Boolean randomizeSuites) {
    m_randomizeSuites = randomizeSuites;
  }

  public void setMethods(String methods) {
    m_methods = methods;
  }

  /**
   * Launches TestNG in a new JVM.
   *
   * {@inheritDoc}
   */
  @Override
  public void execute() throws BuildException {
    validateOptions();

    CommandlineJava cmd = getJavaCommand();
    cmd.setClassname(m_mainClass);
    if(m_assertEnabled) {
      cmd.createVmArgument().setValue("-ea");
    }
    if (m_delegateCommandSystemProperties) {
      delegateCommandSystemProperties();
    }
    List<String> argv = createArguments();

    String fileName= "";
    FileWriter fw= null;
    BufferedWriter bw= null;
    try {
      File f= File.createTempFile("testng", "");
      fileName= f.getAbsolutePath();

      // If the user asked to see the command, preserve the file
      if(!m_dump) {
        f.deleteOnExit();
      }
      fw= new FileWriter(f);
      bw= new BufferedWriter(fw);
      for(String arg : argv) {
        bw.write(arg);
        bw.newLine();
      }
      bw.flush();
    }
    catch(IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if(bw != null) {
          bw.close();
        }
        if(fw != null) {
          fw.close();
        }
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    }

    printDebugInfo(fileName);

    createClasspath().setLocation(findJar());

    cmd.createArgument().setValue("@" + fileName);

    ExecuteWatchdog watchdog= createWatchdog();
    boolean wasKilled= false;
    int exitValue= executeAsForked(cmd, watchdog);
    if(null != watchdog) {
      wasKilled= watchdog.killedProcess();
    }

    actOnResult(exitValue, wasKilled);
  }

  private List<String> createArguments() {
	List<String> argv= Lists.newArrayList();
    addBooleanIfTrue(argv, CommandLineArgs.JUNIT, mode == Mode.junit);
    addBooleanIfTrue(argv, CommandLineArgs.MIXED, mode == Mode.mixed);
    addBooleanIfTrue(argv, CommandLineArgs.SKIP_FAILED_INVOCATION_COUNTS, m_skipFailedInvocationCounts);
    addIntegerIfNotNull(argv, CommandLineArgs.LOG, m_verbose);
    addDefaultListeners(argv);
    addOutputDir(argv);
    addFileIfFile(argv, CommandLineArgs.TEST_JAR, m_testjar);
    addStringIfNotBlank(argv, CommandLineArgs.GROUPS, m_includedGroups);
    addStringIfNotBlank(argv, CommandLineArgs.EXCLUDED_GROUPS, m_excludedGroups);
    addFilesOfRCollection(argv, CommandLineArgs.TEST_CLASS, m_classFilesets);
    addListOfStringIfNotEmpty(argv, CommandLineArgs.LISTENER, m_listeners);
    addListOfStringIfNotEmpty(argv, CommandLineArgs.METHOD_SELECTORS, m_methodselectors);
    addStringIfNotNull(argv, CommandLineArgs.OBJECT_FACTORY, m_objectFactory);
    addStringIfNotNull(argv, CommandLineArgs.TEST_RUNNER_FACTORY, m_testRunnerFactory);
    addStringIfNotNull(argv, CommandLineArgs.PARALLEL, m_parallelMode);
    addStringIfNotNull(argv, CommandLineArgs.CONFIG_FAILURE_POLICY, m_configFailurePolicy);
    addBooleanIfTrue(argv, CommandLineArgs.RANDOMIZE_SUITES, m_randomizeSuites);
    addStringIfNotNull(argv, CommandLineArgs.THREAD_COUNT, m_threadCount);
    addStringIfNotNull(argv, CommandLineArgs.DATA_PROVIDER_THREAD_COUNT, m_dataproviderthreadCount);
    addStringIfNotBlank(argv, CommandLineArgs.SUITE_NAME, m_suiteName);
    addStringIfNotBlank(argv, CommandLineArgs.TEST_NAME, m_testName);
    addStringIfNotBlank(argv, CommandLineArgs.TEST_NAMES, m_testNames);
    addStringIfNotBlank(argv, CommandLineArgs.METHODS, m_methods);
    addReporterConfigs(argv);
    addIntegerIfNotNull(argv, CommandLineArgs.SUITE_THREAD_POOL_SIZE, m_suiteThreadPoolSize);
    addStringIfNotNull(argv, CommandLineArgs.XML_PATH_IN_JAR, m_xmlPathInJar);
    addXmlFiles(argv);
	return argv;
  }

  private void addDefaultListeners(List<String> argv) {
    if (m_useDefaultListeners != null) {
      String useDefaultListeners = "false";
      if ("yes".equalsIgnoreCase(m_useDefaultListeners) || "true".equalsIgnoreCase(m_useDefaultListeners)) {
        useDefaultListeners = "true";
      }
      argv.add(CommandLineArgs.USE_DEFAULT_LISTENERS);
      argv.add(useDefaultListeners);
    }
  }

  private void addOutputDir(List<String> argv) {
    if (null != m_outputDir) {
      if (!m_outputDir.exists()) {
        m_outputDir.mkdirs();
      }
      if (m_outputDir.isDirectory()) {
        argv.add(CommandLineArgs.OUTPUT_DIRECTORY);
        argv.add(m_outputDir.getAbsolutePath());
      } else {
        throw new BuildException("Output directory is not a directory: " + m_outputDir);
      }
    }
  }

  private void addReporterConfigs(List<String> argv) {
    for (ReporterConfig reporterConfig : reporterConfigs) {
      argv.add(CommandLineArgs.REPORTER);
      argv.add(reporterConfig.serialize());
    }
  }

  private void addFilesOfRCollection(List<String> argv, String name, List<ResourceCollection> resources) {
	addArgumentsIfNotEmpty(argv, name, getFiles(resources), ",");
  }

  private void addListOfStringIfNotEmpty(List<String> argv, String name, List<String> arguments) {
	addArgumentsIfNotEmpty(argv, name, arguments, ";");
  }

  private void addArgumentsIfNotEmpty(List<String> argv, String name, List<String> arguments, String separator) {
	if (arguments != null && !arguments.isEmpty()) {
      argv.add(name);
      String value= Utils.join(arguments, separator);
	    argv.add(value);
  	}
  }

  private void addFileIfFile(List<String> argv, String name, File file) {
    if ((null != file) && file.isFile()) {
      argv.add(name);
      argv.add(file.getAbsolutePath());
    }
  }
  
  private void addBooleanIfTrue(List<String> argv, String name, Boolean value) {
    if (TRUE.equals(value)) {
      argv.add(name);
    }
  }
  
  private void addIntegerIfNotNull(List<String> argv, String name, Integer value) {
    if (value != null) {
      argv.add(name);
      argv.add(value.toString());
    }
  }
  
  private void addStringIfNotNull(List<String> argv, String name, String value) {
    if (value != null) {
      argv.add(name);
      argv.add(value);
    }
  }
  
  private void addStringIfNotBlank(List<String> argv, String name, String value) {
    if (isStringNotBlank(value)) {
      argv.add(name);
      argv.add(value);
    }
  }

  private void addXmlFiles(List<String> argv) {
    for (String file : getSuiteFileNames()) {
      argv.add(file);
    }
  }

  /**
   * @return the list of the XML file names. This method can be overridden by subclasses.
   */
  protected List<String> getSuiteFileNames() {
    List<String> result = Lists.newArrayList();

    for(String file : getFiles(m_xmlFilesets)) {
      result.add(file);
    }

    return result;
  }

  private void delegateCommandSystemProperties() {
    // Iterate over command-line args and pass them through as sysproperty
    // exclude any built-in properties that start with "ant."
    for (Object propKey : getProject().getUserProperties().keySet()) {
      String propName = (String) propKey;
      String propVal = getProject().getUserProperty(propName);
      if (propName.startsWith("ant.")) {
        log("Excluding ant property: " + propName + ": " + propVal, Project.MSG_DEBUG);
      }	else {
        log("Including user property: " + propName + ": " + propVal, Project.MSG_DEBUG);
        Environment.Variable var = new Environment.Variable();
        var.setKey(propName);
        var.setValue(propVal);
        addSysproperty(var);
      }
    }
  }

  private void printDebugInfo(String fileName) {
    if(m_dumpSys) {
      System.out.println("* SYSTEM PROPERTIES *");
      Properties props= System.getProperties();
      Enumeration en= props.propertyNames();
      while(en.hasMoreElements()) {
        String key= (String) en.nextElement();
        System.out.println(key + ": " + props.getProperty(key));
      }
      System.out.println("");
    }
    if(m_dumpEnv) {
      String[] vars= m_environment.getVariables();
      if(null != vars && vars.length > 0) {
        System.out.println("* ENVIRONMENT *");
        for(String v: vars) {
          System.out.println(v);
        }
        System.out.println("");
      }
    }
    if(m_dump) {
      dumpCommand(fileName);
    }
  }

  private void ppp(String string) {
    System.out.println("[TestNGAntTask] " + string);
  }

  protected void actOnResult(int exitValue, boolean wasKilled) {
    if(exitValue == -1) {
      executeHaltTarget(exitValue);
      throw new BuildException("an error occured when running TestNG tests");
    }

    if((exitValue & TestNG.HAS_NO_TEST) == TestNG.HAS_NO_TEST) {
      if(m_haltOnFailure) {
        executeHaltTarget(exitValue);
        throw new BuildException("No tests were run");
      }
      else {
        if(null != m_failurePropertyName) {
          getProject().setNewProperty(m_failurePropertyName, "true");
        }

        log("TestNG haven't found any tests to be run", Project.MSG_DEBUG);
      }
    }

    boolean failed= ((exitValue & TestNG.HAS_FAILURE) == TestNG.HAS_FAILURE) || wasKilled;
    if(failed) {
      final String msg= wasKilled ? "The tests timed out and were killed." : "The tests failed.";
      if(m_haltOnFailure) {
        executeHaltTarget(exitValue);
        throw new BuildException(msg);
      }
      else {
        if(null != m_failurePropertyName) {
          getProject().setNewProperty(m_failurePropertyName, "true");
        }

        log(msg, Project.MSG_INFO);
      }
    }

    if((exitValue & TestNG.HAS_SKIPPED) == TestNG.HAS_SKIPPED) {
      if(m_haltOnSkipped) {
        executeHaltTarget(exitValue);
        throw new BuildException("There are TestNG SKIPPED tests");
      }
      else {
        if(null != m_skippedPropertyName) {
          getProject().setNewProperty(m_skippedPropertyName, "true");
        }

        log("There are TestNG SKIPPED tests", Project.MSG_DEBUG);
      }
    }

    if((exitValue & TestNG.HAS_FSP) == TestNG.HAS_FSP) {
      if(m_haltOnFSP) {
        executeHaltTarget(exitValue);
        throw new BuildException("There are TestNG FAILED WITHIN SUCCESS PERCENTAGE tests");
      }
      else {
        if(null != m_fspPropertyName) {
          getProject().setNewProperty(m_fspPropertyName, "true");
        }

        log("There are TestNG FAILED WITHIN SUCCESS PERCENTAGE tests", Project.MSG_DEBUG);
      }
    }
  }

  /** Executes the target, if any, that user designates executing before failing the test */
  private void executeHaltTarget(int exitValue) {
    if(m_onHaltTarget != null) {
      if(m_outputDir != null) {
        getProject().setProperty("testng.outputdir", m_outputDir.getAbsolutePath());
      }
      getProject().setProperty("testng.returncode", String.valueOf(exitValue));
      Target t= (Target) getProject().getTargets().get(m_onHaltTarget);
      if(t != null) {
        t.execute();
      }
    }
  }

  /**
   * Executes the command line as a new process.
   *
   * @param cmd the command to execute
   * @param watchdog
   * @return the exit status of the subprocess or INVALID.
   */
  protected int executeAsForked(CommandlineJava cmd, ExecuteWatchdog watchdog) {
    Execute execute= new Execute(new TestNGLogSH(this, Project.MSG_INFO, Project.MSG_WARN, (m_verbose == null || m_verbose < 5)),
                                 watchdog);
    execute.setCommandline(cmd.getCommandline());
    execute.setAntRun(getProject());
    if(m_workingDir != null) {
      if(m_workingDir.exists() && m_workingDir.isDirectory()) {
        execute.setWorkingDirectory(m_workingDir);
      }
      else {
        log("Ignoring invalid working directory : " + m_workingDir, Project.MSG_WARN);
      }
    }

    String[] environment= m_environment.getVariables();
    if(null != environment) {
      for(String envEntry : environment) {
        log("Setting environment variable: " + envEntry, Project.MSG_VERBOSE);
      }
    }

    execute.setEnvironment(environment);

    log(cmd.describeCommand(), Project.MSG_VERBOSE);
    int retVal;
    try {
      retVal= execute.execute();
    }
    catch(IOException e) {
      throw new BuildException("Process fork failed.", e, getLocation());
    }

    return retVal;
  }

  /**
   * Creates or returns the already created <CODE>CommandlineJava</CODE>.
   */
  protected CommandlineJava getJavaCommand() {
    if(null == m_javaCommand) {
      m_javaCommand = new CommandlineJava();
    }

    return m_javaCommand;
  }

  /**
   * @return <tt>null</tt> if there is no timeout value, otherwise the
   * watchdog instance.
   *
   * @throws BuildException under unspecified circumstances
   * @since Ant 1.2
   */
  protected ExecuteWatchdog createWatchdog() /*throws BuildException*/ {
    if(m_timeout == null) {
      return null;
    }

    return new ExecuteWatchdog(m_timeout.longValue());
  }

  protected void validateOptions() throws BuildException {
    int suiteCount = getSuiteFileNames().size();
    if (suiteCount == 0
      && m_classFilesets.size() == 0
      && Utils.isStringEmpty(m_methods)
      && ((null == m_testjar) || !m_testjar.isFile())) {
      throw new BuildException("No suites, classes, methods or jar file was specified.");
    }

    if((null != m_includedGroups) && (m_classFilesets.size() == 0 && suiteCount == 0)) {
      throw new BuildException("No class filesets or xml file sets specified while using groups");
    }

    if(m_onHaltTarget != null) {
      if(!getProject().getTargets().containsKey(m_onHaltTarget)) {
        throw new BuildException("Target " + m_onHaltTarget + " not found in this project");
      }
    }

  }

  private ResourceCollection createResourceCollection(Reference ref) {
    Object o = ref.getReferencedObject();
    if (!(o instanceof ResourceCollection)) {
        throw new BuildException("Only File based ResourceCollections are supported.");
    }
    ResourceCollection rc = (ResourceCollection) o;
    if (!rc.isFilesystemOnly()) {
        throw new BuildException("Only ResourceCollections from local file system are supported.");
    }
    return rc;
  }

  private FileSet appendClassSelector(FileSet fs) {
    FilenameSelector selector= new FilenameSelector();
    selector.setName("**/*.class");
    selector.setProject(getProject());
    fs.appendSelector(selector);

    return fs;
  }

  private File findJar() {
    Class thisClass= getClass();
    String resource= thisClass.getName().replace('.', '/') + ".class";
    URL url= thisClass.getClassLoader().getResource(resource);

    if(null != url) {
      String u= url.toString();
      if(u.startsWith("jar:file:")) {
        int pling= u.indexOf("!");
        String jarName= u.substring(4, pling);

        return new File(fromURI(jarName));
      }
      else if(u.startsWith("file:")) {
        int tail= u.indexOf(resource);
        String dirName= u.substring(0, tail);

        return new File(fromURI(dirName));
      }
    }

    return null;
  }

  private String fromURI(String uri) {
    URL url= null;
    try {
      url= new URL(uri);
    }
    catch(MalformedURLException murle) {
    }
    if((null == url) || !("file".equals(url.getProtocol()))) {
      throw new IllegalArgumentException("Can only handle valid file: URIs");
    }

    StringBuffer buf= new StringBuffer(url.getHost());
    if(buf.length() > 0) {
      buf.insert(0, File.separatorChar).insert(0, File.separatorChar);
    }

    String file= url.getFile();
    int queryPos= file.indexOf('?');
    buf.append((queryPos < 0) ? file : file.substring(0, queryPos));

    uri= buf.toString().replace('/', File.separatorChar);

    if((File.pathSeparatorChar == ';') && uri.startsWith("\\") && (uri.length() > 2)
      && Character.isLetter(uri.charAt(1)) && (uri.lastIndexOf(':') > -1)) {
      uri= uri.substring(1);
    }

    StringBuffer sb= new StringBuffer();
    CharacterIterator iter= new StringCharacterIterator(uri);
    for(char c= iter.first(); c != CharacterIterator.DONE; c= iter.next()) {
      if(c == '%') {
        char c1= iter.next();
        if(c1 != CharacterIterator.DONE) {
          int i1= Character.digit(c1, 16);
          char c2= iter.next();
          if(c2 != CharacterIterator.DONE) {
            int i2= Character.digit(c2, 16);
            sb.append((char) ((i1 << 4) + i2));
          }
        }
      }
      else {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  /**
   * Returns the list of files corresponding to the resource collection
   *
   * @param resources
   * @return the list of files corresponding to the resource collection
   * @throws BuildException
   */
  private List<String> getFiles(List<ResourceCollection> resources) throws BuildException {
    List<String> files= Lists.newArrayList();
    for (ResourceCollection rc : resources) {
        for (Iterator i = rc.iterator(); i.hasNext();) {
          Object o = i.next();
          if (o instanceof FileResource) {
            FileResource fr = ((FileResource) o);
            if (fr.isDirectory()) {
              throw new BuildException("Directory based FileResources are not supported.");
            }
            if (!fr.isExists()) {
              log("'" + fr.toLongString() + "' does not exist", Project.MSG_VERBOSE);
            }
            files.add(fr.getFile().getAbsolutePath());
          } else {
              log("Unsupported Resource type: " + o.toString(), Project.MSG_VERBOSE);
          }
        }
    }
    return files;
  }

  /**
   * Returns the list of files corresponding to the fileset
   *
   * @param filesets
   * @return the list of files corresponding to the fileset
   * @throws BuildException
   */
  private List<String> fileset(FileSet fileset) throws BuildException {
    List<String> files= Lists.newArrayList();

      DirectoryScanner ds= fileset.getDirectoryScanner(getProject());

      for(String file : ds.getIncludedFiles()) {
        files.add(ds.getBasedir() + File.separator + file);
      }

    return files;
  }

  /**
   * Adds double quotes to the command line argument if it contains spaces.
   * @param pCommandLineArg the command line argument
   * @return pCommandLineArg in double quotes if it contains space.
   *
   */
  private static String doubleQuote(String pCommandLineArg) {
    if(pCommandLineArg.indexOf(" ") != -1 && !(pCommandLineArg.startsWith("\"") && pCommandLineArg.endsWith("\""))) {
      return "\"" + pCommandLineArg + '\"';
    }

    return pCommandLineArg;
  }

  /**
   * Creates a string representation of the path.
   */
  private String createPathString(Path path, String sep) {
    if(path == null) {
      return null;
    }

    final StringBuffer buf= new StringBuffer();

    for(int i= 0; i < path.list().length; i++) {
      File file= getProject().resolveFile(path.list()[i]);

      if(!file.exists()) {
        log("Classpath entry not found: " + file, Project.MSG_WARN);
      }

      buf.append(file.getAbsolutePath()).append(sep);
    }

    if(path.list().length > 0) { // cut the last ;
      buf.deleteCharAt(buf.length() - 1);
    }

    return buf.toString();
  }

  private void dumpCommand(String fileName) {
    ppp("TESTNG PASSED @" + fileName + " WHICH CONTAINS:");
    readAndPrintFile(fileName);
  }

  private void readAndPrintFile(String fileName) {
    File file = new File(fileName);
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      String line = br.readLine();
      while (line != null) {
        System.out.println("  " + line);
        line = br.readLine();
      }
    }
    catch(IOException ex) {
      ex.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void addConfiguredReporter(ReporterConfig reporterConfig) {
    reporterConfigs.add(reporterConfig);
  }

  public void setSkipFailedInvocationCounts(boolean skip) {
    m_skipFailedInvocationCounts = skip;
  }

  public void setXmlPathInJar(String path) {
    m_xmlPathInJar = path;
  }
  /**
   * Add the referenced property set as system properties for the TestNG JVM.
   *
   * @param sysPropertySet A PropertySet of system properties.
   */
  public void addConfiguredPropertySet(PropertySet sysPropertySet) {
    Properties properties = sysPropertySet.getProperties();
    log(properties.keySet().size() + " properties found in nested propertyset", Project.MSG_VERBOSE);
    for (Object propKeyObj : properties.keySet()) {
      String propKey = (String) propKeyObj;
      Environment.Variable sysProp = new Environment.Variable();
      sysProp.setKey(propKey);
      if (properties.get(propKey) instanceof String) {
        String propVal = (String) properties.get(propKey);
        sysProp.setValue(propVal);
        getJavaCommand().addSysproperty(sysProp);
        log("Added system property " + propKey + " with value " + propVal, Project.MSG_VERBOSE);
      } else {
        log("Ignoring non-String property " + propKey, Project.MSG_WARN);
      }
    }
  }

    @Override
    protected void handleOutput(String output) {
        if (output.startsWith(VerboseReporter.LISTENER_PREFIX)) {
            //send everything from VerboseReporter to verbose level unless log level is > 4
            log(output, m_verbose < 5 ? Project.MSG_VERBOSE : Project.MSG_INFO);
        } else {
            super.handleOutput(output);
        }
    }

    private static class TestNGLogOS extends LogOutputStream {

        private Task task;
        private boolean verbose;

        public TestNGLogOS(Task task, int level, boolean verbose) {
            super(task, level);
            this.task = task;
            this.verbose = verbose;
        }

        @Override
        protected void processLine(String line, int level) {
            if (line.startsWith(VerboseReporter.LISTENER_PREFIX)) {
                task.log(line, verbose ? Project.MSG_VERBOSE : Project.MSG_INFO);
            } else {
                super.processLine(line, level);
            }
        }
    }

    protected static class TestNGLogSH extends PumpStreamHandler {

        public TestNGLogSH(Task task, int outlevel, int errlevel, boolean verbose) {
            super(new TestNGLogOS(task, outlevel, verbose),
                    new LogOutputStream(task, errlevel));
        }
    }
}
