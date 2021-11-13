package test.configuration.issue1035;

import java.util.Objects;

public class InvocationTracker {

  private final long time;
  private final long threadId;
  private final Object testInstance;

  public InvocationTracker(long time, long threadId, Object testInstance) {
    this.time = time;
    this.threadId = threadId;
    this.testInstance = testInstance;
  }

  public long getThreadId() {
    return threadId;
  }

  public long getTime() {
    return time;
  }

  public Object getTestInstance() {
    return testInstance;
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
    return testInstance == tracker.testInstance;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(testInstance);
  }
}
