package org.testng.util;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of IRetryAnalyzer that allows you to specify
 * the maximum number of times you want your test to be retried.
 *
 * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
 */
public abstract class RetryAnalyzerCount implements IRetryAnalyzer {

  // Default retry once.
  AtomicInteger count = new AtomicInteger(1);

  /**
   * Set the max number of time the method needs to be retried.
   * @param count
   */
  protected void setCount(int count) {
    this.count.set(count);
  }

  /**
   * Return the current counter value
   * @return
   */
  protected int getCount(){
      return this.count.get();
  }

  /**
   * Retries the test if count is not 0.
   * @param result The result of the test.
   */
  @Override
  public boolean retry(ITestResult result) {
    boolean retry = false;

    if (count.intValue() > 0) {
      retry = retryMethod(result);
      count.decrementAndGet();
    }
    return retry;
  }

  /**
   * The method implemented by the class that test if the test
   * must be retried or not.
   * @param result The result of the test.
   * @return true if the test must be retried, false otherwise.
   */
  public abstract boolean retryMethod(ITestResult result);
}
