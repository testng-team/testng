package org.testng.reporters;

import org.testng.ITestResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hani Suleiman Date: Mar 27, 2007 Time: 9:16:28 AM
 */
public class XMLReporterConfig {

  public static final String TAG_TEST = "test";
  public static final String TAG_TEST_METHOD = "test-method";
  public static final String TAG_EXCEPTION = "exception";
  public static final String TAG_MESSAGE = "message";
  public static final String TAG_SHORT_STACKTRACE = "short-stacktrace";
  public static final String TAG_FULL_STACKTRACE = "full-stacktrace";
  public static final String TAG_TESTNG_RESULTS = "testng-results";
  public static final String TAG_SUITE = "suite";
  public static final String TAG_GROUPS = "groups";
  public static final String TAG_GROUP = "group";
  public static final String TAG_CLASS = "class";
  public static final String TAG_METHOD = "method";
  public static final String TAG_PARAMS = "params";
  public static final String TAG_PARAM = "param";
  public static final String TAG_PARAM_VALUE = "value";
  public static final String TAG_REPORTER_OUTPUT = "reporter-output";
  public static final String TAG_LINE = "line";
  public static final String TAG_ATTRIBUTES = "attributes";
  public static final String TAG_ATTRIBUTE = "attribute";

  public static final String ATTR_URL = "url";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_STATUS = "status";
  public static final String ATTR_DESC = "description";
  public static final String ATTR_METHOD_SIG = "signature";
  public static final String ATTR_GROUPS = "groups";
  public static final String ATTR_CLASS = "class";
  public static final String ATTR_TEST_INSTANCE_NAME = "test-instance-name";
  public static final String ATTR_INDEX = "index";
  public static final String ATTR_IS_NULL = "is-null";
  public static final String ATTR_PACKAGE = "package";
  public static final String ATTR_STARTED_AT = "started-at";
  public static final String ATTR_FINISHED_AT = "finished-at";
  public static final String ATTR_DURATION_MS = "duration-ms";
  public static final String ATTR_IS_CONFIG = "is-config";
  public static final String ATTR_DEPENDS_ON_METHODS = "depends-on-methods";
  public static final String ATTR_DEPENDS_ON_GROUPS = "depends-on-groups";
  public static final String ATTR_DATA_PROVIDER = "data-provider";

  public static final String TEST_PASSED = "PASS";
  public static final String TEST_FAILED = "FAIL";
  public static final String TEST_SKIPPED = "SKIP";

  private static Map<String, Integer> STATUSES = new HashMap<String, Integer>() {{
    put(TEST_PASSED, ITestResult.SUCCESS);
    put(TEST_FAILED, ITestResult.FAILURE);
    put(TEST_SKIPPED, ITestResult.SKIP);
  }};

  public static Integer getStatus(String status) {
    return STATUSES.get(status);
  }

  /**
   * Indicates that no file fragmentation should be performed. This value
   * indicates the XML generator to write all the results in one big file. Not
   * recommended for large test suites.
   */
  public static final int FF_LEVEL_NONE = 1;
  /**
   * Will cause the XML generator to create separate files for each of the
   * suites. A separate directory will be generated for each suite having the
   * name of the suite and containing a <code>suite.xml</code> file that will be
   * referenced in the main file with an <code>url</code> attribute
   */
  public static final int FF_LEVEL_SUITE = 2;
  /**
   * It behaves like <code>FF_LEVEL_SUITE</code>, except that it will also
   * create a file for each <code>ISuiteResult</code>
   */
  public static final int FF_LEVEL_SUITE_RESULT = 3;

  /**
   * No stacktrace will be written in the output file
   */
  public static final int STACKTRACE_NONE = 0;
  /**
   * Write only a short version of the stacktrace
   */
  public static final int STACKTRACE_SHORT = 1;
  /**
   * Write only the full version of the stacktrace
   */
  public static final int STACKTRACE_FULL = 2;
  /**
   * Write both types of stacktrace
   */
  public static final int STACKTRACE_BOTH = 3;

