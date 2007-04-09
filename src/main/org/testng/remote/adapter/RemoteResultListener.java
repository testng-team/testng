/*
 * @(#)ResultListener.java   Apr 9, 2007
 *
 * Copyright 2007 GigaSpaces Technologies Inc.
 */

package org.testng.remote.adapter;

import java.util.Map;

import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.SuiteRunner;
import org.testng.reporters.TestHTMLReporter;

/**
 * This listener is called by the {@link IWorkerApadter} implementation when a remote test result is ready.
 * 
 * @author Guy Korland
 * @date April 9, 2007
 * @see IWorkerApadter
 */
public class RemoteResultListener
{
	/**
	 * Holds the corresponded {@link SuiteRunner} for the processed {@link org.testng.xml.XmlSuite}.
	 */
	final private SuiteRunner _runner;

	/**
	 * Creates a listener for an {@link org.testng.xml.XmlSuite} result.
	 * @param runner the corresponded {@link SuiteRunner}
	 */
	public RemoteResultListener( SuiteRunner runner)
	{
		_runner = runner;
	}

	/**
	 * Should called by the {@link IWorkerApadter} implementation when a remote suite result is ready. 
	 * @param remoteSuiteRunner remote result.
	 */
	public void onResult( SuiteRunner remoteSuiteRunner)
	{
		_runner.setHost(remoteSuiteRunner.getHost());
		Map<String, ISuiteResult> tmpResults = remoteSuiteRunner.getResults();
		Map<String, ISuiteResult> suiteResults = _runner.getResults();
		for (String tests : tmpResults.keySet()) 
		{
			ISuiteResult suiteResult = tmpResults.get(tests);
			suiteResults.put(tests, suiteResult);
			ITestContext tc = suiteResult.getTestContext();
			TestHTMLReporter.generateLog(tc, remoteSuiteRunner.getHost(),
			                             _runner.getOutputDirectory(),
			                             tc.getFailedConfigurations().getAllResults(),
			                             tc.getSkippedConfigurations().getAllResults(),
			                             tc.getPassedTests().getAllResults(),
			                             tc.getFailedTests().getAllResults(),
			                             tc.getSkippedTests().getAllResults(),
			                             tc.getFailedButWithinSuccessPercentageTests().getAllResults());
		}
	}
}
