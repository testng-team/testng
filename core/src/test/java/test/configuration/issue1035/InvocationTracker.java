package test.configuration.issue1035;

import java.util.Objects;

public class InvocationTracker {

  private final long time;
  private final long threadId;
  private final int hashCode;

  public InvocationTracker(long time, long threadId, int hashCode) {
    this.time = time;
    this.threadId = threadId;
    this.hashCode = hashCode;
  }

  public long getThreadId() {
    return threadId;
  }

  public long getTime() {
    return time;
  }

  public int getHashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InvocationTracker tracker = (InvocationTracker) o;
    return hashCode == tracker.hashCode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(hashCode);
  }
}
