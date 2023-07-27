package org.testng.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.xml.issue2937.ListenerSetupReporter.isListenerSetupFailureReported;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.github1533.SuiteCounter;
import org.testng.xml.github1874.TestClassSample;
import org.testng.xml.internal.Parser;
import org.testng.xml.issue2866.ATestClassSample;
import org.testng.xml.issue2866.BTestClassSample;
import org.testng.xml.issue2866.ThreadCountingSuiteAlteringListener;
import org.testng.xml.issue2937.ListenerSetupReporter;
import test.SimpleBaseTest;

public class XmlSuiteTest extends SimpleBaseTest {

  @Test
  public void testIncludedAndExcludedGroups() {
    XmlSuite suite = new XmlSuite();
    suite.addIncludedGroup("foo");
    suite.addExcludedGroup("bar");
    assertThat(suite.getIncludedGroups()).containsExactly("foo");
    assertThat(suite.getExcludedGroups()).containsExactly("bar");
  }

  @Test
  public void testIncludedAndExcludedGroupsWithRun() {
    XmlRun xmlRun = new XmlRun();
    xmlRun.onInclude("foo");
    xmlRun.onExclude("bar");
    XmlGroups groups = new XmlGroups();
    groups.setRun(xmlRun);
    XmlSuite suite = new XmlSuite();
    suite.setGroups(groups);
    assertThat(suite.getIncludedGroups()).containsExactly("foo");
    assertThat(suite.getExcludedGroups()).containsExactly("bar");
  }

