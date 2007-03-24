package org.testng.gigaspaces;

import org.testng.xml.XmlSuite;

/**
 * TODO	add Javadoc
 * 
 * @author	Guyk
 * @version	1.0
 * @since	5.0
 */
public class SuiteEntry
{
	private XmlSuite _suite;
	
	public SuiteEntry() {}
	
	public SuiteEntry( XmlSuite suite)
	{
		_suite = suite;
	}
	
	public XmlSuite getSuite()
	{
		return _suite;
	}
	
	public void setSuite( XmlSuite suite)
	{
		_suite = suite;
	}
	
	public String getSuiteName()
	{
		return _suite.getName();
	}
	
	public String getTestName()
	{
		return _suite.getTest();
	}
	
	public boolean isJUnit()
	{
		return _suite.isJUnit();
	}
}
