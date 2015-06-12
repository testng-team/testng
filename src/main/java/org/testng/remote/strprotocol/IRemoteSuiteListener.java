package org.testng.remote.strprotocol;



/**
 * Interface replicating the <code>ISuiteListener</code> used for remote listeners.
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 * @see org.testng.ISuiteListener
 */
public interface IRemoteSuiteListener {
  /**
   * General information about the number of suites to be run.
   * This is called once before all suites.
   *
   * @param genericMessage a message containing the number of suites that will be run
   */
  void onInitialization(GenericMessage genericMessage);

  /**
   * @see org.testng.ISuiteListener#onStart(org.testng.ISuite)
   *
   * @param suiteMessage the suite message containing the description of the suite to be run.
   */
  void onStart(SuiteMessage suiteMessage);

  /**
   * @see org.testng.ISuiteListener#onFinish(org.testng.ISuite)
   *
   * @param suiteMessage the suite message containing infos about the finished suite.
   */
  void onFinish(SuiteMessage suiteMessage);
}
