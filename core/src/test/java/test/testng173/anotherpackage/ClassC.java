package test.testng173.anotherpackage;

import org.testng.annotations.Test;

public class ClassC {

	@Test
	public void testX() {

	}

	@Test(dependsOnMethods = "testX")
	public void test2() {

	}

	@Test(dependsOnMethods = "test2")
	public void test1() {

	}

}