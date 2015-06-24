package org.testng.remote.adapter;

import java.io.IOException;
import java.util.Properties;

import org.testng.xml.XmlSuite;

/**
 * This interface should be implemented by the Master-Slave transport adapter.
 * This interface is used by the Master to push suites and get results.
 *
 * @author Guy Korland
 * @since April 9, 2007
 * @see IWorkerAdapter
 */
public interface IMasterAdapter
{
	/**
	 * Initializes the Master adapter.
	 * @param prop holds the properties loaded from the remote.properties file.
	 * @throws Exception adapter might throw any exception on initialization, which will abort this adapter.
	 */
	void init( Properties prop) throws Exception;

	/**
	 * Run a suite remotely.
	 * @param suite the suite to send.
	 * @param listener the corresponded listener, should be called when result is ready.
	 * @throws IOException might be thrown on IO error.
	 */
	void runSuitesRemotely( XmlSuite suite, RemoteResultListener listener) throws IOException;

	/**
	 * A blocking wait for the remote results to return.
	 *
	 * @param timeout the maximum time to wait for all the suites to return a result.
	 * @throws InterruptedException
	 */
	public void awaitTermination(long timeout) throws InterruptedException;
}