package test.junit.testsetup;


import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class LoggingTestSuite extends TestSuite
{
	public LoggingTestSuite( String string )
	{
		super( string );
	}

	@Override
  public void run( TestResult result )
	{
		super.run( result );
	}

	@Override
  public void runTest( Test test, TestResult result )
	{
		super.runTest( test, result );
	}
}
