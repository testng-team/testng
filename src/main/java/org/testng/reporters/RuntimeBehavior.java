package org.testng.reporters;

/** This class houses handling all JVM arguments related to TestNG's default reports. */
public class RuntimeBehavior {
  public static final String FILE_NAME = "testng-results.xml";

  private RuntimeBehavior() {}

  public static boolean verboseMode() {
    return System.getProperty("fileStringBuffer") != null;
  }

  public static String getDefaultEmailableReport2Name() {
    return System.getProperty("emailable.report2.name");
  }

  public static String getDefaultEmailableReportName() {
    return System.getProperty("emailable.report.name");
  }

  public static String getDefaultStacktraceLevels() {
    return System.getProperty(
        "stacktrace.success.output.level", XMLReporterConfig.StackTraceLevels.FULL.toString());
  }

  public static String getDefaultFileNameForXmlReports() {
    return System.getProperty("testng.report.xml.name", FILE_NAME);
  }

  public static String getDefaultLineSeparator() {
    return System.getProperty("line.separator");
  }

  public static String getLineSeparatorOrNewLine() {
    return System.getProperty("line.separator", "\n");
  }
}
