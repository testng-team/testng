package org.testng.concurrency.samples.issue3577;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

/**
 * Runs a {@code <test>} named {@value #TEST_NAME} with its methods in parallel, at a verbose level
 * of 2.
 *
 * <p>{@code IssueTest} starts this class in a child Java virtual machine (JVM). The child JVM
 * enables the INFO level of the logger of {@code Utils}. See {@code IssueTest} for the reason.
 */
public class ParallelLogLauncher {

  public static final String TEST_NAME = "Parallel";

  public static void main(String[] args) {
    XmlSuite suite = new XmlSuite();
    suite.setName("S");
    suite.setParallel(ParallelMode.METHODS);
    suite.setThreadCount(2);
    XmlTest test = new XmlTest(suite);
    test.setName(TEST_NAME);
    test.setXmlClasses(Collections.singletonList(new XmlClass(ParallelLogSample.class.getName())));

    TestNG testng = new TestNG();
    testng.setUseDefaultListeners(false);
    testng.setVerbose(2);
    testng.setOutputDirectory(args[0]);
    testng.setXmlSuites(Collections.singletonList(suite));
    testng.run();
  }
}
