package org.testng.internal.junit;

import java.util.ArrayList;
import java.util.List;

/** Thrown when two array elements differ */
public class ArrayComparisonFailure extends AssertionError {

  private final List<Integer> fIndices = new ArrayList<>();
  private final String fMessage;
  private final AssertionError fCause;

  /**
   * Construct a new <code>ArrayComparisonFailure</code> with an error text and the array's
   * dimension that was not equal
   *
   * @param message the message
   * @param cause the exception that caused the array's content to fail the assertion test
   * @param index the array position of the objects that are not equal.
   */
  public ArrayComparisonFailure(String message, AssertionError cause, int index) {
    fMessage = message;
    fCause = cause;
    addDimension(index);
  }

  public void addDimension(int index) {
    fIndices.add(0, index);
  }

  @Override
  public String getMessage() {
    StringBuilder builder = new StringBuilder();
    if (fMessage != null) builder.append(fMessage);
    builder.append("arrays first differed at element ");
    for (int each : fIndices) {
      builder.append("[");
      builder.append(each);
      builder.append("]");
    }
    builder.append("; ");
    builder.append(fCause.getMessage());
    return builder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return getMessage();
  }
}