  // note: We're hardcoding the 'Z' because Java doesn't support all the
  // intricacies of ISO-8601.
  static final String FMT_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * Indicates the way that the file fragmentation should be performed. Set this
   * property to one of the FF_LEVEL_* values for the desired output structure
   */
  private int fileFragmentationLevel = FF_LEVEL_NONE;

  /**
   * Stack trace output method for the failed tests using one of the
   * STACKTRACE_* constants.
   */
  private int stackTraceOutputMethod = STACKTRACE_FULL;

  /**
   * The root output directory where the XMLs will be written. This will default
   * for now to the default TestNG output directory
   */
  private String outputDirectory;

  /**
   * Indicates whether the <code>groups</code> attribute should be generated for
   * a <code>test-method</code> element. Defaults to false due to the fact that
   * this might be considered redundant because of the group generation in the
   * suite file.
   */
  private boolean generateGroupsAttribute = false;

  /**
   * When <code>true</code> it will generate the &lt;class&lt; element with a
   * <code>name</code> and a <code>package</code> attribute. Otherwise, the
   * fully qualified name will be used for the <code>name</code> attribute.
   */
  private boolean splitClassAndPackageNames = false;

  /**
   * Indicates whether the <code>depends-on-methods</code> attribute should be
   * generated for a <code>test-method</code> element
   */
  private boolean generateDependsOnMethods = true;

  /**
   * Indicates whether the <code>depends-on-groups</code> attribute should be
   * generated for a <code>test-method</code> element
   */
  private boolean generateDependsOnGroups = true;

  /**
   * Indicates whether {@link ITestResult} attributes should be generated for
   * each <code>test-method</code> element
   */
  private boolean generateTestResultAttributes = false;

  /**
   * The output format for timestamps
   */
  private String timestampFormat = FMT_DEFAULT;

  public int getFileFragmentationLevel() {
    return fileFragmentationLevel;
  }

  public void setFileFragmentationLevel(int fileFragmentationLevel) {
    this.fileFragmentationLevel = fileFragmentationLevel;
  }

  public int getStackTraceOutputMethod() {
    return stackTraceOutputMethod;
  }

  public void setStackTraceOutputMethod(int stackTraceOutputMethod) {
    this.stackTraceOutputMethod = stackTraceOutputMethod;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public boolean isGenerateGroupsAttribute() {
    return generateGroupsAttribute;
  }

  public void setGenerateGroupsAttribute(boolean generateGroupsAttribute) {
    this.generateGroupsAttribute = generateGroupsAttribute;
  }

  public boolean isSplitClassAndPackageNames() {
    return splitClassAndPackageNames;
  }

  public void setSplitClassAndPackageNames(boolean splitClassAndPackageNames) {
    this.splitClassAndPackageNames = splitClassAndPackageNames;
  }

  public String getTimestampFormat() {
    return timestampFormat;
  }

  public void setTimestampFormat(String timestampFormat) {
    this.timestampFormat = timestampFormat;
  }

  public boolean isGenerateDependsOnMethods() {
    return generateDependsOnMethods;
  }

  public void setGenerateDependsOnMethods(boolean generateDependsOnMethods) {
    this.generateDependsOnMethods = generateDependsOnMethods;
  }

  public boolean isGenerateDependsOnGroups() {
    return generateDependsOnGroups;
  }

  public void setGenerateDependsOnGroups(boolean generateDependsOnGroups) {
    this.generateDependsOnGroups = generateDependsOnGroups;
  }

  public void setGenerateTestResultAttributes(boolean generateTestResultAttributes) {
    this.generateTestResultAttributes = generateTestResultAttributes;
  }

  public boolean isGenerateTestResultAttributes() {
    return generateTestResultAttributes;
  }
}
