package org.testng.internal.invokers;

/** Helps keep track of when a method was invoked */
public interface IInvocationStatus {

  /** @param date - The timestamp at which a method was invoked. */
  void setInvokedAt(long date);

  /** @return - The timestamp at which a method got invoked. */
  long getInvocationTime();
}
