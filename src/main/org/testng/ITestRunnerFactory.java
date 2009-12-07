package org.testng;


import org.testng.xml.XmlTest;

import java.util.List;

/**
 * A factory for TestRunners to be used by SuiteRunners.
 */
public interface ITestRunnerFactory {
	TestRunner newTestRunner(final ISuite suite, final XmlTest test,
	    List<IInvokedMethodListener> listeners);
}
