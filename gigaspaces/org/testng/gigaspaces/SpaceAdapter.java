package org.testng.gigaspaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.SuiteRunner;
import org.testng.TestNG;
import org.testng.internal.Invoker;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.LocalTransactionManager;
import com.j_spaces.core.client.SpaceFinder;


/**
 * @author	Guy Korland
 * @version	1.0
 */
public class SpaceAdapter
{
	final private TestNG				_testNG;
	final IJSpace						_space;


	public SpaceAdapter(String url, TestNG testNG) throws FinderException
	{
		_space = (IJSpace) SpaceFinder.find(url);
		_testNG = testNG;
	}

	public void waitForSuites()
	{
		SuiteEntry suiteTemplate	= new SuiteEntry();
		try
		{
			TransactionManager tm = LocalTransactionManager.getInstance(_space);
			while (true)
			{
				Transaction txn = TransactionFactory.create(tm, 100000).transaction;
				SuiteEntry entry = (SuiteEntry) _space.read(suiteTemplate, txn, Long.MAX_VALUE);
				XmlSuite suite = entry.getSuite();

				ArrayList<XmlSuite> suites = new ArrayList<XmlSuite>();
				suites.add(suite);
				_testNG.setXmlSuites(suites);
				List<ISuite> suiteRunners = _testNG.runSuitesLocally();
				for (ISuite sr : suiteRunners)
				{
					_space.write(new ResultEntry(sr), txn, Lease.FOREVER);
				}

				txn.commit();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace(System.out);
		}
	}

	public List<ISuite> runSuitesRemotely(List<XmlSuite> suites,
			IAnnotationFinder javadocAnnotationFinder,
			IAnnotationFinder jdkAnnotationFinder) throws Exception
	{

		int tests = 0;
		for (XmlSuite suite : suites)
		{
			SuiteRunner suiteRunner = new SuiteRunner(suite, _testNG
					.getOutputDirectory(), new IAnnotationFinder[] {
					javadocAnnotationFinder, jdkAnnotationFinder });
			for (XmlTest test : suite.getTests())
			{
				XmlSuite tmpSuite = new XmlSuite();
				tmpSuite.setXmlPackages(suite.getXmlPackages());
				tmpSuite.setAnnotations(suite.getAnnotations());
				tmpSuite.setJUnit(suite.isJUnit());
				tmpSuite.setName("Temporary suite for " + test.getName());
				tmpSuite.setParallel(suite.getParallel());
				tmpSuite.setParameters(suite.getParameters());
				tmpSuite.setThreadCount(suite.getThreadCount());
				tmpSuite.setVerbose(suite.getVerbose());
				tmpSuite.setObjectFactory(suite.getObjectFactory());

				XmlTest tmpTest = new XmlTest(tmpSuite);
				tmpTest.setAnnotations(test.getAnnotations());
				tmpTest.setBeanShellExpression(test.getExpression());
				tmpTest.setXmlClasses(test.getXmlClasses());
				tmpTest.setExcludedGroups(test.getExcludedGroups());
				tmpTest.setIncludedGroups(test.getIncludedGroups());
				tmpTest.setJUnit(test.isJUnit());
				tmpTest.setMethodSelectors(test.getMethodSelectors());
				tmpTest.setName(test.getName());
				tmpTest.setParallel(test.getParallel());
				tmpTest.setParameters(test.getParameters());
				tmpTest.setVerbose(test.getVerbose());
				tmpTest.setXmlClasses(test.getXmlClasses());
				tmpTest.setXmlPackages(test.getXmlPackages());

				++tests;
				SuiteEntry suiteEntry	= new SuiteEntry(tmpSuite);
				_space.write(suiteEntry, null, Lease.FOREVER);
			}
		}

		ResultEntry resultTemplate = new ResultEntry();
		List<ISuite> result = new ArrayList<ISuite>();
		for (int i = 0; i < tests; ++i)
		{
			ResultEntry rs = (ResultEntry) _space.take(resultTemplate, null,
																		Long.MAX_VALUE);
			result.add(rs.getSuite());
		}

		//
		// Run test listeners
		//
		for (ISuite suite : result)
		{
			for (ISuiteResult suiteResult : suite.getResults().values())
			{
				Collection<ITestResult> allTests[] = new Collection[] {
						suiteResult.getTestContext().getPassedTests().getAllResults(),
						suiteResult.getTestContext().getFailedTests().getAllResults(),
						suiteResult.getTestContext().getSkippedTests()
								.getAllResults(),
						suiteResult.getTestContext()
								.getFailedButWithinSuccessPercentageTests()
								.getAllResults(), };
				for (Collection<ITestResult> all : allTests)
				{
					for (ITestResult tr : all)
					{
						Invoker.runTestListeners(tr, _testNG.getTestListeners());
					}
				}
			}
		}

		return result;
	}

}
