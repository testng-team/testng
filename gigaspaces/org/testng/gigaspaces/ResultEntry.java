package org.testng.gigaspaces;

import org.testng.ISuite;

/**
 * @author	Guy Korland
 * @version	1.0
 */
public class ResultEntry 
{
	private ISuite _suite;

	public ResultEntry() {}

	public ResultEntry( ISuite suite)
	{
		_suite = suite;
	}

	public ISuite getSuite()
	{
		return _suite;
	}

	public void setSuite( ISuite suite)
	{
		_suite = suite;
	}
}