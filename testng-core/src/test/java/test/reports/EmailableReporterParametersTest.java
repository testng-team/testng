package test.reports;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.reporters.EmailableReporter2;
import org.testng.reporters.TextReporter;
import org.testng.reporters.snapshot.ConfigurationParameterSample;
import org.testng.reporters.snapshot.NoRenderingFactoryParameterSample;
import org.testng.reporters.snapshot.NoRenderingParameterSample;
import org.testng.reporters.snapshot.NonCloneableParameterSample;
import org.testng.reporters.snapshot.ParameterShapesSample;
import org.testng.reporters.snapshot.RenderingCountSample;
import org.testng.reporters.snapshot.WrongArgumentCountSample;
import test.SimpleBaseTest;
import test.reports.issue3418.FactoryOnlySample;

/**
 * What {@code emailable-report.html} says an invocation ran with.
 *
 * <p>The page is written once every invocation of the run is over, so reading the parameters back
 * off the results at that point described the objects as they stood then, not as the invocation
 * found them. These read the file rather than the reporter, since the file is what is mailed.
 *
 * <p>Each run registers nothing but an {@link EmailableReporter2}, so what another built-in report
 * would render cannot be mistaken for what this one does.
 */
public class EmailableReporterParametersTest extends SimpleBaseTest {

  /**
   * One scenario: its {@code class#method} heading, and the table under it. Matched across lines,
   * since a scenario that carries a stack trace has newlines between its rows.
   */
  private static final Pattern SCENARIO =
      Pattern.compile(
          "<h3 id=\"m\\d+\">([^<]*)</h3><table class=\"result\">(.*?)</table>", Pattern.DOTALL);

  /** The header row naming the columns, and the value row under it. */
  private static final Pattern ROW =
      Pattern.compile(
          "<tr class=\"param\">((?:<th>[^<]*</th>)+)</tr>"
              + "<tr class=\"param stripe\">((?:<td>[^<]*</td>)*)</tr>");

  private static final Pattern CELL = Pattern.compile("<t[hd]>([^<]*)</t[hd]>");

  @Test(
      description =
          "GITHUB-447: a mutable parameter no reflective clone can copy is reported with the value"
              + " its own invocation ran with")
  public void mutableNonCloneableParameterKeepsItsInvocationTimeValue() throws IOException {
    // The data provider hands the same object to all three, and each invocation leaves it holding
    // the next one's value. Reading it at report time answered "invocation-4" three times.
    assertThat(parametersOf(NonCloneableParameterSample.class, "report"))
        .containsExactlyInAnyOrder(
            singletonList("invocation-1"),
            singletonList("invocation-2"),
            singletonList("invocation-3"));
  }

  @Test(
      description =
          "A configuration method that failed is reported with what it was handed, not with what"
              + " it left behind")
  public void failedConfigurationKeepsItsInvocationTimeValue() throws IOException {
    String page = runUnderEmailableReporter(ConfigurationParameterSample.class);

    // This report lists the failed and the skipped configurations, and the configuration here
    // mutates the row its test method will run with before failing on purpose.
    assertThat(parametersOf(page, "prepare"))
        .containsExactly(singletonList("[before-configuration]"));
    // The test it skipped is announced with what the configuration left, which is what it ran with
    // as far as it ever ran: unchanged by this migration.
    assertThat(parametersOf(page, "report")).containsExactly(singletonList("mutated"));
  }

  @Test(
      description =
          "The HTML representation of a value is unchanged: no console quoting, the word for an"
              + " absent one, and the parameters in the order the method declares them")
  public void valuesKeepTheirHtmlRepresentationAndOrder() throws IOException {
    // Deliberately not the console rendering: TextReporter prints "text" and "" quoted and this
    // page never has. An array is written by its contents, as it already was.
    assertThat(parametersOf(ParameterShapesSample.class, "report"))
        .containsExactly(asList("text", "", "42", "null", "[1, 2]", "[a, b]", "shape"));
  }

  @Test(
      description =
          "A parameter whose toString() answers null is written as the word, where it used to cost"
              + " the whole page")
  public void aValueWithNoRenderingIsWrittenAsTheWord() throws IOException {
    // Utils.toString(Object) answers null for such an object and Utils.escapeHtml does not accept
    // one, so generateReport threw and TestNG dropped the report: the file was left empty.
    assertThat(parametersOf(NoRenderingParameterSample.class, "report"))
        .containsExactly(singletonList("null"));
  }