  @Test(dataProvider = "dp", description = "GITHUB-778")
  public void testTimeOut(String timeout, int size, int lineNumber) {
    XmlSuite suite = new XmlSuite();
    suite.setTimeOut(timeout);
    StringReader stringReader = new StringReader(suite.toXml());
    List<String> resultLines = Lists.newArrayList();
    List<Integer> lineNumbers = grep(stringReader, "time-out=\"1000\"", resultLines);
    assertThat(lineNumbers).size().isEqualTo(size);
    assertThat(resultLines).size().isEqualTo(size);
    if (size > 0) {
      assertThat(lineNumbers.get(size - 1)).isEqualTo(lineNumber);
    }
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
      {"1000", 1, 2},
      {"", 0, 0}
    };
  }

  @Test(description = "GITHUB-1668")
  public void ensureNoExceptionsAreRaisedWhenMethodSelectorsDefinedAtSuiteLevel()
      throws IOException {
    Parser parser = new Parser("src/test/resources/xml/issue1668.xml");
    List<XmlSuite> suites = parser.parseToList();
    XmlSuite xmlsuite = suites.get(0);
    TestNG testNG = create();
    testNG.setXmlSuites(suites);
    testNG.setUseDefaultListeners(false);
    testNG.run();
    // Trigger a call to "toXml()" to ensure that there is no exception raised.
    assertThat(xmlsuite.toXml()).isNotEmpty();
  }

  @Test(description = "GITHUB-435")
  public void ensureSuiteLevelPackageIsAppliedToAllTests() throws IOException {
    Parser parser = new Parser("src/test/resources/xml/issue435.xml");
    List<XmlSuite> suites = parser.parseToList();
    XmlSuite xmlsuite = suites.get(0);
    assertThat(xmlsuite.getTests().get(0).getClasses().size()).isEqualTo(0);
    TestNG testNG = create();
    testNG.setXmlSuites(suites);
    testNG.setUseDefaultListeners(false);
    testNG.run();
    assertThat(xmlsuite.getTests().get(0).getClasses().size()).isEqualTo(1);
  }

  @Test(description = "GITHUB-1674")
  public void ensureSuiteLevelBeanshellIsAppliedToAllTests() throws IOException {
    PrintStream current = System.out;
    StringOutputStream stream = new StringOutputStream();
    try {
      System.setOut(new PrintStream(stream));
      Parser parser = new Parser("src/test/resources/xml/issue1674.xml");
      List<XmlSuite> suites = parser.parseToList();
      XmlSuite xmlsuite = suites.get(0);
      assertThat(xmlsuite.getTests().get(0).getMethodSelectors().size()).isEqualTo(0);
      TestNG testNG = create();
      testNG.setXmlSuites(suites);
      testNG.setUseDefaultListeners(false);
      testNG.run();
      assertThat(xmlsuite.getTests().get(0).getMethodSelectors().size()).isEqualTo(1);
      assertThat(stream.toString()).contains(Arrays.asList("rajni", "kamal", "mgr"));
    } finally {
      System.setOut(current);
    }
  }

  @Test(description = "GITHUB-2866")
  public void ensureSuiteLevelThreadCountsAreInheritedInTestTags() {
    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("Sample_suite");
    xmlSuite.setThreadCount(1);
    xmlSuite.setParallel(ParallelMode.CLASSES);
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setName("Sample_test");
    xmlTest.setClasses(
        Arrays.asList(new XmlClass(ATestClassSample.class), new XmlClass(BTestClassSample.class)));
    xmlTest.setThreadCount(5);
    xmlTest.setParallel(ParallelMode.CLASSES);
    TestNG testng = create(xmlSuite);
    ThreadCountingSuiteAlteringListener listener = new ThreadCountingSuiteAlteringListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getThreadIds("Sample_test_cloned")).hasSize(2);
  }

  @Test(description = "GITHUB-1533")
  public void testScenarioWithChildSuites() {
    String suiteFile = "src/test/resources/xml/github1533/parent.xml";
    runTests(suiteFile, 1, 2, "GitHub1533_Suite", "GitHub1533_Parent_Suite");
  }

  @Test(description = "GITHUB-1533")
  public void testScenarioWithNoChildSuites() {
    String suiteFile = "src/test/resources/xml/github1533/child.xml";
    runTests(suiteFile, 0, 1, "GitHub1533_Suite");
  }

  @Test(description = "GITHUB-1874")
  // We are explicitly not including any assertions here because the intent of this test is to
  // ensure that there are no exceptions triggered related to circular dependency.
  public void testEnsureNoCyclicDependencyTriggered() {
    XmlSuite suite = createXmlSuite("sample_suite");
    XmlTest test = createXmlTest(suite, "sample_test");
    XmlClass entryOne = new XmlClass(TestClassSample.class);
    createXmlInclude(entryOne, "testMethodTwo");
    test.getClasses().add(entryOne);
    XmlClass entryTwo = new XmlClass(TestClassSample.class);
    createXmlInclude(entryTwo, "testMethodOne");
    test.getClasses().add(entryTwo);
    TestNG testng = create(suite);
    testng.run();
  }

  /**
   * This test checks that TestNG can handle duplicate child suites when we have the following set
   * of files:
   *
   * <p>- parent-suite-with-duplicate-child -> [child-suite-1, children/child-suite-3, child-suite1,
   * children/child-suite-3] - children/child-suite-3 -> [../child-suite-2, child-suite-4,
   * morechildren/child-suite-5]
   *
   * <p>SHOULD return a XmlSuite object with following structure:
   *
   * <p>parent-suite-with-duplicate-child ├───child-suite-1 ├───child-suite-3 │ ├───child-suite-2 │
   * ├───child-suite-4 │ └───child-suite-5 ├───child-suite-1(0) └───child-suite-3(0)
   * ├───child-suite-2(0) ├───child-suite-4(0) └───child-suite-5(0)
   *
   * <p>but NOT like:
   *
   * <p>parent-suite-with-duplicate-child ├───child-suite-1 ├───child-suite-3 ├───child-suite-1(0)
   * └───child-suite-3(0) ├───child-suite-2 ├───child-suite-4 ├───child-suite-5 ├───child-suite-2(0)
   * ├───child-suite-4(0) └───child-suite-5(0)
   *
   * <p>Check the <code>checksuitesinitialization</code> folder under test resources
   */
  @Test(description = "GITHUB-1850")
  public void checkDuplicateChildSuites() throws IOException {
    String path =
        getPathToResource("checksuitesinitialization/parent-suite-with-duplicate-child.xml");
    Parser parser = new Parser(path);
    List<XmlSuite> suites = parser.parseToList();
    XmlSuite rootSuite = suites.get(0);
    assertEquals(rootSuite.getChildSuites().size(), 4);

    XmlSuite suite3 = rootSuite.getChildSuites().get(1);
    assertEquals(suite3.getName(), "Child Suite 3");
    assertEquals(suite3.getChildSuites().size(), 3);

    XmlSuite suite3_0 = rootSuite.getChildSuites().get(3);
    assertEquals(suite3.getName(), "Child Suite 3");
    assertEquals(suite3_0.getChildSuites().size(), 3);

    XmlSuite suite5 = suite3.getChildSuites().get(2);
    assertEquals(suite5.getName(), "Child Suite 5");
    assertEquals(suite5.getTests().size(), 1);

    XmlSuite suite5_0 = suite3_0.getChildSuites().get(2);
    assertEquals(suite5_0.getName(), "Child Suite 5");
    assertEquals(suite5_0.getTests().size(), 1);
  }

  @Test()
  public void ensureSuiteLevelTests() throws IOException {
    PrintStream current = System.out;
    try {
      Parser parser = new Parser("src/test/java/org/testng/xml/issue2937/suite.xml");
      List<XmlSuite> suites = parser.parseToList();
      TestNG testNG = new TestNG();
      testNG.setXmlSuites(suites);
//      testNG.addListener(new ListenerSetupReporter());
      testNG.run();
    } finally {

      assertThat(isListenerSetupFailureReported).isEqualTo(true);
    }
  }


  private static void runTests(
      String suiteFile, int childSuitesCount, int suiteCounter, String... suiteNames) {
    List<XmlSuite> suites;
    try {
      suites = new Parser(suiteFile).parseToList();
    } catch (IOException e) {
      throw new TestNGException(e);
    }
    assertEquals(suites.size(), 1);
    assertEquals(suites.get(0).getChildSuites().size(), childSuitesCount);
    TestNG testng = create(suites);
    SuiteCounter listener = new SuiteCounter();
    testng.addListener((ITestNGListener) listener);
    testng.run();
    assertEquals(listener.getCounter(), suiteCounter);
    assertThat(listener.getSuiteNames()).containsExactly(suiteNames);
  }

  static class StringOutputStream extends OutputStream {
    private final StringBuilder string = new StringBuilder();

    @Override
    public void write(int b) {
      this.string.append((char) b);
    }

    // Netbeans IDE automatically overrides this toString()
    public String toString() {
      return this.string.toString();
    }
  }
}
