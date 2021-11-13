package org.testng.thread;

import java.util.concurrent.ExecutorService;

/** Represents the capabilities of a TestNG specific {@link ExecutorService} */
public interface ITestNGThreadPoolExecutor extends ExecutorService {

  /** Helps kick start the execution and is the point of entry for execution. */
  void run();
}
