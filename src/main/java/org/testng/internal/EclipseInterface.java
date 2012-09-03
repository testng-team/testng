package org.testng.internal;

/**
 * Symbols in this class are used by the Eclipse plug-in, do not modify them
 * without updating the plug-in as well.
 *
 * @author Cedric Beust <cedric@beust.com>
 * @since Aug 25, 2012
 */
public class EclipseInterface {
  public static final Character OPENING_CHARACTER = '[';
  public static final Character CLOSING_CHARACTER = ']';

  public static final String ASSERT_LEFT = "expected " + OPENING_CHARACTER;
  public static final String ASSERT_LEFT2 = "expected not same " + OPENING_CHARACTER;
  public static final String ASSERT_MIDDLE = CLOSING_CHARACTER + " but found " + OPENING_CHARACTER;
  public static final String ASSERT_RIGHT = Character.toString(CLOSING_CHARACTER);
}
