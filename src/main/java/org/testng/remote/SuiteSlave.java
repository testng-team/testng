package org.testng.remote;

import java.util.List;
import java.util.Properties;

import org.testng.ISuite;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.PropertiesFile;
import org.testng.internal.Utils;
import org.testng.remote.adapter.DefaultWorkerAdapter;
import org.testng.remote.adapter.IWorkerAdapter;
import org.testng.xml.XmlSuite;

/**
 * Run test suits sent by the dispatcher.
 *
 *
 * @author	Guy Korland
 * @since 	April 20, 2007
 */
public class SuiteSlave
{

	/**
	 * Properties allowed in remote.properties
	 */
	public static final String VERBOSE = "testng.verbose";
	public static final String SLAVE_ADPATER = "testng.slave.adpter";


	final private int m_verbose;
	final private IWorkerAdapter m_slaveAdpter;
	final private TestNG m_testng;

	/**
	 * Creates a new suite dispatcher.
	 */
	public SuiteSlave( String propertiesFile, TestNG testng) throws TestNGException
	{
		try
		{
			m_testng = testng;

			PropertiesFile file = new PropertiesFile( propertiesFile);
			Properties properties = file.getProperties();

			m_verbose = Integer.parseInt( properties.getProperty(VERBOSE, "1"));

			String adapter = properties.getProperty(SLAVE_ADPATER);
			if( adapter == null)
			{
				m_slaveAdpter = new DefaultWorkerAdapter();
			}
			else
			{
				Class clazz = Class.forName(adapter);
				m_slaveAdpter = (IWorkerAdapter)clazz.newInstance();
			}
			m_slaveAdpter.init(properties);
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
	 */
	public void waitForSuites() {
		try {
			while (true) {
				//TODO set timeout
				XmlSuite s = m_slaveAdpter.getSuite(Long.MAX_VALUE);
				if( s== null) {
          continue;
        }
				log("Processing " + s.getName());
				List<XmlSuite> suites = Lists.newArrayList();
				suites.add(s);
				m_testng.setXmlSuites(suites);
				List<ISuite> suiteRunners = m_testng.runSuitesLocally();
				ISuite sr = suiteRunners.get(0);
				log("Done processing " + s.getName());
				m_slaveAdpter.returnResult(sr);
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
