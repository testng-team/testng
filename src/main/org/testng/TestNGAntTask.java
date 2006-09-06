package org.testng;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.selectors.FilenameSelector;

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
 * <li>reporter (attribute)</li>
 * <li>sourcedir (attribute)</li>
 * <li>sourcedirref (attribute)</li>
 * <li>suiterunnerclass (attribute)</li>
 * <li>target (attribute)</li>
 * <li>testjar (attribute)</li>
 * <li>verbose (attribute)</li>
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
 * <li>jvmarg (inner)</li>
 * <li>timeout (attribute)</li>
 * <li>haltonfailure (attribute)</li>
 * <li>failureProperty (attribute)</li>
 * <li>haltonFSP (attribute)</li>
 * <li>FSPproperty (attribute)</li>
 * <li>haltonskipped (attribute)</li>
 * <li>skippedProperty (attribute)</li>
 * </ul>
 * 
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alexandru Popescu</a>
 * @author Cedric Beust
 */
public class TestNGAntTask extends Task {
  protected CommandlineJava m_javaCommand;
  
  protected List<FileSet> m_xmlFilesets = new ArrayList<FileSet>();
  protected List<FileSet> m_classFilesets = new ArrayList<FileSet>();
  protected Path m_sourceDirPath;
  protected File m_outputDir;
  protected File m_testjar;
  protected File m_workingDir;
  private Integer m_timeout;
  protected Boolean m_isJUnit;
  private String m_listener;
  protected Environment m_environment = new Environment();
  protected String m_mainClass = TestNG.class.getName();
  protected String m_target;
  protected boolean m_dump;
  protected boolean m_assertEnabled = true;
  protected boolean m_haltOnFailure;
  protected String m_failurePropertyName;
  protected boolean m_haltOnSkipped;
  protected String m_skippedPropertyName;
  protected boolean m_haltOnFSP;
  protected String m_fspPropertyName;
  protected String m_includedGroups;
  protected String m_excludedGroups;
  protected String m_threadCount;
  
  public void setThreadCount(String threadCount) {
    m_threadCount = threadCount;
  }

  // Ant task settings
  public void setHaltonfailure(boolean value) {
    m_haltOnFailure = value;
  }

  public void setFailureProperty(String propertyName) {
    m_failurePropertyName = propertyName;
  }
  
  public void setHaltonskipped(boolean value) {
    m_haltOnSkipped = value;
  }
  
  public void setSkippedProperty(String propertyName) {
    m_skippedPropertyName = propertyName;
  }
  
  public void setHaltonFSP(boolean value) {
    m_haltOnFSP = value;
  }
  
  public void setFSPProperty(String propertyName) {
    m_fspPropertyName = propertyName;
  }
  
  /**
   * Sets the flag to log the command line.
   */
  public void setDumpCommand(boolean verbose) {
    m_dump = verbose;
  }

  public void setEnableAssert(boolean flag) {
    m_assertEnabled = flag;
  }

  /**
   * The directory to invoke the VM in.
   * @param   dir     the directory to invoke the JVM from.
   */
  public void setWorkingDir(File workingDir) {
    m_workingDir = workingDir;
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
      m_timeout = value;
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
    m_xmlFilesets.add(createFileSet(ref));
  }

  public void addClassfileset(FileSet fs) {
    m_classFilesets.add(appendClassSelector(fs));
  }

  public void setClassfilesetRef(Reference ref) {
    m_classFilesets.add(appendClassSelector(createFileSet(ref)));
  }

  /**
   * Sets the Path like for source directories.
   * @param srcDir path for source directories
   */
  public void setSourcedir(Path srcDir) {
    if(m_sourceDirPath == null) {
      m_sourceDirPath = srcDir;
    }
    else {
      m_sourceDirPath.append(srcDir);
    }
  }

  /**
   * Creates a nested src Path like.
   *
   * @return a new Path
   */
  public Path createSourceDir() {
    if(m_sourceDirPath == null) {
      m_sourceDirPath = new Path(getProject());
    }

    return m_sourceDirPath.createPath();
  }

