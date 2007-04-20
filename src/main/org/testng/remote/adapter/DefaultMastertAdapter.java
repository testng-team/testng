package org.testng.remote.adapter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.internal.Utils;
import org.testng.internal.remote.SlavePool;
import org.testng.internal.thread.ThreadUtil;
import org.testng.remote.RemoteSuiteWorker;
import org.testng.xml.XmlSuite;

/**
 * Default Master adapter, provides an adapter based on hosts file.
 * 
 *  
 * @author	Guy Korland
 * @date 	April 20, 2007
 */
public class DefaultMastertAdapter
implements IMasterAdapter
{
	public static final String HOSTS 	= "testng.hosts";

	private String[] _hosts;

	final private SlavePool m_slavePool = new SlavePool();
	final private List<Runnable> m_workers = new ArrayList<Runnable>();


	/*
	 * @see org.testng.remote.adapter.IMasterAdapter#init(java.util.Properties)
	 */
	public void init(Properties properties)
	{
		String hostLine = properties.getProperty(HOSTS);
		_hosts =  hostLine.split(" ");

		//
		// Create one socket per host found
		//
		Socket[] sockets = new Socket[_hosts.length];
		for (int i = 0; i < _hosts.length; i++) {
			String host = _hosts[i];
			String[] s = host.split(":");
			try {
				sockets[i] = new Socket(s[0], Integer.parseInt(s[1]));
			}
			catch (NumberFormatException e) {
				e.printStackTrace(System.out);
			}
			catch (UnknownHostException e) {
				e.printStackTrace(System.out);
			}
			catch (IOException e) {
				Utils.error("Couldn't connect to " + host + ": " + e.getMessage());
			}
		}

		//
		// Add these hosts to the pool
		//
		try {
			m_slavePool.addSlaves(sockets);
		}
		catch (IOException e1) {
			e1.printStackTrace(System.out);
		}
	}

	/*
	 * @see org.testng.remote.adapter.IMasterAdapter#runSuitesRemotely(java.util.List, org.testng.internal.annotations.IAnnotationFinder, org.testng.internal.annotations.IAnnotationFinder)
	 */
	public void runSuitesRemotely( XmlSuite suite, RemoteResultListener listener) throws IOException
	{
		m_workers.add(new RemoteSuiteWorker(suite, m_slavePool, listener));
	}

	public void awaitTermination(long timeout) throws InterruptedException
	{
		ThreadUtil.execute(m_workers, 1, 10 * 1000L, false);
	}
}
