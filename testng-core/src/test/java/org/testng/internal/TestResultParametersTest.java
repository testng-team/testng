package org.testng.internal;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The parameter representation {@code ITestResult} hands to reporters. Exercised through {@code
 * setParameters}/{@code getParameters} rather than through the helper that implements them, so the
 * rules stay pinned wherever they end up living.
 */
public class TestResultParametersTest {

  @Test(description = "GITHUB-1994")
  public void injectedXmlTestIsKeptByReference() {
    XmlSuite suite = new XmlSuite();
    // The constructor registers the test in the suite, so the suite starts with exactly one.
    XmlTest xmlTest = new XmlTest(suite);

    TestResult result = TestResult.newTestResult(methodStub(), new Object[0], 0);
    result.setParameters(new Object[] {xmlTest});

    assertThat(result.getParameters()[0]).isSameAs(xmlTest);
    assertThat(suite.getTests()).hasSize(1);
  }

  @Test(description = "GITHUB-447")
  public void cloneableParameterIsSnapshotted() {
    List<String> live = new ArrayList<>();
    live.add("first");

    TestResult result = TestResult.newTestResult(methodStub(), new Object[0], 0);
    result.setParameters(new Object[] {live});
    live.add("second");

    assertThat(result.getParameters()[0]).isNotSameAs(live).isEqualTo(singletonList("first"));
  }

  @Test
  public void nonCloneableParameterIsKeptByReference() {
    Object parameter = new Object();

    TestResult result = TestResult.newTestResult(methodStub(), new Object[0], 0);
    result.setParameters(new Object[] {parameter});

    assertThat(result.getParameters()[0]).isSameAs(parameter);
  }

  @Test
  public void nullParameterSurvives() {
    TestResult result = TestResult.newTestResult(methodStub(), new Object[0], 0);
    result.setParameters(new Object[] {null});

    assertThat(result.getParameters()[0]).isNull();
  }

  /**
   * A result cannot be built without a method, so these tests need one; nothing here reads it. The
   * parameters still go through setParameters, which is what this class is about.
   */
  private static ITestNGMethod methodStub() {
    ITestClass testClass = mock(ITestClass.class);
    when(testClass.getName()).thenReturn("testClass");
    ITestNGMethod method = mock(ITestNGMethod.class);
    when(method.getMethodName()).thenReturn("testMethod");
    when(method.getTestClass()).thenReturn(testClass);
    return method;
  }
}
