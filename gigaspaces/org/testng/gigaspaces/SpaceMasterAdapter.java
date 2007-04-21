package org.testng.gigaspaces;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import net.jini.core.lease.Lease;

import org.testng.remote.adapter.IMasterAdapter;
import org.testng.remote.adapter.RemoteResultListener;
import org.testng.xml.XmlSuite;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SpaceFinder;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a Master adapter based on Gigaspaces space implementation.
 * 
 * @author	Guy Korland
 * 
 * @date 	April 20, 2007
 */
public class SpaceMasterAdapter implements IMasterAdapter
{
	public static final String SPACE_URL = "gigaspaces.url";
	
	private IJSpace					m_space;
	
	final private ConcurrentHashMap m_listeners = new ConcurrentHashMap(); 
	
	final private SuitesCounter m_counter = new SuitesCounter();
	private ResultCollector m_collector;
	
	/*
	 * @see org.testng.remote.adapter.IWorkerApadter#init(java.util.Properties)
	 */
	public void init(Properties prop) throws Exception
	{
		String url = prop.getProperty(SPACE_URL, "/./TestNGSpace?groups=TestNG");
		m_space = (IJSpace) SpaceFinder.find(url);
		m_collector = new ResultCollector();
		m_collector.start();
	}

	/*
	 * @see org.testng.remote.adapter.IMasterAdapter#awaitTermination(long)
	 */
	public void awaitTermination(long timeout) throws InterruptedException
	{
		try
		{
			m_counter.waitFor( timeout);
		}
		finally
		{
			m_collector.shutDown();
		}
	}

	/*
	 * @see org.testng.remote.adapter.IMasterAdapter#runSuitesRemotely(org.testng.xml.XmlSuite, org.testng.remote.adapter.RemoteResultListener)
	 */
	public void runSuitesRemotely(XmlSuite suite, RemoteResultListener listener)
	throws IOException
	{
		UUID testID = UUID.randomUUID();
		SuiteEntry suiteEntry	= new SuiteEntry( suite, testID);
		try
		{
			m_listeners.putIfAbsent(testID, listener);
			m_space.write(suiteEntry, null, Lease.FOREVER);
			m_counter.inc();
		}
		catch (Exception e)
		{
			m_listeners.remove( testID);
			e.printStackTrace();
		}
	}

	private class ResultCollector extends Thread
	{
		final private ResultEntry resultTemplate = new ResultEntry();
		volatile private boolean m_shutdown = false;  

		public ResultCollector()
		{
			this.setDaemon(true);
			this.setName("ResultCollector");
		}

		public void run()
		{
			while( !m_shutdown)
			{
				try
				{
					ResultEntry rs = (ResultEntry) m_space.take(resultTemplate, null,
					                                           Long.MAX_VALUE);
					RemoteResultListener listener = (RemoteResultListener)m_listeners.remove(rs.getTestID());
					listener.onResult(rs.getSuite());
					m_counter.dec();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		public void shutDown()
		{
			m_shutdown = true;
			this.interrupt();
		}
	}


	private static class SuitesCounter
	{
		private AtomicInteger m_count = new AtomicInteger(1); 
		
		public void dec()
		{
			int c = m_count.decrementAndGet();
			if( c == 0)
			{
				synchronized (this)
				{
					notifyAll();
				}
			}
		}
		
		public void inc()
		{
			m_count.incrementAndGet();
		}
		
		public void waitFor( long timeout) throws InterruptedException
		{
			m_count.decrementAndGet();
			synchronized (this)
			{
				wait( timeout);	
			}
			
		}
	}
}
