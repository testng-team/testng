package org.testng.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.ISuite;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.internal.PropertiesFile;
import org.testng.internal.Utils;
import org.testng.remote.adapter.DefaultWorkerAdapter;
import org.testng.remote.adapter.IWorkerApadter;
import org.testng.xml.XmlSuite;

/**
 * Run test suits sent by the dispatcher.
 * 
 *  
 * @author	Guy Korland
 * @date 	April 20, 2007
 */
public class SuiteSlave
{

	/**
	 * Properties allowed in remote.properties
	 */
	public static final String VERBOSE = "testng.verbose";
	public static final String SLAVE_ADPATER = "testng.slave.adpter";


	final private int _verbose;
	final private IWorkerApadter _slaveAdpter;
	final TestNG _testng;

	/**
	 * Creates a new suite dispatcher.
	 * 
	 * @param propertiesFile
	 * @throws Exception
	 */
	public SuiteSlave( String propertiesFile, TestNG testng) throws TestNGException
	{
		try
		{
			_testng = testng;

			PropertiesFile file = new PropertiesFile( propertiesFile);
			Properties properties = file.getProperties();

			_verbose = Integer.parseInt( properties.getProperty(VERBOSE, "1"));

			String adapter = properties.getProperty(SLAVE_ADPATER);
			if( adapter == null)
			{
				_slaveAdpter = new DefaultWorkerAdapter();
			}
			else
			{
				Class clazz = Class.forName(adapter);
				_slaveAdpter = (IWorkerApadter)clazz.newInstance();
			}
			_slaveAdpter.init(properties);
		}
		catch( Exception e)
		{
			throw new TestNGException( "Fail to initialize slave mode", e);
		}
	}

	/**
	 * Invoked in client mode.  In this case, wait for a connection
	 * on the given port, run the XmlSuite we received and return the SuiteRunner
	 * created to run it.
	 * @throws IOException 
	 */
	public void waitForSuites() {
		try {
			while (true) {
				//TODO set timeout
				XmlSuite s = _slaveAdpter.getSuite(Long.MAX_VALUE);
				log("Processing " + s.getName());
				List<XmlSuite> suites = new ArrayList<XmlSuite>();
				suites.add(s);
				_testng.setXmlSuites(suites);
				List<ISuite> suiteRunners = _testng.runSuitesLocally();
				ISuite sr = suiteRunners.get(0);
				log("Done processing " + s.getName());
				_slaveAdpter.returnResult(sr);
			}
		}
		catch(Exception ex) {
			ex.printStackTrace(System.out);
		}
	}

	private static void log(String string) {
		Utils.log("", 2, string);
	}

}
