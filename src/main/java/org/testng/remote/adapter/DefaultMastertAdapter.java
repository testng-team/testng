package org.testng.remote.adapter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import org.testng.collections.Lists;
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
 * @since 	April 20, 2007
 */
public class DefaultMastertAdapter
implements IMasterAdapter
{
	public static final String HOSTS 	= "testng.hosts";

	private String[] m_hosts;

	final private SlavePool m_slavePool = new SlavePool();
	final private List<Runnable> m_workers = Lists.newArrayList();

	/*
	 * @see org.testng.remote.adapter.IMasterAdapter#init(java.util.Properties)
	 */
	@Override
  public void init(Properties properties)
	{
		String hostLine = properties.getProperty(HOSTS);
		m_hosts =  hostLine.split(" ");

		//
		// Create one socket per host found
		//
		Socket[] sockets = new Socket[m_hosts.length];
		for (int i = 0; i < m_hosts.length; i++) {
			String host = m_hosts[i];
			String[] s = host.split(":");
			try {
				sockets[i] = new Socket(s[0], Integer.parseInt(s[1]));
			}
			catch (NumberFormatException | UnknownHostException e) {
				e.printStackTrace(System.out);
			} catch (IOException e) {
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
	@Override
  public void runSuitesRemotely( XmlSuite suite, RemoteResultListener listener) throws IOException
	{
		m_workers.add(new RemoteSuiteWorker(suite, m_slavePool, listener));
	}

	@Override
  public void awaitTermination(long timeout) throws InterruptedException
	{
		ThreadUtil.execute(m_workers, 1, 10 * 1000L, false);
	}
}
