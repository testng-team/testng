package org.testng.remote.strprotocol;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestResultMessageTest {

	private TestResultMessage testResultMessage = new TestResultMessage(0,
			"suiteName", "testName", "className", "methodName",
			"testDescriptor", "testinstanceName", new String[] {}, 0, 0,
			"stackTrace", 1, 0);

	@DataProvider(name = "testGen")
	private Object[][] getP() {
		return new Object[][] {
				{ null, new Class[] { null }, Arrays.asList() },
				{ new Object[] { new byte[] { 1 } },
						new Class[] { byte[].class }, Arrays.asList("[1]") },
				{ new Object[] { new short[] { 1 } },
						new Class[] { short[].class }, Arrays.asList("[1]") },
				{ new Object[] { new long[] { 1 } },
						new Class[] { long[].class }, Arrays.asList("[1]") },
				{ new Object[] { new int[] { 1, 2, 3 } },
						new Class[] { int[].class }, Arrays.asList("[1,2,3]") },
				{ new Object[] { new boolean[] { true, false } },
						new Class[] { boolean[].class },
						Arrays.asList("[true,false]") },
				{ new Object[] { new char[] { 'a', 'b', 'c' } },
						new Class[] { int[].class }, Arrays.asList("[a,b,c]") },
				{ new Object[] { new float[] { 1.1f, 2.2f, 3.3f } },
						new Class[] { float[].class },
						Arrays.asList("[1.1,2.2,3.3]") },
				{ new Object[] { new double[] { 1.1, 2.2, 3.3 } },
						new Class[] { double[].class },
						Arrays.asList("[1.1,2.2,3.3]") },
				{ new Object[] { new Object[] { "this is a string", false } },
						new Class[] { String.class, Boolean.class },
						Arrays.asList("[this is a string,false]") },
				{ new Object[] { new Object[] { null, "" } },
						new Class[] { String.class, Boolean.class },
						Arrays.asList("[null,\"\"]") }, };
	}

	@Test(dataProvider = "testGen")
	public void toStringTest(Object[] objects, Class<?>[] objectClasses,
			List<String> expectedResults) throws Exception {
		String[] results = testResultMessage.toString(objects, objectClasses);
		Assert.assertEquals(Arrays.asList(results), expectedResults);
	}
}
