package org.testng.thread;

import java.util.concurrent.ExecutorService;

/**
 * Represents the capabilities of a TestNG specific {@link ExecutorService}
 *
 * @deprecated - This interface stands deprecated as of TestNG <code>v7.9.0</code>.
 */
@Deprecated
public interface ITestNGThreadPoolExecutor extends ExecutorService {

  /** Helps kick start the execution and is the point of entry for execution. */
  void run();
}