  /**
   * Sets a reference to a Path-like structure for source directories.
   *
   * @param r reference to a Path representing the source directories
   */
  public void setSourceDirRef(Reference r) {
    createSourceDir().setRefid(r);
  }
  

  /**
   * Sets the suite runner class to invoke
   * @param s the name of the suite runner class
   */
  public void setSuiteRunnerClass(String s) {
    m_mainClass = s;
  }

  // TestNG settings
  public void setJUnit(boolean value) {
    m_isJUnit = new Boolean(value);
  }
  
  public void setTarget(String target) {
    m_target = target;
  }
  
  /**
   * Sets the test output directory
   * @param dir the name of directory
   */
  public void setOutputDir(File dir) {
    m_outputDir = dir;
  }

  /**
   * Sets the test jar
   * @param s the name of test jar
   */
  public void setTestJar(File s) {
    m_testjar = s;
  }

  public void setGroups(String groups) {
    m_includedGroups = groups;
  }
  
  public void setExcludedGroups(String groups) {
    m_excludedGroups = groups;
  }

  private Integer m_verbose = null;

  public void setVerbose(Integer verbose) {
    m_verbose = verbose;
  }
  
  public void setReporter(String listener) {
    m_listener = listener;
  }

  public void setListener(String listener) {
    m_listener = listener;
  }
  
