package test.ant;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
/**
 * Tests that more than one thread is used for running tests
 * @author micheb10 2 Oct 2006
 *
 */
public class MultipleThreadTest {
	public static Set<Thread> _threads;

	@BeforeClass
	public void prepareHashSet() {
		_threads=Collections.synchronizedSet(new HashSet<Thread>());
	}

	@Test
	public void recordThread00() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread01() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread02() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread03() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread04() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread05() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread06() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread07() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread08() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread09() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread10() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread11() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread12() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread13() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread14() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread15() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread16() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread17() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread18() {
		_threads.add(Thread.currentThread());
	}

	@Test
	public void recordThread19() {
		_threads.add(Thread.currentThread());
	}

	@AfterClass
	public void confirmMultipleThreads() {
		Assert.assertTrue(_threads.size()>1,"More than one thread should have been used for running the tests - "+_threads.size()+" was used");
	}
}
