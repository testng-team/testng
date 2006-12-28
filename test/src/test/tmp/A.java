package test.tmp;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public abstract class A {
  @Test
	public abstract Object createObject();

	@Test(dependsOnMethods = { "createObject" })
	public void testObject() {
		assertTrue(createObject() instanceof Object);
	}
}
