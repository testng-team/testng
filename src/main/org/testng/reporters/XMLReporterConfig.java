package org.testng.reporters;

/**
 * @author Hani Suleiman
 *         Date: Mar 27, 2007
 *         Time: 9:16:28 AM
 */
public class XMLReporterConfig {
  /**
   * Indicates that no file fragmentation should be performed. This value indicates the XML generator to write all the
   * results in one big file. Not recommended for large test suites.
   */
  public static final int FF_LEVEL_NONE = 1;
  /**
   * Will cause the XML generator to create separate files for each of the suites. A separate directory will be
   * generated for each suite having the name of the suite and containing a <code>suite.xml</code> file that will be
   * referenced in the main file with an <code>url</code> attribute
   */
  public static final int FF_LEVEL_SUITE = 2;
  /**
   * It behaves like <code>FF_LEVEL_SUITE</code>, except that it will also create a file for each
   * <code>ISuiteResult</code>
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

  public static final String TAG_TEST = "test";
  public static final String TAG_PASSED = "passed";
  public static final String TAG_FAILED = "failed";
  public static final String TAG_SKIPPED = "skipped";
  public static final String TAG_TEST_METHOD = "test-method";
  public static final String TAG_EXCEPTION = "exception";
  public static final String TAG_MESSAGE = "message";
  public static final String TAG_SHORT_STACKTRACE = "short-stacktrace";
  public static final String TAG_FULL_STACKTRACE = "full-stacktrace";
  public static final String TAG_TESTNG_RESULTS = "testng-results";
  public static final String TAG_SUITE = "suite";
  public static final String TAG_GROUPS = "groups";
  public static final String TAG_GROUP = "group";
  public static final String TAG_METHOD = "method";
  public static final String TAG_PARAMS = "params";
  public static final String TAG_PARAM = "param";
  public static final String TAG_PARAM_VALUE = "value";

  public static final String ATTR_URL = "url";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_DESC = "description";
  public static final String ATTR_METHOD_SIG = "signature";
  public static final String ATTR_GROUPS = "groups";
  public static final String ATTR_CLASS = "class";
  public static final String ATTR_INDEX = "index";
  public static final String ATTR_IS_NULL = "is-null";

  //note: We're hardcoding the 'Z' because Java doesn't support all the intricacies of ISO-8601.
  static final String FMT_DAY_MONTH_YEAR_TIME = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * Indicates the way that the file fragmentation should be performed. Set this property to one of the FF_LEVEL_*
   * values for the desired output structure
   */
  private int fileFragmentationLevel = FF_LEVEL_NONE;

  /**
   * Stack trace output method for the failed tests using one of the STACKTRACE_* constants.
   */
  private int stackTraceOutputMethod = STACKTRACE_FULL;

  /**
   * The root output directory where the XMLs will be written. This will default for now to the default TestNG output
   * directory
   */
  private String outputDirectory;

  /**
   * Indicates wheather the <code>groups</code> attribute should be generated for a <code>test-method</code> element.
   * Defaults to false due to the fact that this might be considered reduntant because of the group generation in the
   * suite file.
   */
  private boolean generateGroupsAttribute = false;

  /**
   * The output format for timestamps
   */
  private String timestampFormat = FMT_DAY_MONTH_YEAR_TIME;

  public XMLReporterConfig(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public int getStackTraceOutputMethod() {
    return stackTraceOutputMethod;
  }

  public void setStackTraceOutputMethod(int stackTraceOutputMethod) {
    this.stackTraceOutputMethod = stackTraceOutputMethod;
  }

  public int getFileFragmentationLevel() {
    return fileFragmentationLevel;
  }

  public void setFileFragmentationLevel(int fileFragmentationLevel) {
    this.fileFragmentationLevel = fileFragmentationLevel;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public boolean isGenerateGroupsAttribute() {
    return generateGroupsAttribute;
  }

  public void setGenerateGroupsAttribute(boolean generateGroupsAttribute) {
    this.generateGroupsAttribute = generateGroupsAttribute;
  }

  public String getTimestampFormat() {
    return timestampFormat;
  }

  public void setTimestampFormat(String timestampFormat) {
    this.timestampFormat = timestampFormat;
  }
}
