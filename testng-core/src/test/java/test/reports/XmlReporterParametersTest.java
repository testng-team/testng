package test.reports;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.PerSuiteXMLReporter;
import org.testng.reporters.RuntimeBehavior;
import org.testng.reporters.TextReporter;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.snapshot.CountedConfigurationParameterSample;
import org.testng.reporters.snapshot.NonCloneableParameterSample;
import org.testng.reporters.snapshot.ParallelParameterSample;
import org.testng.reporters.snapshot.ParameterShapesSample;
import org.testng.reporters.snapshot.PassingConfigurationParameterSample;
import org.testng.reporters.snapshot.RenderingCountSample;
import org.testng.reporters.snapshot.WrongArgumentCountSample;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import test.SimpleBaseTest;

/**
 * What {@code testng-results.xml} says an invocation ran with.
 *
 * <p>The report is written once every invocation of the run is over, so reading the parameters back
 * off the results at that point describes the objects as they stand then, not as the invocation
 * found them. These read the file rather than the reporter, since the file is the contract: it is
 * what a CI server parses.
 */
public class XmlReporterParametersTest extends SimpleBaseTest {

  /** How a {@code <value is-null="true"/>} is spelled here, to tell it from the string "null". */
  private static final String IS_NULL = "<is-null/>";

  private static final String SUITE = "xml-parameters";

  @Test(
      description =
          "GITHUB-447: a mutable parameter no reflective clone can copy is reported with the value"
              + " its own invocation ran with")
  public void mutableNonCloneableParameterKeepsItsInvocationTimeValue() {
    List<List<String>> reported =
        parametersOf(runUnderXmlReporter(NonCloneableParameterSample.class), "report");

    // The data provider hands the same object to all three, and each invocation leaves it holding
    // the next one's value. Reading it at report time would answer "invocation-4" three times.
    assertThat(reported)
        .containsExactlyInAnyOrder(
            singletonList("invocation-1"),
            singletonList("invocation-2"),
            singletonList("invocation-3"));
  }

  @Test(
      description =
          "GITHUB-447: the historical Cloneable case reports the same values it always has, now"
              + " because of the snapshot rather than because the type happens to be cloneable")
  public void mutableCloneableParameterKeepsItsInvocationTimeValue() {
    List<List<String>> reported = parametersOf(runUnderXmlReporter(GitHub447Sample.class), "add");

    assertThat(reported)
        .containsExactlyInAnyOrder(
            asList("[]", IS_NULL, "[null]"),
            asList("[null]", "dup", "[null, dup]"),
            asList("[null, dup]", "dup", "[null, dup, dup]"),
            asList("[null, dup, dup]", "str", "[null, dup, dup, str]"),
            asList("[null, dup, dup, str]", IS_NULL, "[null, dup, dup, str, null]"));
  }

  @Test(description = "Overlapping invocations each report their own value")
  public void parallelInvocationsKeepTheirOwnValue() {
    List<List<String>> reported =
        parametersOf(runUnderXmlReporter(ParallelParameterSample.class), "report");

    // Every row mutates its own object to "mutated", but only once all of them are in flight, so a
    // report that read the objects late would say "mutated" for every one of them.
    assertThat(reported)
        .containsExactlyInAnyOrder(
            singletonList("row-0"),
            singletonList("row-1"),
            singletonList("row-2"),
            singletonList("row-3"));
  }

  @Test(
      description =
          "A configuration method that passed is reported with what it was announced with, which"
              + " means its snapshot outlived the invocation that took it")
  public void passedConfigurationParametersKeepTheirInvocationTimeValue() {
    List<List<String>> reported =
        parametersOf(runUnderXmlReporter(PassingConfigurationParameterSample.class), "prepare");

    // The configuration is handed the row its test method will run with, and mutates it. Nothing
    // lists a passing configuration until this file does, so reading it back here would answer
    // what the method left behind rather than what it was given.
    assertThat(reported).containsExactly(singletonList("[before-configuration]"));
  }

  @Test(
      description =
          "The XML serialization of a value is unchanged: no console quoting, an attribute for a"
              + " null, and the parameters in the order the method declares them")
  public void valuesKeepTheirXmlRepresentationAndOrder() {
    List<List<String>> reported =
        parametersOf(runUnderXmlReporter(ParameterShapesSample.class), "report");

    // Deliberately not the console rendering: TextReporter prints "text" and "" quoted and writes
    // the word null, none of which this file has ever contained.
    assertThat(reported)
        .containsExactly(asList("text", "", "42", IS_NULL, "[1, 2]", "[a, b]", "shape"));
  }

