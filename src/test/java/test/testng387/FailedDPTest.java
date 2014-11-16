package test.testng387;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
	static List<Integer> primes = new ArrayList<Integer>();
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

}
