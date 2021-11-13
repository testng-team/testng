package test.commandline;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.jarfileutils.JarCreator;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.TestHelper;
import test.commandline.issue341.LocalLogAggregator;
import test.commandline.issue341.TestSampleA;
import test.commandline.issue341.TestSampleB;

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
    tng.addListener(tla);
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

  @Test(description = "GITHUB-341")
  public void ensureParallelismIsHonoredWhenOnlyClassesSpecifiedInJar() throws IOException {
    Class<?>[] classes = new Class<?>[] {TestSampleA.class, TestSampleB.class};
    File jarfile = JarCreator.generateJar(classes);
    String[] args =
        new String[] {
          "-parallel",
          "classes",
          "-testjar",
          jarfile.getAbsolutePath(),
          "-listener",
          LocalLogAggregator.class.getCanonicalName()
        };
    TestNG.privateMain(args, null);
    Set<String> logs = LocalLogAggregator.getLogs();
    assertThat(logs).hasSize(2);
  }

  @Test(description = "GITHUB-1810")
  public void ensureNoNullPointerExceptionIsThrown() throws IOException {
    TestNG testng = TestNG.privateMain(new String[] {createTemporarySuiteAndGetItsPath()}, null);
    assertThat(testng.getStatus()).isEqualTo(8);
  }

  private static String createTemporarySuiteAndGetItsPath() throws IOException {
    Path file = Files.createTempFile("testng", ".xml");
    org.testng.reporters.Files.writeFile(
        buildSuiteContentThatRefersToInvalidTestClass(), file.toFile());
    return file.toFile().getAbsolutePath();
  }

  private static String buildSuiteContentThatRefersToInvalidTestClass() {
    return TestHelper.SUITE_XML_HEADER
        + "<suite name=\"1810_Suite\">\n"
        + "    <test name=\"1810_test\">\n"
        + "        <classes>\n"
        + "            <class name=\"com.foo.bar.issue1810.ClassDoesnotExist\">\n"
        + "            </class>\n"
        + "        </classes>\n"
        + "    </test>\n"
        + "</suite>\n";
  }
}
