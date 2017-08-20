package org.testng;


import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.List;

/**
 * A factory for TestRunners to be used by SuiteRunners.
 */
public interface ITestRunnerFactory {
	TestRunner newTestRunner(ISuite suite, XmlTest test,
	    Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners);
}