  @Test(description = "A factory parameter with nothing to say costs no more than a method one")
  public void aFactoryValueWithNoRenderingIsWrittenAsTheWordToo() throws IOException {
    // Factory parameters have no snapshot and are still read from the result, but what is written
    // for a value that renders as nothing is the same question on either kind of row.
    assertThat(
            rowsOf(
                runUnderEmailableReporter(NoRenderingFactoryParameterSample.class),
                "report",
                "Factory Parameter"))
        .containsExactly(singletonList("null"));
  }

  @Test(
      description =
          "A data provider that supplied the wrong number of values leaves the invocation with"
              + " none, and the page says nothing about them")
  public void aResultThatNeverGotItsValuesReportsNone() throws IOException {
    // The matcher throws while conforming the values, so the invocation is never given any: this
    // wrote no parameter rows before the snapshots and writes none now.
    assertThat(parametersOf(WrongArgumentCountSample.class, "report")).isEmpty();
  }

  @Test(description = "Factory parameters are untouched: no snapshot describes them")
  public void factoryParametersAreStillReadFromTheResult() throws IOException {
    String page = runUnderEmailableReporter(FactoryOnlySample.class);

    assertThat(rowsOf(page, "test", "Factory Parameter")).containsExactly(singletonList("alpha"));
    assertThat(parametersOf(page, "test")).isEmpty();
  }

  @Test(
      description =
          "The page and a console reporter describe the same invocation from the one snapshot, so"
              + " the value is rendered once")
  public void theEmailableReportSharesTheRenderingWithAConsoleReporter() throws IOException {
    File outputDirectory = createDirInTempDir(UUID.randomUUID().toString());
    EmailableReporter2 reporter = new EmailableReporter2();
    TestNG testng = create(outputDirectory.toPath(), RenderingCountSample.class);
    testng.addListener(reporter);
    testng.addListener(new TextReporter("Example_Test", 2));

    int renderedBefore = RenderingCountSample.renderings();
    testng.run();

    assertThat(RenderingCountSample.renderings() - renderedBefore).isEqualTo(1);
    // And the page did describe it, so a count of one cannot be a page that described nothing.
    assertThat(parametersOf(read(outputDirectory, reporter), "report"))
        .containsExactly(singletonList("counted"));
  }

  private static List<List<String>> parametersOf(Class<?> testClass, String methodName)
      throws IOException {
    return parametersOf(runUnderEmailableReporter(testClass), methodName);
  }

  /** The method parameter rows of every scenario of the named method, in the order they appear. */
  private static List<List<String>> parametersOf(String page, String methodName) {
    return rowsOf(page, methodName, "Parameter");
  }

  /**
   * @param page - The generated page.
   * @param methodName - The method whose scenarios are being read.
   * @param prefix - Which of the two kinds of row to keep, {@code Parameter} or {@code Factory
   *     Parameter}. They are told apart by their header, since both are written the same way.
   * @return - One entry per row, holding its values in column order.
   */
  private static List<List<String>> rowsOf(String page, String methodName, String prefix) {
    List<List<String>> rows = new ArrayList<>();
    Matcher scenario = SCENARIO.matcher(page);
    while (scenario.find()) {
      if (!scenario.group(1).endsWith("#" + methodName)) {
        continue;
      }
      Matcher row = ROW.matcher(scenario.group(2));
      while (row.find()) {
        if (cells(row.group(1)).get(0).equals(prefix + " #1")) {
          rows.add(cells(row.group(2)));
        }
      }
    }
    return rows;
  }

  private static List<String> cells(String rowMarkup) {
    List<String> values = new ArrayList<>();
    Matcher cell = CELL.matcher(rowMarkup);
    while (cell.find()) {
      values.add(cell.group(1));
    }
    return values;
  }

  private static String runUnderEmailableReporter(Class<?> testClass) throws IOException {
    File outputDirectory = createDirInTempDir(UUID.randomUUID().toString());
    EmailableReporter2 reporter = new EmailableReporter2();
    TestNG testng = create(outputDirectory.toPath(), testClass);
    testng.addListener(reporter);
    testng.run();
    return read(outputDirectory, reporter);
  }

  private static String read(File outputDirectory, EmailableReporter2 reporter) throws IOException {
    File page = new File(outputDirectory, reporter.getFileName());
    // An empty one is what a NullPointerException inside generateReport used to leave behind, and
    // TestNG prints that to stderr rather than failing the run.
    assertThat(page).exists().isNotEmpty();
    return Files.readString(page.toPath());
  }
}
