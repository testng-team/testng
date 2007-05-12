package org.testng.gigaspaces;

import java.io.IOException;
import java.util.Properties;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.Transaction.Created;
import net.jini.core.transaction.server.TransactionManager;

import org.testng.ISuite;
import org.testng.internal.Utils;
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
	public static final String SLAVE_TIMEOUT = "gigaspaces.slave.timeout";
	
	final private SuiteEntry _suiteTemplate	= new SuiteEntry();

	private long 					m_transactionTimeout;
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
		//default 5 min
		m_transactionTimeout = Integer.parseInt( prop.getProperty(SLAVE_TIMEOUT, "300000"));
	}

	/*
	 * @see org.testng.remote.adapter.IWorkerApadter#getSuite(long)
	 */
	public XmlSuite getSuite(long timeout) throws InterruptedException,
	IOException
	{
		try
		{
			Created created = TransactionFactory.create(m_tm, m_transactionTimeout);
			m_currentTransaction = created.transaction;
			
			m_currentSuite = (SuiteEntry) m_space.take( _suiteTemplate, m_currentTransaction, m_transactionTimeout);
			created.lease.renew(m_transactionTimeout);
			return m_currentSuite.getSuite();
		}
		catch (TransactionException e)
		{
			Utils.log( "Transaction error", 1, e.toString()); 
		}
		catch (LeaseException e)
		{
			Utils.log( "Lease error", 1, e.toString()); 
		}
		catch (UnusableEntryException e)
		{
			IOException ex = new IOException();
			ex.initCause(e);
			throw ex;
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
			Utils.log( "Transaction error", 0, e.toString()); 
		}
		finally
		{
			m_currentTransaction = null;
			m_currentSuite = null;
		}
	}
}
