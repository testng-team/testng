package test.inject.parameterresolver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.internal.Utils;
import test.SimpleBaseTest;

/** Runs one sample class in its own TestNG run and keeps what came out of it. */
public final class SampleRun {

  private final List<ITestResult> failed;

  private SampleRun(List<ITestResult> failed) {
    this.failed = failed;
  }

  public static SampleRun of(Class<?> sample, ITestNGListener... listeners) {
    ParameterRecorder.clear();
    TestNG testng = SimpleBaseTest.create(sample);
    TestListenerAdapter adapter = new TestListenerAdapter();
    testng.addListener(adapter);
    for (ITestNGListener listener : listeners) {
      testng.addListener(listener);
    }
    testng.run();
    return new SampleRun(new ArrayList<>(adapter.getFailedTests()));
  }

  public List<ITestResult> failed() {
    return failed;
  }

  /**
   * The failures as text, so an assertion that expects none prints why the sample did not run
   * rather than an empty list.
   */
  public List<String> failureMessages() {
    return failed.stream()
        .map(
            result -> {
              Throwable throwable = result.getThrowable();
              return result.getMethod().getMethodName()
                  + ": "
                  + (throwable == null ? "<no throwable>" : firstLine(throwable));
            })
        .collect(Collectors.toList());
  }

  /**
   * The exception type plus its first non blank line: a TestNG matcher diagnostic starts with a
   * newline, so taking line zero would report every failure as an empty string.
   */
  private static String firstLine(Throwable throwable) {
    String message = throwable.getMessage() == null ? "" : throwable.getMessage();
    String summary = "";
    for (String line : Utils.splitOnLiteral(message, "\n")) {
      if (!line.trim().isEmpty()) {
        summary = line.trim();
        break;
      }
    }
    return throwable.getClass().getName() + ": " + summary;
  }
}
