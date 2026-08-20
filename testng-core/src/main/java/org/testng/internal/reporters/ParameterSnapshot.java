package org.testng.internal.reporters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.internal.Utils;

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
 * <p>The price is that {@link Object#toString()} runs as the invocation starts rather than when the
 * report is written. See {@link ParameterSnapshots#captureIfAbsent} for what happens when it
 * throws.
 */
public final class ParameterSnapshot {

  private final int suppliedCount;
  private final int expectedCount;
  private final List<String> renderedValues;

  private ParameterSnapshot(int suppliedCount, int expectedCount, List<String> renderedValues) {
    this.suppliedCount = suppliedCount;
    this.expectedCount = expectedCount;
    this.renderedValues = renderedValues;
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
    List<String> rendered = new ArrayList<>(parameters.length);
    for (int i = 0; i < parameters.length; i++) {
      rendered.add(Utils.toString(parameters[i], parameterTypes[i]));
    }
    return new ParameterSnapshot(
        parameters.length, parameterTypes.length, Collections.unmodifiableList(rendered));
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

  /** The rendered values in invocation order. Empty when {@link #hasCountMismatch()}. */
  public List<String> renderedValues() {
    return renderedValues;
  }
}
