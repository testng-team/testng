package test.testng387;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * test for http://jira.opensymphony.com/browse/TESTNG-387
 * The invocation-numbers logic in failed.xml is wrong for dataprovider and parallel=true
 *
 * The test will throw exception when numbers are prime, so getFailedInvocationNumbers() should be a list of prime numbers.
 *
 * @author freynaud
 */
public class FailedDPTest {

	// prime numbers < 10
	private List<Integer> primes = new ArrayList<Integer>();
	public FailedDPTest(){
		primes.add(2);
		primes.add(3);
		primes.add(5);
		primes.add(7);
	}


	/**
	 * DP generating all number from 0 to 9.
	 * */
	@DataProvider(name = "DP", parallel = true)
	public Iterator<Integer[]> getData() {
		List<Integer[]> list = new ArrayList<Integer[]>();
		for (int i = 0; i < 10; i++) {
			list.add(new Integer[] { i });
		}
		return list.iterator();
	}

	/**
	 * Throws an exception for a prime number.
	 * @throws Exception
	 */
	@Test(dataProvider = "DP", groups = { "DPTest" })
	public void isNotPrime(Integer i) throws Exception {
		if (primes.contains(i)){
			throw new Exception(i+" is prime");
		}
	}

	/**
	 * validates that the failed invoc number are the correct ones, ie the prime numbers.
	 * @param ctx
	 */
	@AfterClass(alwaysRun=true)
	public void check(ITestContext ctx){
		ITestNGMethod testMethod = getMethod(ctx, "isNotPrime");

		List<Integer> failed = testMethod.getFailedInvocationNumbers();
		if (failed.size() != primes.size()){
			throw new Error();
		}
		for (Integer num : primes) {
			Assert.assertTrue(failed.contains(num),num+" should be present to be retried.It is not.");
		}
	}



	private ITestNGMethod getMethod(ITestContext ctx, String methodName) {

		ITestNGMethod method = null;
		for (int i = 0; i < ctx.getAllTestMethods().length; i++) {
			method = ctx.getAllTestMethods()[i];
			if (method.getMethodName().equals(methodName)) {
				return method;
			}
		}
		throw new RuntimeException("test case creation bug.");
	}

}
