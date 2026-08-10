package test.groups.issue2232;

import java.util.Collections;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlRun;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The GITHUB-2232 suite, shared by the in-process test in {@code testng-core} and the forked one in
 * {@code testng-jcommander}. The scanned package is spelled out rather than derived from the
 * caller, so the two halves cannot drift apart by moving.
 */
public final class Issue2232Suites {

  private static final String SAMPLES_PACKAGE = "test.groups.issue2232.samples.*";

  private Issue2232Suites() {}

  public static XmlSuite construct() {
    XmlSuite xmlsuite = new XmlSuite();
    xmlsuite.setName("2232_suite");
    xmlsuite.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    xmlsuite.setThreadCount(256);
    xmlsuite.setParallel(XmlSuite.ParallelMode.CLASSES);

    XmlTest xmltest = new XmlTest(xmlsuite);
    xmltest.setName("2232_test");

    XmlRun xmlrun = new XmlRun();
    xmlrun.onInclude("Group2");
    xmlrun.onExclude("Broken");
    XmlGroups xmlgroup = new XmlGroups();
    xmlgroup.setRun(xmlrun);
    xmltest.setGroups(xmlgroup);

    XmlPackage xmlpackage = new XmlPackage();
    xmlpackage.setName(SAMPLES_PACKAGE);
    xmltest.setPackages(Collections.singletonList(xmlpackage));
    return xmlsuite;
  }
}
