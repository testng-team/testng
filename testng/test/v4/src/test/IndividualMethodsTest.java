package test;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Test that if an individual method is specified on testng.xml, the @Configuration
 * method still runs.
 * 
 * Created on Aug 1, 2005
 * @author cbeust
 */
public class IndividualMethodsTest
{
    private boolean m_setUpCalled = false;

    @Configuration(beforeTestMethod = true)
    public void setUp()
    {
        m_setUpCalled = true;
    }

    @Test
    public void testMethod()
    {
        // this line causes the test to fail, showing that setUp() hadn't been run
        Assert.assertTrue(m_setUpCalled);
    }
}
