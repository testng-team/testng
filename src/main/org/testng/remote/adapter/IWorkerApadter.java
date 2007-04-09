package org.testng.remote.adapter;

import java.util.Properties;

import org.testng.ISuite;
import org.testng.xml.XmlSuite;

/**
 * This interface should be implemented by the Master-Slave transport adapter.
 * This interface is used by the Slave to pull suites and return results.
 *   
 * @author	Guy Korland
 * @date April 9, 2007
 * @see IMasterAdapter
 */
public interface IWorkerApadter
{
	/**
	 * Initializes the worker adapter.  
	 * @param properties holds the properties loaded from the remote.properties file. 
	 */
	void init( Properties properties);
	
	/**
	 * A blocking call to get the next Suite to test. 
	 * @param timeout the maximum time to wait for the next suite. 
	 * @return the next suite avaliable or <code>null</code> if the timeout has reached.
	 * @throws InterruptedException
	 */
	XmlSuite getSuite( long timeout) throws InterruptedException;
	
	/**
	 * Return a suite result.
	 * @param result the result to return
	 */
	void returnResult( ISuite result);
}
