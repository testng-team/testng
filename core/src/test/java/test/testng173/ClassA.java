package test.testng173;

import org.testng.annotations.Test;

public class ClassA {

	@Test
	public void test1() {
	}

	@Test(dependsOnMethods = "test1")
	public void test2() {
	}

}
