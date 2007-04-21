package org.testng.gigaspaces;

import java.util.UUID;

import org.testng.ISuite;

/**
 * @author	Guy Korland
 * 
 * @date 	April 20, 2007
 */
public class ResultEntry 
{
	private ISuite _suite;
	private UUID 	  m_testID;

	public ResultEntry() {}

	public ResultEntry( ISuite suite, UUID testID)
	{
		_suite = suite;
		m_testID = testID;
	}

	public ISuite getSuite()
	{
		return _suite;
	}

	public void setSuite( ISuite suite)
	{
		_suite = suite;
	}
	
	public UUID getTestID()
	{
		return m_testID;
	}

	public void setTestID(UUID testid)
	{
		m_testID = testid;
	}
}