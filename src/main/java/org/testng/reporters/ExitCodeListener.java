package org.testng.reporters;

import org.testng.TestNG;

/**
 * A very simple <code>ITestListener</code> used by the TestNG runner to
 * find out the exit code.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class ExitCodeListener extends TestNG.ExitCodeListener {
  public ExitCodeListener() {
    super();
  }

  public ExitCodeListener(TestNG runner) {
    super(runner);
  }
}
