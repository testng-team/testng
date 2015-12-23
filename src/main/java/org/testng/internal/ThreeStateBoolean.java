package org.testng.internal;

/**
 * For boolean use cases were 'non-existence' can not be defaulted to either true or false.
 *
 * @author <a href="mailto:nitin.matrix@gmail.com">Nitin Verma</a>
 */
public enum ThreeStateBoolean {
  NONE, TRUE, FALSE;
}
