package org.testng.reporters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

public class TestHTMLReporterTest {

  @Test(description = "GITHUB-2830")
  public void generateTableParametersToStringShouldBeFailsafe() {
    ITestClass testClass = mock(ITestClass.class);
    when(testClass.getName()).thenReturn("testClass");

    ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
    when(testNGMethod.getMethodName()).thenReturn("testMethod");
    when(testNGMethod.getTestClass()).thenReturn(testClass);

    TestResult testResult = TestResult.newEmptyTestResult();
    testResult.setMethod(testNGMethod);

    testResult.setParameters(new Object[] {new ThrowingOnToString()});

    List<ITestResult> tests = Collections.singletonList(testResult);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    TestHTMLReporter.generateTable(pw, "title", tests, "cssClass", (t1, t2) -> 0);

    assertThat(sw.toString())
        .contains("Parameters: org.testng.reporters.TestHTMLReporterTest$ThrowingOnToString@");
  }

  private static class ThrowingOnToString {
    @Override
    public String toString() {
      throw new IllegalStateException("Cannot calculate toString");
    }
  }
}
