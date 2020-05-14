package org.testng.internal.invokers;

import org.testng.IInvokedMethodListener;

/**
 * Indicates which of the methods of a {@link IInvokedMethodListener} should be called.
 *
 * @author Ansgar Konermann
 */
public enum InvokedMethodListenerMethod {
  BEFORE_INVOCATION,
  AFTER_INVOCATION
}
