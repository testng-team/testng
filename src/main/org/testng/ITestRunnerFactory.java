package org.testng;


import org.testng.xml.XmlTest;

/**
 * A factory for TestRunners to be used by SuiteRunners.
 * 
 * @author <a href="mailto:the_mindstorm@evolva.ro">Alex Popescu</a>
 */
public interface ITestRunnerFactory {
	TestRunner newTestRunner(final ISuite suite, final XmlTest test);
}
