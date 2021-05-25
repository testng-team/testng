package test.junit.testsetup;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SmokeSuite extends LoggingTestSuite
{
	public static void main( String[] args )
	{
		junit.textui.TestRunner.run( suite() );
	}

	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Smoke Test Suite" );

		suite.addTest(LayerATestSuite.suite());

		return suite;
	}

//	public SmokeSuite()
//	{
//		this("SmokeSuite");
//	}

	public SmokeSuite( String name )
	{
		super( name );
	}
}
