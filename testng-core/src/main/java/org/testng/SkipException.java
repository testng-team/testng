package org.testng;

import org.jspecify.annotations.Nullable;
import org.testng.internal.AutoCloseableLock;

/**
 * An exception that makes TestNG report a method as skipped.
 *
 * <p>When a test method or a configuration method throws this exception, the answer of {@link
 * #isSkip()} decides the result. TestNG reports the method as skipped or as failed. You can extend
 * this class to change that decision.
 *
 * @since 5.6
 */
public class SkipException extends RuntimeException {

  private static final long serialVersionUID = 4052142657885527260L;

  private StackTraceElement @Nullable [] m_stackTrace;
  private volatile boolean m_stackReduced;

  /**
   * Creates the exception with a message.
   *
   * @param skipMessage the reason for the skip.
   */
  public SkipException(String skipMessage) {
    super(skipMessage);
  }

  /**
   * Creates the exception with a message and a cause.
   *
   * @param skipMessage the reason for the skip.
   * @param cause the exception that caused the skip.
   */
  public SkipException(String skipMessage, Throwable cause) {
    super(skipMessage, cause);
  }

  /**
   * Tells TestNG if the method that threw this exception is skipped or failed.
   *
   * <p>This implementation returns {@code true}. A subclass can override it to decide in a
   * different way.
   *
   * @return {@code true} when TestNG must report the method as skipped. {@code false} when TestNG
   *     must report it as failed.
   */
  public boolean isSkip() {
    return true;
  }

  private final AutoCloseableLock internalLock = new AutoCloseableLock();

  /**
   * Cuts the stack trace down to its top frame, the place where the exception was created.
   *
   * <p>A subclass can call this method to print a shorter stack trace. This exception keeps the
   * full stack trace, so {@link #restoreStackTrace()} can bring it back.
   */
  protected void reduceStackTrace() {
    if (!m_stackReduced) {
      try (AutoCloseableLock ignore = internalLock.lock()) {
        StackTraceElement[] newStack = new StackTraceElement[1];
        StackTraceElement[] originalStack = getStackTrace();
        if (originalStack.length > 0) {
          m_stackTrace = originalStack;
          newStack[0] = getStackTrace()[0];
          setStackTrace(newStack);
        }
        m_stackReduced = true;
      }
    }
  }

  /** Brings back the full stack trace after a call to {@link #reduceStackTrace()}. */
  protected void restoreStackTrace() {
    if (m_stackReduced && null != m_stackTrace) {
      try (AutoCloseableLock ignore = internalLock.lock()) {
        setStackTrace(m_stackTrace);
        m_stackReduced = false;
      }
    }
  }
}