  @Test(
      description =
          "The XML report and a console reporter describe the same invocation from the one"
              + " snapshot, so the value is rendered once")
  public void theXmlReportSharesTheRenderingWithAConsoleReporter() {
    File outputDirectory = createDirInTempDir(UUID.randomUUID().toString());
    TestNG testng = create(outputDirectory.toPath(), RenderingCountSample.class);
    testng.addListener(new XMLReporter());
    testng.addListener(new TextReporter("Example_Test", 2));

    int renderedBefore = RenderingCountSample.renderings();
    testng.run();

    assertThat(RenderingCountSample.renderings() - renderedBefore).isEqualTo(1);
    assertThat(parametersOf(parse(new File(outputDirectory, RuntimeBehavior.FILE_NAME)), "report"))
        .containsExactly(singletonList("counted"));
  }

  @Test(
      description =
          "A passing configuration is described from the snapshot that was taken for it, so its"
              + " value is rendered once rather than captured, dropped and rendered again")
  public void aPassingConfigurationIsRenderedOnce() {
    int renderedBefore = CountedConfigurationParameterSample.renderings();

    Document report = runUnderXmlReporter(CountedConfigurationParameterSample.class);

    // Two renderings, one per invocation that was handed the value: the configuration, which is
    // given the whole row, and the test method itself -- as the two assertions below account for.
    // Dropping the configuration's snapshot before this report ran would make it three, which is
    // the capture-then-discard-then-fallback pair this measures the absence of.
    assertThat(CountedConfigurationParameterSample.renderings() - renderedBefore).isEqualTo(2);
    assertThat(parametersOf(report, "prepare")).containsExactly(singletonList("[counted]"));
    assertThat(parametersOf(report, "report")).containsExactly(singletonList("counted"));
  }

  @Test(
      description =
          "The per-suite variant reports the same values, which is what declaring the reading on"
              + " the abstraction they share buys")
  public void thePerSuiteReporterKeepsTheInvocationTimeValuesToo() {
    File outputDirectory = createDirInTempDir(UUID.randomUUID().toString());
    TestNG testng = createTests(outputDirectory.toPath(), SUITE, NonCloneableParameterSample.class);
    testng.addListener(new PerSuiteXMLReporter());
    testng.run();

    File report = new File(new File(outputDirectory, SUITE), RuntimeBehavior.FILE_NAME);
    assertThat(report).exists();
    assertThat(parametersOf(parse(report), "report"))
        .containsExactlyInAnyOrder(
            singletonList("invocation-1"),
            singletonList("invocation-2"),
            singletonList("invocation-3"));
  }

  @Test(
      description =
          "A data provider that supplied the wrong number of values leaves the invocation with"
              + " none, and the report says nothing about them")
  public void aResultThatNeverGotItsValuesReportsNone() {
    // The matcher throws while conforming the values, so the invocation is never given any: this
    // wrote no <params> before the snapshots and writes none now.
    assertThat(parametersOf(runUnderXmlReporter(WrongArgumentCountSample.class), "report"))
        .containsExactly(emptyList());
  }

  /**
   * Runs the class under nothing but an {@link XMLReporter}, so that what a non-migrated built-in
   * report would render cannot be mistaken for what this one does.
   */
  private static Document runUnderXmlReporter(Class<?> testClass) {
    File outputDirectory = createDirInTempDir(UUID.randomUUID().toString());
    TestNG testng = create(outputDirectory.toPath(), testClass);
    testng.addListener(new XMLReporter());
    testng.run();
    return parse(new File(outputDirectory, RuntimeBehavior.FILE_NAME));
  }

  private static Document parse(File report) {
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(report);
    } catch (Exception parsing) {
      throw new AssertionError("Could not read " + report, parsing);
    }
  }

  /**
   * @param report - A parsed {@code testng-results.xml}.
   * @param methodName - The method whose invocations are being read.
   * @return - One entry per invocation of that method, holding its values in the order the {@code
   *     <param index>}es give them, and empty for an invocation the report lists no values for.
   */
  private static List<List<String>> parametersOf(Document report, String methodName) {
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      NodeList methods =
          (NodeList)
              xPath
                  .compile("//test-method[@name='" + methodName + "']")
                  .evaluate(report, XPathConstants.NODESET);
      XPathExpression reportedValues = xPath.compile("params/param/value");
      List<List<String>> invocations = new ArrayList<>();
      for (int i = 0; i < methods.getLength(); i++) {
        NodeList values =
            (NodeList) reportedValues.evaluate(methods.item(i), XPathConstants.NODESET);
        List<String> reported = new ArrayList<>();
        for (int j = 0; j < values.getLength(); j++) {
          Node value = values.item(j);
          Node isNull = value.getAttributes().getNamedItem("is-null");
          reported.add(isNull == null ? value.getTextContent().trim() : IS_NULL);
        }
        invocations.add(reported);
      }
      return invocations;
    } catch (Exception reading) {
      throw new AssertionError("Could not read the reported parameters of " + methodName, reading);
    }
  }
}
