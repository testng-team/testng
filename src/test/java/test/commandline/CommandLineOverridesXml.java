package test.commandline;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

public class CommandLineOverridesXml extends SimpleBaseTest {

  @Test(description = "Specifying -groups on the command line should override testng.xml")
  public void commandLineGroupsShouldOverrideXml() {
    runTest("go", null, Arrays.asList(new String[] { "f2" }));
  }

  @Test(description = "Specifying -excludegroups on the command line should override testng.xml")
  public void commandLineExcludedGroupsShouldOverrideXml() {
    runTest(null, "go", Arrays.asList(new String[] { "f1" }));
  }

  @Test
  public void shouldRunBothMethods() {
    runTest(null, null, Arrays.asList(new String[] { "f1", "f2" }));
  }

  private void runTest(String group, String excludedGroups, List<String> methods) {
    XmlSuite s = createXmlSuite(getClass().getName());
    XmlTest t = createXmlTest(s, "Test", OverrideSampleTest.class.getName());
    TestNG tng = create();
    if (group != null) tng.setGroups(group);
    if (excludedGroups != null) tng.setExcludedGroups(excludedGroups);
    tng.setXmlSuites(Arrays.asList(new XmlSuite[] { s }));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertTestResultsEqual(tla.getPassedTests(), methods);
  }
}
