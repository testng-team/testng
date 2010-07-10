package test.junit.testsetup;

import junit.framework.TestCase;

public class ATest extends TestCase
{
	public void testIt()
	{
    System.out.println("A.testIt()");
		Data d = TestSuiteContainerWrapper.getData();
		assertEquals(3,d.i);
	}
}
