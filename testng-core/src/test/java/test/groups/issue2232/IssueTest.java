package test.groups.issue2232;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlRun;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
  @Test(description = "GITHUB-2232", invocationCount = 2)
  // This test case doesn't vet out the fix completely because the bug by itself is very
  // sporadic and is not easy to reproduce. That is why this test is being executed 10 times
  // to ensure that the issue can be reproduced in one of the executions
  public void ensureNoNPEThrownWhenRunningGroups() throws InterruptedException {
    TestNG testng = create(constructSuite());
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
  }

  private XmlSuite constructSuite() {
    XmlSuite xmlsuite = createXmlSuite("2232_suite");
    xmlsuite.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
    xmlsuite.setThreadCount(256);
    xmlsuite.setParallel(XmlSuite.ParallelMode.CLASSES);
    XmlTest xmltest = createXmlTest(xmlsuite, "2232_test");
    XmlRun xmlrun = new XmlRun();
    xmlrun.onInclude("Group2");
    xmlrun.onExclude("Broken");
    XmlGroups xmlgroup = new XmlGroups();
    xmlgroup.setRun(xmlrun);
    xmltest.setGroups(xmlgroup);
    XmlPackage xmlpackage = new XmlPackage();
    xmlpackage.setName(getClass().getPackage().getName() + ".samples.*");
    xmltest.setPackages(Collections.singletonList(xmlpackage));
    return xmlsuite;
  }
}
