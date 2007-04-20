package org.testng.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SuiteRunner;
import org.testng.TestNGException;
import org.testng.internal.Invoker;
import org.testng.internal.PropertiesFile;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.remote.adapter.DefaultMastertAdapter;
import org.testng.remote.adapter.IMasterAdapter;
import org.testng.remote.adapter.RemoteResultListener;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Dispatches test suits according to the strategy defined.
 * 
 *  
 * @author	Guy Korland
 * @date 	April 20, 2007
 */
public class SuiteDispatcher
{
	/**
	 * Properties allowed in remote.properties
	 */
	public static final String MASTER_STRATEGY = "testng.master.strategy";
	public static final String VERBOSE = "testng.verbose";
	public static final String MASTER_ADPATER = "testng.master.adpter";

	/**
	 * Values allowed for STRATEGY
	 */
	public static final String STRATEGY_TEST = "test";
	public static final String STRATEGY_SUITE = "suite";

	final private int _verbose;
	final private boolean _isStrategyTest;

	final private IMasterAdapter _masterAdpter;


	/**
	 * Creates a new suite dispatcher.
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	public SuiteDispatcher( String propertiesFile) throws TestNGException
	{
		try
		{
			PropertiesFile file = new PropertiesFile( propertiesFile);
			Properties properties = file.getProperties();

			_verbose = Integer.parseInt( properties.getProperty(VERBOSE, "1"));

			String strategy = properties.getProperty(MASTER_STRATEGY, STRATEGY_SUITE);
			_isStrategyTest = STRATEGY_TEST.equalsIgnoreCase(strategy);

			String adapter = properties.getProperty(MASTER_ADPATER);
			if( adapter == null)
			{
				_masterAdpter = new DefaultMastertAdapter();
			}
			else
			{
				Class clazz = Class.forName(adapter);
				_masterAdpter = (IMasterAdapter)clazz.newInstance();
			}
			_masterAdpter.init(properties);
		}
		catch( Exception e)
		{
			throw new TestNGException( "Fail to initialize master mode", e);
		}
	}

	/**
	 * Dispatch test suites 
	 * @param suites
	 * @param outputDir
	 * @param javadocAnnotationFinder
	 * @param jdkAnnotationFinder
	 * @param testListeners
	 * @return suites result
	 */
	public List<ISuite> dispatch( List<XmlSuite> suites,
	                              String outputDir, IAnnotationFinder javadocAnnotationFinder,
	                              IAnnotationFinder jdkAnnotationFinder, List<ITestListener> testListeners){
		List<ISuite> result = new ArrayList<ISuite>();
		try
		{
			//
			// Dispatch the suites/tests
			//

			for (XmlSuite suite : suites) {
				suite.setVerbose(_verbose);
				SuiteRunner suiteRunner = new SuiteRunner(suite, outputDir,
				                                          new IAnnotationFinder[] {javadocAnnotationFinder,	jdkAnnotationFinder});
				RemoteResultListener listener = new RemoteResultListener( suiteRunner);
				if (_isStrategyTest) {
					for (XmlTest test : suite.getTests()) {
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

						_masterAdpter.runSuitesRemotely(tmpSuite, listener); 
					}
				}
				else
				{
					_masterAdpter.runSuitesRemotely(suite, listener);
				}
				result.add(suiteRunner);  
			}        

			_masterAdpter.awaitTermination(100000);

			//
			// Run test listeners
			//
			for (ISuite suite : result) {
				for (ISuiteResult suiteResult : suite.getResults().values()) {
					Collection<ITestResult> allTests[] = new Collection[] {
							suiteResult.getTestContext().getPassedTests().getAllResults(),
							suiteResult.getTestContext().getFailedTests().getAllResults(),  
							suiteResult.getTestContext().getSkippedTests().getAllResults(),  
							suiteResult.getTestContext().getFailedButWithinSuccessPercentageTests().getAllResults(),  
					};
					for (Collection<ITestResult> all : allTests) {
						for (ITestResult tr : all) {
							Invoker.runTestListeners(tr, testListeners);
						}
					}
				}
			}
		}
		catch( Exception ex)
		{
			//TODO add to logs
			ex.printStackTrace();
		}
		return result;
	}
}
