package org.testng.internal.invokers;

import org.testng.IInvokedMethodListener;
import org.testng.IInvokedMethodListener2;

/**
 * Indicates which of the methods of a {@link IInvokedMethodListener} or
 * {@link IInvokedMethodListener2} should be called.
 *
 * @author Ansgar Konermann
 */
public enum InvokedMethodListenerMethod {
  BEFORE_INVOCATION,
  AFTER_INVOCATION
}
