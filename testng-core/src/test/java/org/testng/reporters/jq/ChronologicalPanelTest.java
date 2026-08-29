package org.testng.reporters.jq;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.paramhandler.FakeSuite;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.XmlSuite;
import org.xmlunit.assertj.XmlAssert;
import test.SimpleBaseTest;

/**
 * The markup the chronological panel of the HTML report produces.
 *
 * <p>These parse the fragment rather than matching strings against it, since what is at stake is
 * whether the tags balance.
 */
public class ChronologicalPanelTest extends SimpleBaseTest {

  /**
   * Anchored at the root the fragment is wrapped in, so a block nested in another one -- which is
   * what GITHUB-299 produced -- does not match.
   */
  private static final String CHRONOLOGICAL_CLASS_NAMES =
      "/root/div[@class='chronological-class']/div[@class='chronological-class-name']";

  @Test(description = "GITHUB-299")
  public void theOnlyChronologicalClassOfASuiteIsClosed() {
    List<ISuite> suites = runAndCaptureSuites(create(FirstSample.class));

    assertThatFragment(chronologicalContentOf(suites))
        .nodesByXPath(CHRONOLOGICAL_CLASS_NAMES)
        .extractingText()
        .containsExactly(FirstSample.class.getName());
  }

  @Test(description = "GITHUB-299")
  public void everyChronologicalClassOfASuiteIsClosed() {
    List<ISuite> suites = runAndCaptureSuites(create(FirstSample.class, SecondSample.class));

    assertThatFragment(chronologicalContentOf(suites))
        .nodesByXPath(CHRONOLOGICAL_CLASS_NAMES)
        .extractingText()
        .containsExactly(FirstSample.class.getName(), SecondSample.class.getName());
  }

  @Test(description = "GITHUB-299")
  public void theChronologicalPanelsOfSeveralSuitesAreSiblings() {
    XmlSuite first = createXmlSuite("chronological-first", "first", FirstSample.class);
    XmlSuite second = createXmlSuite("chronological-second", "second", SecondSample.class);
    List<ISuite> suites = runAndCaptureSuites(create(first, second));

    XMLStringBuffer xsb = new XMLStringBuffer("");
    new ChronologicalPanel(new Model(suites)).generate(xsb);

    // One panel nested in the other answers a single node here, not two.
    assertThatFragment(xsb.toXML()).nodesByXPath("/root/div[@class='panel']").hasSize(2);
  }

  @Test(description = "GITHUB-299")
  public void aSuiteThatInvokedNothingOpensNoBlock() {
    ISuite suite = new FakeSuite(createXmlTest("chronological-empty", "empty"));

    String content = chronologicalContentOf(Collections.singletonList(suite));

    assertThat(content).isEmpty();
  }

  @Test(description = "GITHUB-299")
  public void theGeneratedIndexHtmlOpensAndClosesAsManyDivs() throws IOException {
    Path outputDirectory = createDirInTempDir("chronological").toPath();
    TestNG tng = create(outputDirectory, FirstSample.class, SecondSample.class);
    tng.setUseDefaultListeners(true);

    tng.run();

    // index.html is not well formed XML -- the header resource it is prepended with carries a
    // <!DOCTYPE html> and an unclosed <meta> -- so the file is counted rather than parsed. Nothing
    // else it holds spells "<div": the only inline script is the one TimesPanel writes.
    String html = Files.readString(outputDirectory.resolve("index.html"));
    assertThat(occurrencesOf(html, "<div")).isEqualTo(occurrencesOf(html, "</div>"));
  }

  private static String chronologicalContentOf(List<ISuite> suites) {
    return new ChronologicalPanel(new Model(suites))
        .getContent(suites.get(0), new XMLStringBuffer(""));
  }

  /** Wraps the fragment in a root element, which is what the XPaths above are anchored at. */
  private static XmlAssert assertThatFragment(String fragment) {
    return XmlAssert.assertThat("<root>" + fragment + "</root>");
  }

  private static int occurrencesOf(String haystack, String needle) {
    int count = 0;
    for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) {
      count++;
    }
    return count;
  }

  private static List<ISuite> runAndCaptureSuites(TestNG tng) {
    SuiteCapture capture = new SuiteCapture();
    tng.addListener((ITestNGListener) capture);
    tng.run();
    return capture.suites;
  }

  /** Holds on to the suites TestNG hands its reporters, which is what {@link Main} is given. */
  private static class SuiteCapture implements IReporter {
    private final List<ISuite> suites = new ArrayList<>();

    @Override
    public void generateReport(
        List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
      this.suites.addAll(suites);
    }
  }

  // The panel orders the invoked methods by the millisecond they started, and SuiteRunner collects
  // them through a HashSet, so two methods starting within the same millisecond can come out in
  // either order and interleave the classes. Sleeping keeps the start times apart.
  public static class FirstSample {
    @Test
    public void alpha() throws InterruptedException {
      Thread.sleep(2);
    }

    @Test
    public void beta() throws InterruptedException {
      Thread.sleep(2);
    }
  }

  public static class SecondSample {
    @Test
    public void gamma() throws InterruptedException {
      Thread.sleep(2);
    }

    @Test
    public void delta() throws InterruptedException {
      Thread.sleep(2);
    }
  }
}
