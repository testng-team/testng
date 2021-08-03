package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import org.testng.xml.SuiteXmlParser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import test.SimpleBaseTest;
import test.reports.issue2611.TestClassFailsAtBeforeGroupsWithBeforeGroupsSuiteTestSample;
import test.reports.issue2611.TestClassFailsAtBeforeSuiteWithBeforeGroupsSuiteTestSample;
import test.reports.issue2611.TestClassFailsAtBeforeTestWithBeforeGroupsSuiteTestSample;
import test.reports.issue2611.TestClassWithBeforeGroupsSample;
import test.reports.issue2611.TestClassWithBeforeSuiteSample;
import test.reports.issue2611.TestClassWithBeforeTestSample;
import test.reports.issue2611.TestClassWithJustTestMethodsSample;

public class FailedReporterTest extends SimpleBaseTest {

  @Test
  public void failedFile() throws IOException {
    XmlSuite xmlSuite = createXmlSuite("Suite");
    xmlSuite.getParameters().put("n", "42");

    XmlTest xmlTest = createXmlTest(xmlSuite, "Test");
    xmlTest.addParameter("o", "43");

    XmlClass xmlClass = createXmlClass(xmlTest, SimpleFailedSample.class);
    xmlClass.getLocalParameters().put("p", "44");

    TestNG tng = create(xmlSuite);

    Path temp = Files.createTempDirectory("tmp");
    tng.setOutputDirectory(temp.toAbsolutePath().toString());
    tng.addListener(new FailedReporter());
    tng.run();

    Collection<XmlSuite> failedSuites =
        new Parser(temp.resolve(FailedReporter.TESTNG_FAILED_XML).toAbsolutePath().toString())
            .parse();
    XmlSuite failedSuite = failedSuites.iterator().next();
    Assert.assertEquals("42", failedSuite.getParameter("n"));

    XmlTest failedTest = failedSuite.getTests().get(0);
    Assert.assertEquals("43", failedTest.getParameter("o"));

    XmlClass failedClass = failedTest.getClasses().get(0);
    Assert.assertEquals("44", failedClass.getAllParameters().get("p"));
  }

  @Test(description = "ISSUE-2445")
  public void testParameterPreservationWithFactory() throws IOException {
    final SuiteXmlParser parser = new SuiteXmlParser();
    final String testSuite = "src/test/resources/xml/github2445/test-suite.xml";
    final String expectedResult = "src/test/resources/xml/github2445/expected-failed-report.xml";
    final XmlSuite xmlSuite = parser.parse(testSuite, new FileInputStream(testSuite), true);
    final TestNG tng = create(xmlSuite);

    final Path temp = Files.createTempDirectory("tmp");
    tng.setOutputDirectory(temp.toAbsolutePath().toString());
    tng.addListener(new FailedReporter());
    tng.run();

    final Diff myDiff =
        DiffBuilder.compare(Input.fromFile(expectedResult))
            .withTest(
                Input.fromFile(
                    temp.resolve(FailedReporter.TESTNG_FAILED_XML).toAbsolutePath().toString()))
            .checkForSimilar()
            .ignoreWhitespace()
            .build();

    assertThat(myDiff).matches((it) -> !it.hasDifferences(), "!it.hasDifferences()");
  }

