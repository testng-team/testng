package org.testng.internal.issue573;

import java.util.Collections;
import org.testng.TestNG;

/**
 * Runs the suite file in the first argument, and ends the Java virtual machine (JVM) with the
 * status of the run. The second argument is the output directory.
 *
 * <p>{@code IssueTest} starts this class in a child JVM. See {@code IssueTest} for the reason.
 */
public class PackageScanLauncher {

  public static void main(String[] args) {
    TestNG testng = new TestNG();
    testng.setUseDefaultListeners(false);
    testng.setOutputDirectory(args[1]);
    testng.setTestSuites(Collections.singletonList(args[0]));
    testng.run();
    System.exit(testng.getStatus());
  }
}
