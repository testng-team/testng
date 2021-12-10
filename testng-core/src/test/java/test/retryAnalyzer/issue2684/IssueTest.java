package test.retryAnalyzer.issue2684;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlRun;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class IssueTest {

  @Test
  public void testRetriedTestsWithGroupConfigs() {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("groups_suite");
    xmlSuite.setVerbose(2);
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setName("groups_test");
    XmlGroups xmlGroups = new XmlGroups();
    XmlRun xmlRun = new XmlRun();
    xmlRun.onInclude("p1");
    xmlGroups.setRun(xmlRun);
    xmlTest.setGroups(xmlGroups);
    xmlTest.setClasses(
        Collections.singletonList(new XmlClass(SampleTestClassWithGroupConfigs.class)));
    TestNG testng = new TestNG();
    testng.setXmlSuites(Collections.singletonList(xmlSuite));
    testng.run();
    List<String> actual = SampleTestClassWithGroupConfigs.logs;
    List<String> expected =
        Arrays.asList("beforeGroups", "testMethod", "testMethod", "afterGroups");

    assertEquals(actual, expected, actual + " didn't match elements in expected list" + expected);
  }
}
