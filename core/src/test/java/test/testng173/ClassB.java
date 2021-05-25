package test.testng173;

import org.testng.annotations.*;

public class ClassB {
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