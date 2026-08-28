package test.reports;

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
import org.testng.reporters.TextReporter;
import org.testng.reporters.jq.Main;
import org.testng.reporters.snapshot.NonCloneableParameterSample;
import org.testng.reporters.snapshot.PassingConfigurationParameterSample;
import org.testng.reporters.snapshot.RenderingCountSample;
import test.SimpleBaseTest;

/**
 * What the default HTML report says an invocation ran with.
 *
 * <p>These read {@code index.html} rather than the model, because what they are about is the wiring
 * rather than the formatting: {@code Main} is an {@link org.testng.IReporter} and is told about a
 * run only once every invocation of it is over, so it has to declare that it reads the snapshots
 * before any suite starts. Nothing but generating the page proves it did. The formatting itself is
 * pinned by {@code org.testng.reporters.jq.ModelParametersTest}.
 *
 * <p>Each run registers {@link Main} and nothing else, so what another built-in report would render
 * cannot be mistaken for what this one does.
 */
public class JqReportParametersTest extends SimpleBaseTest {

  /** The two places a value reaches the page: the method anchor, and the visible span. */
  private static final Pattern ANCHOR = Pattern.compile("<a name=\"([^\"]*)\"");

  private static final Pattern PARAMETERS =
      Pattern.compile("<span class=\"parameters\">\\(([^<]*)\\)</span>");

  private static final Pattern CHRONOLOGICAL =
      Pattern.compile("<span class=\"method-name\">([^<]*)</span>\\s*<span class=\"method-start\"");

  @Test(
      description =
          "GITHUB-447: the page describes an invocation with the values it ran with, not with what"
              + " the objects hold once the run is over")
  public void mutableNonCloneableParameterKeepsItsInvocationTimeValue() throws IOException {
    String page = runUnderJqReporter(NonCloneableParameterSample.class);

    // One data provider row shared by three invocations, each leaving it holding the next one's
    // value. Read at report time it answered "invocation-4" three times -- which also gave the
    // three of them the same anchor.
    assertThat(matches(ANCHOR, page))
        .containsExactlyInAnyOrder(
            "report(invocation-1)", "report(invocation-2)", "report(invocation-3)");
    assertThat(matches(PARAMETERS, page))
        .containsExactlyInAnyOrder("invocation-1", "invocation-2", "invocation-3");
  }

  @Test(
      description =
          "A configuration method that passed is listed with what it was handed, which is what"
              + " declaring the reading on Main buys")
  public void aPassingConfigurationIsListedWithItsInvocationTimeValue() throws IOException {
    String page = runUnderJqReporter(PassingConfigurationParameterSample.class);

    // The chronological panel lists every invoked method, configurations included. A snapshot the
    // live reporters are done with is dropped as a configuration succeeds unless someone says they
    // will read it later, and this page is read later.
    assertThat(matches(CHRONOLOGICAL, page))
        .containsExactlyInAnyOrder("prepare([before-configuration])", "report(mutated)");
  }

  @Test(
      description =
          "The page and a console reporter describe the same invocation from the one snapshot, so"
              + " the value is rendered once")
  public void theJqReportSharesTheRenderingWithAConsoleReporter() throws IOException {
    File outputDirectory = createDirInTempDir(UUID.randomUUID().toString());
    TestNG testng = create(outputDirectory.toPath(), RenderingCountSample.class);
    testng.addListener(new Main());
    testng.addListener(new TextReporter("Example_Test", 2));

    int renderedBefore = RenderingCountSample.renderings();
    testng.run();

    assertThat(RenderingCountSample.renderings() - renderedBefore).isEqualTo(1);
    // And the page did describe it, so a count of one cannot be a page that described nothing.
    assertThat(matches(PARAMETERS, read(outputDirectory))).containsExactly("counted");
  }

  private static String runUnderJqReporter(Class<?> testClass) throws IOException {
    File outputDirectory = createDirInTempDir(UUID.randomUUID().toString());
    TestNG testng = create(outputDirectory.toPath(), testClass);
    testng.addListener(new Main());
    testng.run();
    return read(outputDirectory);
  }

  private static String read(File outputDirectory) throws IOException {
    File page = new File(outputDirectory, "index.html");
    assertThat(page).exists();
    return Files.readString(page.toPath());
  }

  private static List<String> matches(Pattern pattern, String page) {
    List<String> found = new ArrayList<>();
    Matcher matcher = pattern.matcher(page);
    while (matcher.find()) {
      found.add(matcher.group(1));
    }
    return found;
  }
}
