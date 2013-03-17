package org.testng;

/**
 * The root exception for special skip handling. In case a @Test or @Configuration
 * throws this exception the method will be considered a skip or a failure according to the
 * return of {@link #isSkip()}.
 * Users may provide extensions to this mechanism by extending this class.
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 * @since 5.6
 */
public class SkipException extends RuntimeException {
  private static final long serialVersionUID = 4052142657885527260L;
  private StackTraceElement[] m_stackTrace;
  private volatile boolean m_stackReduced;

  public SkipException(String skipMessage) {
    super(skipMessage);
  }

  public SkipException(String skipMessage, Throwable cause) {
    super(skipMessage, cause);
  }

  /**
   * Flag if the current exception marks a skipped method (<tt>true</tt>)
   * or a failure (<tt>false</tt>). By default Subclasses should override this method
   * in order to provide smarter behavior.
   *
   * @return <tt>true</tt> if the method should be considered a skip,
   *    <tt>false</tt> if the method should be considered failed. If not
   *    overwritten it returns <tt>true</tt>
   */
  public boolean isSkip() {
    return true;
  }

  /**
   * Subclasses may use this method to reduce the printed stack trace.
   * This method keeps only the last frame.
   * <b>Important</b>: after calling this method the preserved internally
   * and can be restored called {@link #restoreStackTrace}.
   */
  protected void reduceStackTrace() {
    if(!m_stackReduced) {
      synchronized(this) {
        StackTraceElement[] newStack= new StackTraceElement[1];
        StackTraceElement[] originalStack= getStackTrace();
        if(originalStack.length > 0) {
          m_stackTrace= originalStack;
          newStack[0]= getStackTrace()[0];
          setStackTrace(newStack);
        }
        m_stackReduced= true;
      }
    }
  }

  /**
   * Restores the original exception stack trace after a
   * previous call to {@link #reduceStackTrace()}.
   *
   */
  protected void restoreStackTrace() {
    if(m_stackReduced && null != m_stackTrace) {
      synchronized(this) {
        setStackTrace(m_stackTrace);
        m_stackReduced= false;
      }
    }
  }
}
