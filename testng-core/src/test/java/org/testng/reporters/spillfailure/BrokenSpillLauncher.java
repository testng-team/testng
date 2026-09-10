package org.testng.reporters.spillfailure;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Runs a suite whose reports are large enough to spill, with every default listener on.
 *
 * <p>Started as a child JVM whose {@code java.io.tmpdir} does not exist, so no buffer can create
 * the file it spills to. See {@code BrokenSpillTest} for what that is meant to prove.
 */
public class BrokenSpillLauncher {

  public static final String MARKER = "RUN COMPLETED";

  public static void main(String[] args) {
    XmlSuite suite = new XmlSuite();
    suite.setName("broken-spill");
    XmlTest test = new XmlTest(suite);
    test.setName("broken-spill-test");
    test.setXmlClasses(
        Collections.singletonList(
            new XmlClass("org.testng.reporters.issue1259.LargeReportSample")));

    TestNG testng = new TestNG();
    testng.setUseDefaultListeners(true);
    testng.setOutputDirectory(args[0]);
    testng.setXmlSuites(Collections.singletonList(suite));
    testng.run();
    System.out.println(MARKER);
  }
}
