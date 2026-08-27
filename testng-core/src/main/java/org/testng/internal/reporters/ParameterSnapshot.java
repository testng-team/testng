package org.testng.internal.reporters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * What a reporter needs to remember about the values an invocation ran with: their rendered form,
 * captured while the invocation was starting.
 *
 * <p>This is reporting state, not the arguments TestNG invoked the method with. It holds no
 * reference to the invocation objects, so nothing that happens afterwards -- a data provider
 * handing the same mutable row to every invocation, a test mutating what it was given -- can change
 * what a reporter later prints. Keeping the rendering rather than a copy of the object is
 * deliberate: it asks nothing of the user's type, where the historical {@code
 * LegacyParameterSnapshotter} needs it to honour {@link Cloneable}.
 *
 * <p>Built-in reports do not all write a value the same way, so what is held is a {@link
 * ParameterValue} rather than a string: one capture, from which the console and the XML forms are
 * each derived. The user's {@code toString()} still runs once per invocation, not once per report.
 *
 * <p>The price is that {@link Object#toString()} runs as the invocation starts rather than when the
 * report is written. See {@link org.testng.internal.Utils#toString(Object)} for what happens when
 * it throws.
 */
public final class ParameterSnapshot {

  private final int suppliedCount;
  private final int expectedCount;
  private final List<ParameterValue> values;

  private ParameterSnapshot(int suppliedCount, int expectedCount, List<ParameterValue> values) {
    this.suppliedCount = suppliedCount;
    this.expectedCount = expectedCount;
    this.values = values;
  }

  /**
   * @param parameters - The values an invocation ran with.
   * @param parameterTypes - The types the method declares, which decide how a value is rendered.
   * @return - The rendering of those values, or {@code null} when there is nothing to report.
   */
  public static @Nullable ParameterSnapshot of(
      Object @Nullable [] parameters, Class<?> @Nullable [] parameterTypes) {
    if (parameters == null || parameterTypes == null || parameters.length == 0) {
      return null;
    }
    if (parameters.length != parameterTypes.length) {
      // A data provider supplied the wrong number of values: there is no type to render each value
      // against. A reporter has both counts and reports the mismatch instead.
      return new ParameterSnapshot(
          parameters.length, parameterTypes.length, Collections.emptyList());
    }
    List<ParameterValue> values = new ArrayList<>(parameters.length);
    for (int i = 0; i < parameters.length; i++) {
      values.add(ParameterValue.of(parameters[i], parameterTypes[i]));
    }
    return new ParameterSnapshot(
        parameters.length, parameterTypes.length, Collections.unmodifiableList(values));
  }

  /**
   * Whether the invocation received a different number of values than the method declares, which a
   * reporter reports instead of the values -- there are none to report.
   */
  public boolean hasCountMismatch() {
    return suppliedCount != expectedCount;
  }

  public int suppliedCount() {
    return suppliedCount;
  }

  public int expectedCount() {
    return expectedCount;
  }

  /** The values in invocation order. Empty when {@link #hasCountMismatch()}. */
  public List<ParameterValue> values() {
    return values;
  }

  /**
   * The values as a console reporter prints them, in invocation order. Empty when {@link
   * #hasCountMismatch()}.
   */
  public List<String> renderedValues() {
    return values.stream().map(ParameterValue::rendered).collect(Collectors.toList());
  }

  /**
   * The values as the built-in HTML reports write them, in invocation order. Empty when {@link
   * #hasCountMismatch()}.
   *
   * <p>{@link ParameterValue#plain()} keeps the one hole {@link
   * org.testng.internal.Utils#toString(Object)} has -- an object whose {@code toString()} answered
   * {@code null} -- so that the two can be pinned against each other. Answering what a report
   * writes for such a value is this projection's job, and it is the word, which is what every
   * built-in report has always shown for it. Deciding it here is what keeps the reports from each
   * deciding it again.
   */
  public List<String> plainValues() {
    return values.stream().map(value -> String.valueOf(value.plain())).collect(Collectors.toList());
  }
}
