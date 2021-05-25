package test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    @BeforeMethod
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
