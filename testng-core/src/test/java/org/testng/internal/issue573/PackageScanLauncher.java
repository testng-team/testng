package org.testng.internal.issue573;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;

/**
 * Runs the suite file in the first argument, and ends the Java virtual machine (JVM) with the
 * status of the run. The second argument is the output directory. The third argument is the file
 * that receives the results, one line for each result in the form {@code <class>.<method>
 * <status>}, in sorted order.
 *
 * <p>{@code IssueTest} starts this class in a child JVM. See {@code IssueTest} for the reason.
 */
public class PackageScanLauncher implements ITestListener {

  private final Collection<String> results = new ConcurrentLinkedQueue<>();

  public static void main(String[] args) throws IOException {
    PackageScanLauncher launcher = new PackageScanLauncher();
    TestNG testng = new TestNG();
    testng.setUseDefaultListeners(false);
    testng.setOutputDirectory(args[1]);
    testng.setTestSuites(Collections.singletonList(args[0]));
    testng.addListener(launcher);
    testng.run();
    List<String> lines = new ArrayList<>(launcher.results);
    Collections.sort(lines);
    Files.write(Paths.get(args[2]), lines, StandardCharsets.UTF_8);
    System.exit(testng.getStatus());
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    record(result, "PASSED");
  }

  @Override
  public void onTestFailure(ITestResult result) {
    record(result, "FAILED");
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    record(result, "SKIPPED");
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    record(result, "FAILED");
  }

  private void record(ITestResult result, String status) {
    results.add(result.getMethod().getQualifiedName() + " " + status);
  }
}
