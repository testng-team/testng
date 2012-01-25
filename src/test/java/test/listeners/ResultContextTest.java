package test.listeners;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class ResultContextTest extends SimpleBaseTest {
	
	@Test
	public void testResultContext() {
		TestNG tng = create(ResultContextListenerSample.class);
	    tng.run();
	    Assert.assertTrue(ResultContextListener.contextProvided, 
	    		"Test context was not provided to the listener");
	}
	
}
