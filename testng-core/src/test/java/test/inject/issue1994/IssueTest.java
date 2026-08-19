package test.inject.issue1994;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1994")
  public void injectedXmlTestIsNotClonedIntoTheRunningSuite() {
    XmlSuite suite = createXmlSuite("issue1994-suite");
    XmlTest xmlTest = createXmlTest(suite, "issue1994-test", XmlTestInjectionSample.class);

    InjectedXmlTestCollector collector = new InjectedXmlTestCollector();
    TestNG tng = create(suite);
    tng.addListener(collector);
    tng.run();

    // XmlTest#clone() builds its copy with new XmlTest(suite), and that constructor registers the
    // copy in the suite. Snapshotting the injected parameter therefore used to append a phantom
    // <test> per invocation that received one.
    assertThat(suite.getTests()).hasSize(1);
    assertThat(suite.getTests().get(0)).isSameAs(xmlTest);

    // isSameAs, not containsExactly: XmlTest overrides equals, so a clone compares equal to its
    // original and equality-based assertions would not discriminate.
    assertThat(collector.getReported())
        .hasSize(3)
        .allSatisfy(reported -> assertThat(reported).isSameAs(xmlTest));
  }
}