  @Test(dataProvider = "getTestData")
  public void testToEnsureConfigFailuresAreIncluded(ClassMethodInfo pairA, ClassMethodInfo pairB)
      throws IOException {
    XmlSuite xmlSuite = createXmlSuite("kungfu-panda-suite");
    XmlTest xmlTest =
        createXmlTest(xmlSuite, "kungfu-panda-test", pairA.getTestClass(), pairB.getTestClass());
    xmlTest.addIncludedGroup("dragon-warrior");
    TestNG tng = create(xmlSuite);
    final Path temp = Files.createTempDirectory("tmp");
    tng.setOutputDirectory(temp.toAbsolutePath().toString());
    tng.addListener(new FailedReporter());
    tng.run();
    Collection<XmlSuite> failedSuites =
        new Parser(temp.resolve(FailedReporter.TESTNG_FAILED_XML).toAbsolutePath().toString())
            .parse();
    XmlSuite failedSuite = failedSuites.iterator().next();
    assertThat(failedSuite.getName())
        .withFailMessage("The failed suite should have had the prefix of [Failed suite]")
        .isEqualTo("Failed suite [kungfu-panda-suite]");
    XmlTest failedTest = failedSuite.getTests().iterator().next();
    assertThat(failedTest.getName())
        .withFailMessage("The failed test should have had the suffix of [(failed)]")
        .isEqualTo("kungfu-panda-test(failed)");
    runIncludedMethodsAssertion(
        failedTest.getClasses(), pairA.getTestClass(), pairA.getTestMethods());
    runIncludedMethodsAssertion(
        failedTest.getClasses(), pairB.getTestClass(), pairB.getTestMethods());
  }

  @DataProvider(name = "getTestData")
  public Object[][] getTestData() {
    return new Object[][] {
      {
        new ClassMethodInfo(TestClassWithBeforeTestSample.class, "beforeTest", "afterTest"),
        new ClassMethodInfo(TestClassWithJustTestMethodsSample.class, "test2Method")
      },
      {
        new ClassMethodInfo(TestClassWithBeforeSuiteSample.class, "beforeSuite", "afterSuite"),
        new ClassMethodInfo(TestClassWithJustTestMethodsSample.class, "test2Method")
      },
      {
        new ClassMethodInfo(TestClassWithBeforeGroupsSample.class, "beforeGroups", "afterGroups"),
        new ClassMethodInfo(TestClassWithJustTestMethodsSample.class, "test2Method")
      },
      {
        new ClassMethodInfo(
            TestClassFailsAtBeforeGroupsWithBeforeGroupsSuiteTestSample.class,
            "beforeGroups",
            "afterGroups",
            "beforeTest",
            "afterTest",
            "beforeSuite",
            "afterSuite"),
        new ClassMethodInfo(TestClassWithJustTestMethodsSample.class, "test2Method")
      },
      {
        new ClassMethodInfo(
            TestClassFailsAtBeforeSuiteWithBeforeGroupsSuiteTestSample.class,
            "beforeGroups",
            "afterGroups",
            "beforeTest",
            "afterTest",
            "beforeSuite",
            "afterSuite"),
        new ClassMethodInfo(TestClassWithJustTestMethodsSample.class, "test2Method")
      },
      {
        new ClassMethodInfo(
            TestClassFailsAtBeforeTestWithBeforeGroupsSuiteTestSample.class,
            "beforeGroups",
            "afterGroups",
            "beforeTest",
            "afterTest",
            "beforeSuite",
            "afterSuite"),
        new ClassMethodInfo(TestClassWithJustTestMethodsSample.class, "test2Method")
      }
    };
  }

  private static void runIncludedMethodsAssertion(
      List<XmlClass> failedClasses, Class<?> cls, String... methods) {
    XmlClass xmlClass =
        failedClasses.stream()
            .filter(each -> each.getName().equals(cls.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to locate " + cls.getName()));
    List<String> includedMethods =
        xmlClass.getIncludedMethods().stream()
            .map(XmlInclude::getName)
            .collect(Collectors.toList());
    assertThat(includedMethods)
        .withFailMessage(
            String.format(
                "Included methods:\n%s\n in class\n%s\n should match\n%s",
                String.join("\n", includedMethods), cls.getName(), String.join("\n", methods)))
        .containsExactlyInAnyOrder(methods);
  }

  public static class ClassMethodInfo {
    private final Class<?> clazz;
    private final String[] testMethods;

    public ClassMethodInfo(Class<?> clazz, String... testMethods) {
      this.clazz = clazz;
      this.testMethods = testMethods;
    }

    public Class<?> getTestClass() {
      return clazz;
    }

    public String[] getTestMethods() {
      return testMethods;
    }

    @Override
    public String toString() {
      return String.format("%s->%s", clazz.toString(), Arrays.toString(testMethods));
    }
  }
}
