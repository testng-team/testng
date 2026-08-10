package org.testng.cli;

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

  public CliParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
