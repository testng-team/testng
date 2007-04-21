package org.testng.gigaspaces;

import java.io.IOException;
import java.util.Properties;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;

import org.testng.ISuite;
import org.testng.remote.adapter.IWorkerApadter;
import org.testng.xml.XmlSuite;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.LocalTransactionManager;
import com.j_spaces.core.client.SpaceFinder;


/**
 * Provides a Worker adapter based on Gigaspaces space implementation.
 * 
 * @author	Guy Korland
 * 
 * @date 	April 20, 2007
 */
public class SpaceWorkerAdapter implements IWorkerApadter
{
	public static final String SPACE_URL = "gigaspaces.url";
	
	final private SuiteEntry _suiteTemplate	= new SuiteEntry();

	private TransactionManager m_tm; 
	private IJSpace				m_space;
	private Transaction			m_currentTransaction;
	private SuiteEntry			m_currentSuite;

	public SpaceWorkerAdapter()
	{
	}
	
	/*
	 * @see org.testng.remote.adapter.IWorkerApadter#init(java.util.Properties)
	 */
	public void init(Properties prop) throws Exception
	{
		String url = prop.getProperty(SPACE_URL, "jini://*/*/TestNGSpace?groups=TestNG");
		m_space = (IJSpace) SpaceFinder.find(url);
		m_tm = LocalTransactionManager.getInstance(m_space);
	}

	/*
	 * @see org.testng.remote.adapter.IWorkerApadter#getSuite(long)
	 */
	public XmlSuite getSuite(long timeout) throws InterruptedException,
	IOException
	{
		try
		{
			//TODO set transaction timeout 
			m_currentTransaction = TransactionFactory.create(m_tm, 100000).transaction;
			
			//TODO set wait timeout
			m_currentSuite = (SuiteEntry) m_space.take( _suiteTemplate, m_currentTransaction, Long.MAX_VALUE);
			return m_currentSuite.getSuite();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * @see org.testng.remote.adapter.IWorkerApadter#returnResult(org.testng.ISuite)
	 */
	public void returnResult(ISuite result) throws IOException
	{
		try
		{
			m_space.write(new ResultEntry(result, m_currentSuite.getTestID()), m_currentTransaction, Lease.FOREVER);
			m_currentTransaction.commit();
		}
		catch (TransactionException e)
		{
			e.printStackTrace();
		}
		finally
		{
			m_currentTransaction = null;
			m_currentSuite = null;
		}
	}
}