  @Override
  public void execute() throws BuildException {
    validateOptions();
    
    CommandlineJava cmd = getJavaCommand();
    
    cmd.setClassname(m_mainClass);
    
    List<String> argv = new ArrayList<String>();
    
    if (null != m_isJUnit) {
      if (m_isJUnit.booleanValue()) {
        argv.add(TestNGCommandLineArgs.JUNIT_DEF_OPT);
      }
    }
    
    if (null != m_verbose) {
      argv.add(TestNGCommandLineArgs.LOG);
      argv.add(m_verbose.toString());
    }
    
    if(m_assertEnabled) {
      cmd.createVmArgument().setValue("-ea");
    }

    if((null != m_outputDir)) {
      if (! m_outputDir.exists()) {
        m_outputDir.mkdirs();
      }
      if (m_outputDir.isDirectory()) {
        argv.add(TestNGCommandLineArgs.OUTDIR_COMMAND_OPT);
        argv.add(m_outputDir.getAbsolutePath());
      }
      else {
        throw new BuildException("Output directory is not a directory: " + m_outputDir);
      }
    }

    if(null != m_target) {
      argv.add(TestNGCommandLineArgs.TARGET_COMMAND_OPT);
      argv.add(m_target);
    }
    
    if((null != m_testjar) && m_testjar.isFile()) {
      argv.add(TestNGCommandLineArgs.TESTJAR_COMMAND_OPT);
      argv.add(m_testjar.getAbsolutePath());
    }

    if(null != m_sourceDirPath) {
      String srcPath = createPathString(m_sourceDirPath, ";");
      argv.add(TestNGCommandLineArgs.SRC_COMMAND_OPT);
      argv.add(srcPath);
    }
    
    if(null != m_includedGroups) {
      argv.add(TestNGCommandLineArgs.GROUPS_COMMAND_OPT);
      argv.add(m_includedGroups);
    }
    
    if(null != m_excludedGroups) {
      argv.add(TestNGCommandLineArgs.EXCLUDED_GROUPS_COMMAND_OPT);
      argv.add(m_excludedGroups);
    }

    if(m_classFilesets.size() > 0) {
      argv.add(TestNGCommandLineArgs.TESTCLASS_COMMAND_OPT);
      for(String file: fileset(m_classFilesets)) {
        argv.add(file);
      }
    }

    if (m_listener != null) {
        argv.add(TestNGCommandLineArgs.LISTENER_COMMAND_OPT);
        argv.add(m_listener);
    }
    
    if (m_threadCount != null) {
      argv.add(TestNGCommandLineArgs.THREAD_COUNT);
      argv.add(m_threadCount);
    }

    if(m_xmlFilesets.size() > 0) {
      for(String file: fileset(m_xmlFilesets)) {
        argv.add(file);
      }
    }
    
    String fileName = "";
    FileWriter fw = null;
    BufferedWriter bw = null;
    try {
      File f = File.createTempFile("testng", "");
      fileName = f.getAbsolutePath();
      // If the user asked to see the command, preserve the file
      if (! m_dump) f.deleteOnExit();
      fw = new FileWriter(f);
      bw = new BufferedWriter(fw);
      for (String arg : argv) {
        bw.write(arg);
        bw.write(" ");
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if (bw != null) bw.close();
        if (fw != null) fw.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    if(m_dump) {
      dumpCommand(fileName);
    }
    
    createClasspath().setLocation(findJar());
    
    cmd.createVmArgument().setValue("-ea");
    
    cmd.createArgument().setValue("@" + fileName);

    ExecuteWatchdog watchdog = createWatchdog();
    boolean wasKilled = false;
    int exitValue = executeAsForked(cmd, watchdog);
    if(null != watchdog) {
      wasKilled = watchdog.killedProcess();
    }

    actOnResult(exitValue, wasKilled);
  }

  private void ppp(String string) {
    System.out.println("[TestNGAntTask] " + string);
  }

  protected void actOnResult(int exitValue, boolean wasKilled) {
    if(exitValue == -1) {
      throw new BuildException("an error occured when running TestNG tests");
    }
    
    if((exitValue & TestNG.HAS_NO_TEST) == TestNG.HAS_NO_TEST) {
      if(m_haltOnFailure) {
        throw new BuildException("No tests were run");
      }
      else {
        if(null != m_failurePropertyName) {
          getProject().setNewProperty(m_failurePropertyName, "true");
        }
        
        log("TestNG haven't found any tests to be run", Project.MSG_DEBUG);
      }
    }
    
    boolean failed = ((exitValue & TestNG.HAS_FAILURE) == TestNG.HAS_FAILURE) || wasKilled;
    if(failed) {
      final String msg = wasKilled ? "The tests timed out and were killed." : "The tests failed.";
      if(m_haltOnFailure) {
        throw new BuildException(msg);
      }
      else {
        if(null != m_failurePropertyName) {
          getProject().setNewProperty(m_failurePropertyName, "true");
        }
        
        log(msg, Project.MSG_DEBUG);
      }
    }
    
    if((exitValue & TestNG.HAS_SKIPPED) == TestNG.HAS_SKIPPED) {
      if(m_haltOnSkipped) {
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
  
  protected int executeAsForked(CommandlineJava cmd, ExecuteWatchdog watchdog) {
    Execute execute = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN), watchdog);
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
    
    String[] environment = m_environment.getVariables();
    if(null != environment) {
      for(String envEntry: environment) {
        log("Setting environment variable: " + envEntry, Project.MSG_VERBOSE);
      }
    }
    
    execute.setEnvironment(environment);
    
    log(cmd.describeCommand(), Project.MSG_VERBOSE);
    int retVal;
    boolean wasKilled = false;
    try {
      retVal = execute.execute();
    } 
    catch (IOException e) {
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
      if (m_timeout == null) {
          return null;
      }
      
      return new ExecuteWatchdog(m_timeout.longValue());
  }
  
  protected void validateOptions() throws BuildException {
    if(m_xmlFilesets.size() == 0 
        && m_classFilesets.size() == 0 
        && (null == m_testjar || !m_testjar.isFile())) {
      throw new BuildException("No suite or classes or jar is specified.");
    }
    
    if(null != m_includedGroups && m_classFilesets.size() == 0) {
      throw new BuildException("No classfileset specified while using groups");
    }
    
    if(usesJavadocAnnotations()) {
      m_target = null; // reset the target
      
      if(null == m_sourceDirPath) {
        throw new BuildException("No sourceDir is specified.");
      }
    }
    else {
      if(null == m_target) {
        m_target = "1.5";
      }
      else if(!"1.4".equals(m_target) && !"1.5".equals(m_target)) {
        throw new BuildException("Target attribute value unknown. Must be 1.4 or 1.5.");
      }
    }
  }
  
  private boolean usesJavadocAnnotations() {
    final String jversion = getJavaCommand().getVmversion();
    
    if(jversion.startsWith("1.1") 
        || jversion.startsWith("1.2") 
        || jversion.startsWith("1.3")
        || jversion.startsWith("1.4")) {
      return true;
    }
    
    return false;
  }
  
  private FileSet createFileSet(Reference ref) {
    FileSet fs = new FileSet();
    fs.setRefid(ref);
    fs.setProject(getProject());

    return fs;
  }
  
  private FileSet appendClassSelector(FileSet fs) {
    FilenameSelector selector = new FilenameSelector();
    selector.setName("**/*.class");
    selector.setProject(getProject());
    fs.appendSelector(selector);
    
    return fs;
  }
  
  private File findJar() {
    Class  thisClass = getClass();
    String resource = thisClass.getName().replace('.', '/') + ".class";
    URL url = thisClass.getClassLoader().getResource(resource);

    if(null != url) {
      String u = url.toString();
      if(u.startsWith("jar:file:")) {
        int    pling = u.indexOf("!");
        String jarName = u.substring(4, pling);
        return new File(fromURI(jarName));
      }
      else if(u.startsWith("file:")) {
        int    tail = u.indexOf(resource);
        String dirName = u.substring(0, tail);
        return new File(fromURI(dirName));
      }
    }
    
    return null;
  }

  private String fromURI(String uri) {
    URL url = null;
    try {
      url = new URL(uri);
    }
    catch(MalformedURLException murle) {
    }
    if((null == url) || !("file".equals(url.getProtocol()))) {
      throw new IllegalArgumentException("Can only handle valid file: URIs");
    }

    StringBuffer buf = new StringBuffer(url.getHost());
    if(buf.length() > 0) {
      buf.insert(0, File.separatorChar).insert(0, File.separatorChar);
    }

    String file = url.getFile();
    int    queryPos = file.indexOf('?');
    buf.append((queryPos < 0) ? file : file.substring(0, queryPos));

    uri = buf.toString().replace('/', File.separatorChar);

    if((File.pathSeparatorChar == ';') && uri.startsWith("\\") && (uri.length() > 2)
      && Character.isLetter(uri.charAt(1)) && (uri.lastIndexOf(':') > -1)) {
      uri = uri.substring(1);
    }

    StringBuffer      sb = new StringBuffer();
    CharacterIterator iter = new StringCharacterIterator(uri);
    for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
      if(c == '%') {
        char c1 = iter.next();
        if(c1 != CharacterIterator.DONE) {
          int  i1 = Character.digit(c1, 16);
          char c2 = iter.next();
          if(c2 != CharacterIterator.DONE) {
            int i2 = Character.digit(c2, 16);
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
  
  private List<String> fileset(List<FileSet> filesets) throws BuildException {
    List<String> files = new ArrayList<String>();
    
    for(Iterator<FileSet> iterator = filesets.iterator(); iterator.hasNext(); ) {
      FileSet fileset = iterator.next();
      DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
      
      for(String file: ds.getIncludedFiles()) {
        files.add(ds.getBasedir() + File.separator + file);
      }
    }
    
    return files;
  }
  
  /*private String filesetToString(List<FileSet> filesets, String sep) throws BuildException {
    final StringBuffer buf = new StringBuffer();

    for(Iterator<FileSet> iterator = filesets.iterator(); iterator.hasNext();) {
      FileSet          fileset = iterator.next();
      DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
      for(int i = 0; i < ds.getIncludedFiles().length; i++) {
        String file = ds.getIncludedFiles()[i];
        buf.append(ds.getBasedir() + File.separator + file).append(sep);
      }
    }

    return buf.toString();
  }*/
  
  /**
   * Creates a string representation of the path.
   */
  private String createPathString(Path path, String sep) {
    if(path == null) {
      return null;
    }

    final StringBuffer buf = new StringBuffer();

    for(int i = 0; i < path.list().length; i++) {
      File file = getProject().resolveFile(path.list()[i]);

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
    List<String> lines = TestNGCommandLineArgs.readFile(fileName);
    for (String line : lines) {
      ppp(line);
    }
  }
}
