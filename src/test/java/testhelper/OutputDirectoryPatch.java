package testhelper;

/**
 * <code>OutputDirectoryPatch</code> is a helper class to provide an output directory
 * for TestNG tests that explicitly create an instance of TestNG and do not know the
 * output directory specified for the test.
 *
 * @author cquezel
 * @since 4.8
 */
public final class OutputDirectoryPatch {

  /** The default output directory name if none was specified. We should use something
   * different than "test-output" to make it clear that the output directory
   * has not been set. */
  private static final String DEFAULT_OUTPUT_DIRECTORY = "test-output";

  /** The name of the System property used to store the output directory. */
  private static final String OUTPUT_DIRECTORY_PROPERTY_NAME = "testng.outputDir";

  /**
   * Private constructor to disable instantiation.
   *
   * @since 4.8
   */
  private OutputDirectoryPatch() {
    // Hide constructor
  }

  /**
   * Returns the output directory as specified for the current test.
   *
   * @return the output directory as specified for the current test.
   * @since 4.8
   */
  public static String getOutputDirectory() {
    String tmp = System.getProperty(OUTPUT_DIRECTORY_PROPERTY_NAME);
    if (tmp != null) {
      return tmp;
    }
//    System.err.println("System property: " + OUTPUT_DIRECTORY_PROPERTY_NAME
//        + " has not been set. Using default path: " + DEFAULT_OUTPUT_DIRECTORY);

//    new Throwable("Stack is only to help locate the problem. No excpetion thrown.").printStackTrace(System.err);
    return DEFAULT_OUTPUT_DIRECTORY;
  }
}
