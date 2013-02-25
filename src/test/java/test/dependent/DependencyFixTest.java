package test.dependent;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class DependencyFixTest {
	@Test(dependsOnMethods = "helloWorld", ignoreMissingDependencies = true)
	public void dependentOnNonExistingMethod() {
		assertTrue(true);
	}

	@Test(dependsOnMethods = "dependentOnNonExistingMethod")
	public void dependentOnExistingMethod() {
		assertTrue(true);
	}

	@Test(groups = "selfSufficient", dependsOnGroups = "nonExistingGroup", ignoreMissingDependencies = true)
	public void dependentOnNonExistingGroup() {
		assertTrue(true);

	}

}
