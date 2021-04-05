package test.junit.testsetup;

import junit.framework.Test;
import junit.framework.TestSuite;

public class LayerATestSuite
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Layer A Test Suite" );

		suite.addTestSuite( ATest.class );

		TestSuiteContainerWrapper wrapper = new TestSuiteContainerWrapper(suite, Data.class);
		return wrapper;
	}
}
