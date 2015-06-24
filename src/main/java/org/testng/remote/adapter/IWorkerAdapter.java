package org.testng.remote.adapter;

import java.io.IOException;
import java.util.Properties;

import org.testng.ISuite;
import org.testng.xml.XmlSuite;

/**
 * This interface should be implemented by the Master-Slave transport adapter.
 * This interface is used by the Slave to pull suites and return results.
 *
 * @author	Guy Korland
 * @since April 9, 2007
 * @see IMasterAdapter
 */
public interface IWorkerAdapter
{
	/**
	 * Initializes the worker adapter.
	 * @param properties holds the properties loaded from the remote.properties file.
	 * @throws Exception adapter might throw any exception on initialization, which will abort this adapter.
	 */
	void init( Properties properties) throws Exception;

	/**
	 * A blocking call to get the next Suite to test.
	 * @param timeout the maximum time to wait for the next suite.
	 * @return the next suite avaliable or <code>null</code> if the timeout has reached.
	 * @throws IOException might be thrown on IO error.
	 * @throws InterruptedException if interrupted while waiting.
	 */
	XmlSuite getSuite( long timeout) throws InterruptedException, IOException;

	/**
	 * Return a suite result.
	 * @param result the result to return
	 * @throws IOException might be thrown on IO error.
	 */
	void returnResult( ISuite result) throws IOException;
}
