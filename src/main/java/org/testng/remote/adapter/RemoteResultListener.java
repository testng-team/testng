/*
 * @(#)ResultListener.java   Apr 9, 2007
 *
 * Copyright 2007 GigaSpaces Technologies Inc.
 */

package org.testng.remote.adapter;

import java.util.Map;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.SuiteRunner;
import org.testng.reporters.TestHTMLReporter;

/**
 * This listener is called by the {@link IWorkerAdapter} implementation when a remote test result is ready.
 *
 * @author Guy Korland
 * @since April 9, 2007
 * @see IWorkerAdapter
 */
public class RemoteResultListener
{
	/**
	 * Holds the corresponded {@link SuiteRunner} for the processed {@link org.testng.xml.XmlSuite}.
	 */
	final private SuiteRunner m_runner;

	/**
	 * Creates a listener for an {@link org.testng.xml.XmlSuite} result.
	 * @param runner the corresponded {@link SuiteRunner}
	 */
	public RemoteResultListener( SuiteRunner runner)
	{
		m_runner = runner;
	}

	/**
	 * Should called by the {@link IWorkerAdapter} implementation when a remote suite result is ready.
	 * @param remoteSuiteRunner remote result.
	 */
	public void onResult( ISuite remoteSuiteRunner)
	{
		m_runner.setHost(remoteSuiteRunner.getHost());
		Map<String, ISuiteResult> tmpResults = remoteSuiteRunner.getResults();
		Map<String, ISuiteResult> suiteResults = m_runner.getResults();
		for (Map.Entry<String, ISuiteResult> entry : tmpResults.entrySet())
		{
			ISuiteResult suiteResult = entry.getValue();
			suiteResults.put(entry.getKey(), suiteResult);
			ITestContext tc = suiteResult.getTestContext();
			TestHTMLReporter.generateLog(tc, remoteSuiteRunner.getHost(),
			                             m_runner.getOutputDirectory(),
			                             tc.getFailedConfigurations().getAllResults(),
			                             tc.getSkippedConfigurations().getAllResults(),
			                             tc.getPassedTests().getAllResults(),
			                             tc.getFailedTests().getAllResults(),
			                             tc.getSkippedTests().getAllResults(),
			                             tc.getFailedButWithinSuccessPercentageTests().getAllResults());
		}
	}
}
