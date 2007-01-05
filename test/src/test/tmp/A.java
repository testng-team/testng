package test.tmp;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class A {

	@Test
	public void testObject() {
    System.out.println("TEST, THREAD:" + Thread.currentThread().getId());
	}
}
