package test.inheritance.testng234;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.Arrays;

public class PolymorphicFailureTest extends SimpleBaseTest {

  @Test
  public void superclassFailureShouldCauseFailure() {
    TestNG tng = create(ChildTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertTestResultsEqual(tla.getSkippedTests(), Arrays.asList(
        "polymorphicMethod",
        "executePolymorphicMethod")
        );
    Assert.assertEquals(0, tla.getPassedTests().size());
    Assert.assertEquals(0, tla.getFailedTests().size());
  }
}
