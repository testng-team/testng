package org.testng.remote.strprotocol;



/**
 * Interface replicating <code>ITestListener</code> for remote listeners.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 * @see org.testng.ITestListener
 */
public interface IRemoteTestListener {
  void onStart(TestMessage tm);

  void onFinish(TestMessage tm);

  void onTestStart(TestResultMessage trm);

  void onTestSuccess(TestResultMessage trm);

  void onTestFailure(TestResultMessage trm);

  void onTestSkipped(TestResultMessage trm);

  void onTestFailedButWithinSuccessPercentage(TestResultMessage trm);
}
