package test.groupinvocation;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Joiner;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

/** Test that <suite> tags can have g. */
public class GroupSuiteTest extends SimpleBaseTest {

  private static final File PARENT = new File(getPathToResource("groupinvocation"));

  @DataProvider
  private static Object[][] dp() {
    return new Object[][] {
      {new String[] {"suiteA.xml", "suiteB.xml"}, /* Group in xml */ true},
      {new String[] {"suiteA.xml", "suiteB.xml"}, /* Group in TestNG */ false},
      {new String[] {"parent-suiteA.xml", "parent-suiteB.xml"}, /* Group in xml */ true},
      {new String[] {"parent-suiteA.xml", "parent-suiteB.xml"}, /* Group in TestNG */ false},
      {new String[0], /* Group in xml */ true},
      {new String[0], /* Group in TestNG */ false}
    };
  }

  @Test(dataProvider = "dp")
  public void includeFromSuite0(String[] withSuiteFiles, boolean excludeWithXml) {
    runWithSuite(withSuiteFiles, excludeWithXml, "a", "b", "c", "a2", "b2", "c2");
  }

  @Test(dataProvider = "dp")
  public void includeFromSuite1(String[] withSuiteFiles, boolean excludeWithXml) {
    runWithSuite(withSuiteFiles, excludeWithXml, g("a"), g(), "a", "a2");
  }

  @Test(dataProvider = "dp")
  public void includeFromSuite2(String[] withSuiteFiles, boolean excludeWithXml) {
    runWithSuite(withSuiteFiles, excludeWithXml, g("a", "b"), g(), "a", "b", "a2", "b2");
  }

  @Test(dataProvider = "dp")
  public void excludeFromSuite1(String[] withSuiteFiles, boolean excludeWithXml) {
    runWithSuite(withSuiteFiles, excludeWithXml, g(), g("a"), "b", "c", "b2", "c2");
  }

  @Test(dataProvider = "dp")
  public void excludeFromSuite2(String[] withSuiteFiles, boolean excludeWithXml) {
    runWithSuite(withSuiteFiles, excludeWithXml, g(), g("a", "b"), "c", "c2");
  }

  @Test(description = "Include in both suite and test")
  public void includeTestAndSuite1Xml() {
    runWithSuite(g("a"), g(), g("b"), g(), true, "a", "b", "a2", "b2");
  }

  @Test(description = "Include in both suite and test")
  public void includeTestAndSuite1Cli() {
    runWithSuite(g("a"), g(), g("b"), g(), false, "a", "b", "a2", "b2");
  }

  @Test(description = "Include in suite, exclude in test")
  public void excludeTestAndSuite2Xml() {
    runWithSuite(g("a", "b"), g(), g(), g("a"), true, "b", "b2");
  }

  @Test(description = "Include in suite, exclude in test")
  public void excludeTestAndSuite2Cli() {
    runWithSuite(g("a", "b"), g(), g(), g("a"), false, "b", "b2");
  }

  private void runWithSuite(String[] withSuiteFiles, boolean excludeWithXml, String... methods) {
    runWithSuite(withSuiteFiles, excludeWithXml, g(), g(), g(), g(), methods);
  }

  private void runWithSuite(
      String[] withSuiteFiles,
      boolean excludeWithXml,
      List<String> suiteGroups,
      List<String> excludedSuiteGroups,
      String... methods) {
    runWithSuite(
        withSuiteFiles, excludeWithXml, suiteGroups, excludedSuiteGroups, g(), g(), methods);
  }

  private void runWithSuite(
      List<String> suiteGroups,
      List<String> excludedSuiteGroups,
      List<String> testGroups,
      List<String> excludedTestGroups,
      boolean groupsWithXml,
      String... methods) {
    runWithSuite(
        new String[0],
        groupsWithXml,
        suiteGroups,
        excludedSuiteGroups,
        testGroups,
        excludedTestGroups,
        methods);
  }

  private void runWithSuite(
      String[] withSuiteFiles,
      boolean groupsWithXml,
      List<String> suiteGroups,
      List<String> excludedSuiteGroups,
      List<String> testGroups,
      List<String> excludedTestGroups,
      String... methods) {
    TestNG tng = create();

    XmlSuite suite = createXmlSuite("Groups");
    if (groupsWithXml) {
      suite.setIncludedGroups(suiteGroups);
      suite.setExcludedGroups(excludedSuiteGroups);
    } else {
      tng.setGroups(Joiner.on(',').join(suiteGroups));
      tng.setExcludedGroups(Joiner.on(',').join(excludedSuiteGroups));
    }
    if (withSuiteFiles.length != 0) {
      List<String> suiteFiles = new ArrayList<>(withSuiteFiles.length);
      for (String suiteFile : withSuiteFiles) {
        suiteFiles.add(new File(PARENT, suiteFile).getAbsolutePath());
      }
      suite.setSuiteFiles(suiteFiles);
      createXmlTest(suite, "Groups-test");
    } else {
      XmlTest test =
          createXmlTest(
              suite, "Groups-test", GroupSuiteSampleTest.class, GroupSuiteSampleTest2.class);
      test.setIncludedGroups(testGroups);
      test.setExcludedGroups(excludedTestGroups);
    }

    tng.setXmlSuites(Collections.singletonList(suite));

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getInvokedMethodNames()).containsExactly(methods);
  }

  private static List<String> g(String... groups) {
    return Arrays.asList(groups);
  }
}
