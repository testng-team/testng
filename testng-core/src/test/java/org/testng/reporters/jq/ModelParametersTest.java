package org.testng.reporters.jq;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.reporters.ParameterSnapshotReader;
import org.testng.reporters.snapshot.ConfigurationParameterSample;
import org.testng.reporters.snapshot.LongParameterSample;
import org.testng.reporters.snapshot.NonCloneableParameterSample;
import org.testng.reporters.snapshot.ParameterShapesSample;
import org.testng.reporters.snapshot.PassingConfigurationParameterSample;
import org.testng.reporters.snapshot.WrongArgumentCountSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * How the jq report names an invocation, which is the only place its parameters appear -- as the
 * anchor of a method, as the text of a navigator link, and as the whole label in the chronological
 * and reporter panels.
 *
 * <p>These read {@link Model#getTestResultName} rather than the generated page. The values reach
 * the page twice, joined differently and truncated in only one of the two, so a regression in one
 * of the sites could be read off the other; and the formatting -- the separator, the hundred
 * character limit, the ellipsis -- is this method's own contract. That the report is wired to read
 * the snapshots at all is a separate question, and {@code test.reports.JqReportParametersTest}
 * answers it against the page.
 *
 * <p>The names have to be taken while the run is still reportable: the store is released once every
 * reporter has run, so a name built after {@code TestNG#run} returns reads through the fallback and
 * describes the objects as they stand then -- exactly the defect. {@link Namer} is therefore an
 * {@link IReporter}, and declares the reading itself so that these do not depend on {@code Main}.
 */
public class ModelParametersTest extends SimpleBaseTest {

  @Test(
      description =
          "GITHUB-447: a mutable parameter no reflective clone can copy names its own invocation")
  public void mutableNonCloneableParameterKeepsItsInvocationTimeValue() {
    // The data provider hands the same object to all three, and each invocation leaves it holding
    // the next one's value. Naming them at report time answered "invocation-4" three times over,
    // which also made three results share one anchor.
    assertThat(namesOf(NonCloneableParameterSample.class))
        .containsExactlyInAnyOrder(
            "report(invocation-1)", "report(invocation-2)", "report(invocation-3)");
  }

  @Test(
      description =
          "A configuration method that passed is named with what it was handed, which means its"
              + " snapshot outlived the invocation that took it")
  public void passedConfigurationKeepsItsInvocationTimeValue() {
    // ChronologicalPanel names every invoked method, configuration methods included, and it is
    // built once every invocation of the run is over. The configuration is given the row its test
    // method will run with and mutates it, so reading it back there answered what it left behind.
    assertThat(namesOf(PassingConfigurationParameterSample.class))
        .containsExactlyInAnyOrder("prepare([before-configuration])", "report(mutated)");
  }

  @Test(description = "A configuration method that failed keeps its values too")
  public void failedConfigurationKeepsItsInvocationTimeValue() {
    assertThat(namesOf(ConfigurationParameterSample.class))
        .containsExactlyInAnyOrder("prepare([before-configuration])", "report(mutated)");
  }

  @Test(description = "Every value is named in the plain form, with no console decoration")
  public void valuesKeepTheirPlainRenderingAndOrder() {
    // Deliberately not the console rendering: TextReporter prints "text" and "" quoted, which this
    // report has never done. An absent value is the word, since there is nowhere here to say it
    // otherwise, and an array is named by its contents.
    assertThat(namesOf(ParameterShapesSample.class))
        .containsExactly("report(text, , 42, null, [1, 2], [a, b], shape)");
  }

  @Test(description = "A name longer than a hundred characters is cut there and given an ellipsis")
  public void aLongNameIsTruncatedWhereItAlwaysWas() {
    // 60 + ", " + 60 is 122 characters, so the cut falls 38 characters into the second value.
    String truncated =
        (LongParameterSample.FIRST + ", " + LongParameterSample.SECOND).substring(0, 100);

    assertThat(namesOf(LongParameterSample.class))
        .containsExactlyInAnyOrder("report(" + truncated + "...)", "report(short, brief)");
  }

  @Test(
      description =
          "An invocation a data provider supplied the wrong number of values to is named without"
              + " parentheses, as it was before")
  public void aResultThatNeverGotItsValuesIsNamedWithoutThem() {
    // The matcher throws while conforming the values, so the invocation is never given any.
    assertThat(namesOf(WrongArgumentCountSample.class)).containsExactly("report");
  }

  private static List<String> namesOf(Class<?> sample) {
    Namer namer = new Namer();
    TestNG testng = create(sample);
    testng.addListener(namer);
    testng.run();

    // TestNG catches whatever a reporter throws and prints it to stderr, so a Namer that blew up
    // would otherwise look exactly like a run that named nothing.
    assertThat(namer.failure).isNull();
    return namer.names;
  }

  /**
   * Names the results the jq report names, once every context has finished -- which is when it
   * builds its model, and the last moment a snapshot is readable.
   *
   * <p>Taken from the suite rather than from the announcements, because that is where the report
   * takes them and the two are not the same set. An invocation a data provider supplied the wrong
   * number of values to reaches the failed map without ever having been announced as starting.
   *
   * <p>Both of the report's own traversals, so that a name this test never asks for is a name no
   * panel builds either: {@link Model#getAllTestResults(ISuite)}, which is what {@code
   * ReporterPanel} and {@code TimesPanel} iterate, and {@code suite.getAllInvokedMethods()}, which
   * is what {@code ChronologicalPanel} iterates -- configuration methods included, and they are in
   * neither the first list nor the announcements this could have collected instead.
   */
  private static final class Namer implements IReporter, ParameterSnapshotReader {

    final List<String> names = new ArrayList<>();

    /** Whatever went wrong in here, which TestNG would otherwise print to stderr and drop. */
    @Nullable Exception failure;

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      try {
        // Held by identity, as the store is: two invocations of one method with one value must not
        // collapse into a single name here.
        Set<ITestResult> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Model model = new Model(suites);
        for (ISuite suite : suites) {
          name(seen, model.getAllTestResults(suite));
          List<ITestResult> invoked = new ArrayList<>();
          for (IInvokedMethod invokedMethod : suite.getAllInvokedMethods()) {
            invoked.add(invokedMethod.getTestResult());
          }
          name(seen, invoked);
        }
      } catch (Exception naming) {
        failure = naming;
      }
    }

    private void name(Set<ITestResult> seen, Iterable<ITestResult> results) {
      for (ITestResult result : results) {
        if (seen.add(result)) {
          names.add(Model.getTestResultName(result));
        }
      }
    }
  }
}
