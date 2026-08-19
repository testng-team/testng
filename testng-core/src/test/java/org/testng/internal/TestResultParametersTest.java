package org.testng.internal;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The parameter representation {@code ITestResult} hands to reporters. Everything here goes through
 * the public {@code ITestResult} methods on purpose: the rules are a compatibility contract, not a
 * property of whichever internal helper happens to implement them.
 */
public class TestResultParametersTest {

  @Test(description = "GITHUB-1994")
  public void injectedXmlTestIsKeptByReference() {
    XmlSuite suite = new XmlSuite();
    // The constructor registers the test in the suite, so the suite starts with exactly one.
    XmlTest xmlTest = new XmlTest(suite);

    TestResult result = TestResult.newEmptyTestResult();
    result.setParameters(new Object[] {xmlTest});

    assertThat(result.getParameters()[0]).isSameAs(xmlTest);
    assertThat(suite.getTests()).hasSize(1);
  }

  @Test(description = "GITHUB-447")
  public void cloneableParameterIsSnapshotted() {
    List<String> live = new ArrayList<>();
    live.add("first");

    TestResult result = TestResult.newEmptyTestResult();
    result.setParameters(new Object[] {live});
    live.add("second");

    assertThat(result.getParameters()[0]).isNotSameAs(live).isEqualTo(singletonList("first"));
  }

  @Test
  public void nonCloneableParameterIsKeptByReference() {
    Object parameter = new Object();

    TestResult result = TestResult.newEmptyTestResult();
    result.setParameters(new Object[] {parameter});

    assertThat(result.getParameters()[0]).isSameAs(parameter);
  }

  @Test
  public void nullParameterSurvives() {
    TestResult result = TestResult.newEmptyTestResult();
    result.setParameters(new Object[] {null});

    assertThat(result.getParameters()).containsExactly((Object) null);
  }
}
