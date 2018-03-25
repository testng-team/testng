package test.commandline;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandLineOverridesXml extends SimpleBaseTest {

  @Test(description = "Specifying -groups on the command line should override testng.xml")
  public void commandLineGroupsShouldOverrideXml() {
    runTest("go", null, Collections.singletonList("f2"));
  }

  @Test(description = "Specifying -excludegroups on the command line should override testng.xml")
  public void commandLineExcludedGroupsShouldOverrideXml() {
    runTest(null, "go", Collections.singletonList("f1"));
  }

  @Test
  public void shouldRunBothMethods() {
    runTest(null, null, Arrays.asList("f1", "f2"));
  }

  private void runTest(String group, String excludedGroups, List<String> methods) {
    XmlSuite s = createXmlSuite(getClass().getName());
    createXmlTest(s, "Test", OverrideSampleTest.class.getName());
    TestNG tng = create();
    if (group != null) tng.setGroups(group);
    if (excludedGroups != null) tng.setExcludedGroups(excludedGroups);
    tng.setXmlSuites(Collections.singletonList(s));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertTestResultsEqual(tla.getPassedTests(), methods);
  }

  @Test
  public void ensureThatParallelismAndThreadCountAreRallied() {
    TestNG testng = create();
    testng.setTestSuites(Collections.singletonList("src/test/resources/987.xml"));
    testng.setThreadCount(2);
    testng.setParallel(XmlSuite.ParallelMode.METHODS);
    testng.run();
    assertThat(Issue987TestSample.maps).hasSize(2);
    assertThat(Issue987TestSample.maps.values()).contains("method2", "method1");
  }
}
