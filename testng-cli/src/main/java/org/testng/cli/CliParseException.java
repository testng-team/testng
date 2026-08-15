package org.testng.cli;

import org.jspecify.annotations.Nullable;
import org.testng.TestNGException;

/**
 * Raised when a command line cannot be parsed or fails validation. Command line front ends report
 * the message and the usage banner, then exit with a failure status.
 *
 * @since 7.13
 */
public class CliParseException extends TestNGException {

  private static final long serialVersionUID = 1L;

  public CliParseException(String message) {
    super(message);
  }

  /**
   * Only this overload takes a nullable message: it exists to wrap another exception, and {@link
   * Throwable#getMessage()} is allowed to return {@code null}. A front end raising a parse failure
   * on its own has a message to give and uses {@link #CliParseException(String)}.
   */
  public CliParseException(@Nullable String message, Throwable cause) {
    super(message, cause);
  }
}
