package org.testng.gigaspaces;

import java.util.UUID;

import org.testng.xml.XmlSuite;

/**
 * @author	Guy Korland
 * 
 * @date 	April 20, 2007
 */
public class SuiteEntry
{
	private XmlSuite m_suite;
	private UUID 	  m_testID;	
	
	public SuiteEntry() {}
	
	public SuiteEntry( XmlSuite suite, UUID testID)
	{
		m_suite = suite;
		m_testID = testID;
	}
	
	public XmlSuite getSuite()
	{
		return m_suite;
	}
	
	public void setSuite( XmlSuite suite)
	{
		m_suite = suite;
	}
	
	public String getSuiteName()
	{
		return m_suite.getName();
	}
	
	public String getTestName()
	{
		return m_suite.getTest();
	}
	
	public boolean isJUnit()
	{
		return m_suite.isJUnit();
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
